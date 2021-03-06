package service.integration

import java.util.Date
import javax.inject.{Inject, Singleton}

import constants._
import data.model.Tables.{FcomtRow, FcsotRow}
import helpers.{OrderHelper, SchemeHelper}
import models._
import models.integration.enumerations._
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsValue, Json}
import repository.module.{BankRepository, IntegrationRepository, OrderRepository, SchemeRepository}
import service.{AMCService, BankService, SchemeService, ZendeskService}
import utils.DateTimeUtils

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by fincash on 19-04-2017.
  */

@Singleton
class BSEIntegrationServiceImpl @Inject()(implicit ec: ExecutionContext,
                                          bseStarOrderEntryService: BSEStarOrderEntryServiceImpl,
                                          bseUploadService: BSEStarUploadServiceImpl,
                                          orderRepository: OrderRepository,
                                          schemeRepository: SchemeRepository,
                                          schemeHelper: SchemeHelper,
                                          orderHelper: OrderHelper, zendeskService: ZendeskService,
                                          integrationRepository: IntegrationRepository,
                                          schemeService: SchemeService, aMCService: AMCService,
                                          bankRepository: BankRepository, bankService: BankService) extends IntegrationService(orderHelper, orderRepository) with DBConstants
  with IntegrationConstants with OrderConstants with DateConstants {

  val logger, log = LoggerFactory.getLogger(classOf[BSEIntegrationServiceImpl])

  /**
    *
    * @param orderModel
    * @param order
    * @param subOrder
    * @param subOrderModel
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  override def placeSubOrders(orderModel: OrderModel, order: FcomtRow, subOrder: FcsotRow, subOrderModel: SubOrder, orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {
    placeBseSuborders(orderModel, order, subOrder, subOrderModel, orderStateChangeMap, userLoginObject)
  }

  /**
    *
    * @param orderModel
    * @param order
    * @param subOrder
    * @param subOrderModel
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  def placeBseSuborders(orderModel: OrderModel, order: FcomtRow, subOrder: FcsotRow, subOrderModel: SubOrder, orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {
    //var mandateTuple: Option[(String, String)] = None

    var ftr: Future[Option[(String, String)]] = Future[Option[(String, String)]](None)

    if (subOrder.sotinvestmentmode == SIP_INVESTMENT_MODE) {
      if (subOrderModel.existingMmtRfnum.isEmpty) {
        ftr = processMandate(orderModel.buaRfnum, subOrder, userLoginObject)
      } else {
        ftr = bankService.getMandateId(subOrderModel.existingMmtRfnum.get)
      }

      ftr.flatMap(mandateTuple => {
        processSipOrder(subOrder, order, subOrderModel, orderModel, mandateTuple, orderStateChangeMap, userLoginObject)
      })
    }
    else {
      placeBSELumpSumOrder(subOrder, order, subOrderModel, orderModel, orderStateChangeMap, userLoginObject)
    }



  }

  /**
    *
    * @param subOrder
    * @param order
    * @param subOrderModel
    * @param orderModel
    * @param mandateTuple
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  def processSipOrder(subOrder: FcsotRow, order: FcomtRow, subOrderModel: SubOrder, orderModel: OrderModel,
                      mandateTuple: Option[(String, String)], orderStateChangeMap: mutable.HashMap[Long, List[Long]],
                      userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {

    if (mandateTuple.isEmpty) {
      Future {
        val buySellTypeName = orderHelper.getBuySellTypeName(order.omtbuysell, subOrder.sotbuyselltype)
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_FAILURE, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode, order.omtbuysell, buySellTypeName)
      }
    } else {
      val subOrderAmount = subOrder.sotorderamount
      bankService.populateMandateUsageLog(subOrder.id, mandateTuple.get._1, subOrderAmount, userLoginObject.username.get).flatMap(data => {
        placeSipOrder(subOrder, order, subOrderModel, orderModel, mandateTuple, orderStateChangeMap, userLoginObject)
      })
    }

  }

  /**
    *
    * @param subOrder
    * @param order
    * @param subOrderModel
    * @param orderModel
    * @param mandateTuple
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  def placeSipOrder(subOrder: FcsotRow, order: FcomtRow, subOrderModel: SubOrder, orderModel: OrderModel,
                    mandateTuple: Option[(String, String)], orderStateChangeMap: mutable.HashMap[Long, List[Long]],
                    userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {

    val userName = userLoginObject.username.get
    val userId = userLoginObject.userid.get
    val clientCode = userLoginObject.userid.get.toString
    val subOrderAmount = subOrder.sotorderamount
    val bseSchemeCode = subOrderModel.bseSchemeCode.get
    val folioNo = subOrderModel.folioNo
    val dpTxnMode = DPTxnEnum.withName(orderModel.cdslNdslPhysicalTxnMode.getOrElse(PHYSICAL_MODE))
    val transactionMode = TransactionModeEnum.withName(subOrderModel.dematPhysicalMode.getOrElse(PHYSICAL_MODE))
    val internalRefNo = order.id + "-" + subOrder.id
    val uniqueRefNo = subOrder.id + DateTimeUtils.convertDateToFormat(new Date(), BSE_UNIQUE_REF_NO_DATE_FORMAT)

    var fcXsipOrderEntry = FCXsipOrderEntryModel(uniqueRefNo, TransactionCodeEnum.NEW, bseSchemeCode, clientCode,
      DateTimeUtils.getEstimatedSIPDate(subOrder.sotsipdayofmonth.get),
      OrderConstants.FREQUENCY_MAP.getOrElse(subOrder.sotsipfrequency.get, ""), transactionMode, dpTxnMode, Some(internalRefNo),
      OrderConstants.ROLLING_FREQUENCY, subOrderAmount, subOrder.sotsipinstallments.get, None, None,
      FirstOrderEnum.Y, None, orderModel.ipAddress, folioNo)

    if (mandateTuple.get._2 == PHYSICAL_MANDATE_VALUE) {
      fcXsipOrderEntry = fcXsipOrderEntry.copy(mandateId = Some(mandateTuple.get._1.toLong))
    } else {
      fcXsipOrderEntry = fcXsipOrderEntry.copy(isipMandateId = Some(mandateTuple.get._1))
    }

    bseStarOrderEntryService.getXsipOrderEntryParamResponse(fcXsipOrderEntry, subOrder.id.toString, userName).map(bseXsipOrderValidateWrapper => {
      val errorList = bseXsipOrderValidateWrapper.errorList
      val bseOrderId = bseXsipOrderValidateWrapper.bseXsipOrderEntryParamResponse.xsipRegId
      val buySellTypeName = orderHelper.getBuySellTypeName(order.omtbuysell, subOrder.sotbuyselltype)
      if (!errorList.get.isEmpty) {
        logger.debug("Sip Order Not Placed in BSE")
        updateSubOrderState(subOrder, order, ORDER_FAILED, orderStateChangeMap, ORDER_STATUS_FAILURE, userLoginObject)
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_FAILURE, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode, order.omtbuysell, buySellTypeName)
      } else {
        logger.debug("Sip Order Placed successfully in BSE")
        orderRepository.updateSipRegNo(subOrder.id, order.id, subOrder.sotostmstaterfnum, bseOrderId, userName).map(updated => {
          updateSubOrderState(subOrder, order, PLACED_AT_EXCHANGE, orderStateChangeMap, ORDER_STATUS_SUCCESS, userLoginObject)
        })
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_SUCCESS, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode, order.omtbuysell, buySellTypeName)
      }
    })

  }

  /**
    *
    * @param subOrder
    * @param order
    * @param subOrderModel
    * @param orderModel
    * @param orderStateChangeMap
    * @param userLoginObject
    * @return
    */
  def placeBSELumpSumOrder(subOrder: FcsotRow, order: FcomtRow, subOrderModel: SubOrder, orderModel: OrderModel,
                           orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {

    val userName = userLoginObject.username.get
    val userId = userLoginObject.userid.get
    val clientCode = userLoginObject.userid.get.toString
    val bseSchemeCode = subOrderModel.bseSchemeCode.get
    val folioNo = subOrderModel.folioNo

    val orderAmt: Option[BigDecimal] = if (subOrder.sotorderamount >= 0) Some(subOrder.sotorderamount) else None
    val allredeem = if (subOrderModel.allRedeem.nonEmpty) {
      if (subOrderModel.allRedeem.get) AllRedeemEnum.Y else AllRedeemEnum.N
    } else AllRedeemEnum.N
    val orderQuantity: Option[BigDecimal] = if (allredeem != AllRedeemEnum.Y && subOrder.sotorderquantity.nonEmpty) Some(subOrder.sotorderquantity.get) else None
    val internalRefNo = order.id + "-" + subOrder.id
    val dpTransaction = DPTxnEnum.withName(orderModel.cdslNdslPhysicalTxnMode.getOrElse(PHYSICAL_MODE))
    val uniqueRefNo = subOrder.id + DateTimeUtils.convertDateToFormat(new Date(), BSE_UNIQUE_REF_NO_DATE_FORMAT)

    val fcOrderEntry = FCOrderEntryModel(uniqueRefNo, TransactionCodeEnum.NEW, None, bseSchemeCode, clientCode,
      orderModel.orderType, OrderConstants.BUY_SELL_TYPEMAP.getOrElse(subOrder.sotbuyselltype, ""), dpTransaction, allredeem,
      Some(internalRefNo), orderAmt, orderQuantity, orderModel.ipAddress, folioNo)

    bseStarOrderEntryService.getOrderEntryParamResponse(fcOrderEntry, subOrder.id.toString, userName).flatMap(bseOrderValidateWrapper => {
      val buySellTypeName = orderHelper.getBuySellTypeName(order.omtbuysell, subOrder.sotbuyselltype)
      val errorList = bseOrderValidateWrapper.errorList
      //@sumit - adding it because order number not received from bse
      if (!errorList.get.isEmpty || bseOrderValidateWrapper.bseOrderEntryParamResponse.orderNumber.isEmpty) {
        logger.debug("LumpSum Order Not Placed in BSE")
        updateSubOrderState(subOrder, order, ORDER_FAILED, orderStateChangeMap, ORDER_STATUS_FAILURE, userLoginObject).map(orderState =>{
          ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_FAILURE, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode, order.omtbuysell, buySellTypeName)
        })
      } else {
        val bseOrderId = bseOrderValidateWrapper.bseOrderEntryParamResponse.orderNumber.get.toString
        logger.debug("LumpSum Order Placed successfully in BSE")
        orderRepository.updateSubOrderTxnId(subOrder.id, order.id, subOrder.sotostmstaterfnum, bseOrderId, userName).flatMap(updated => {
          updateSubOrderState(subOrder, order, PLACED_AT_EXCHANGE, orderStateChangeMap, ORDER_STATUS_SUCCESS, userLoginObject).map(orderState =>{
            ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_SUCCESS, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode, order.omtbuysell, buySellTypeName)
          })
        })
      }
    })

  }

  /**
    *
    * @param buaRfnum
    * @param subOrder
    * @param userLoginObject
    * @return
    */
  def processMandate(buaRfnum: Option[Long], subOrder: FcsotRow, userLoginObject: UserLoginObject): Future[Option[(String, String)]] = {

    val userPk = userLoginObject.userid.get

    schemeService.getSchemeOptionAmcDetails(subOrder.sotsoptrfnum).flatMap(amctRow => {
      aMCService.isISIPAllowed(amctRow.id).flatMap(isAmcIsipAllowed => {
        if (!buaRfnum.isEmpty) {
          bankRepository.getSipBankDetails(buaRfnum.get).flatMap(mandateTuple => {
            val mandateAmount = subOrder.sotorderamount
            bankRepository.getExistingBankMandate(buaRfnum.get, mandateAmount).flatMap(existingMandate => {
              if (existingMandate.isEmpty) {
                generateMandateId(mandateTuple, isAmcIsipAllowed, subOrder, userLoginObject)
              } else {
                checkManadateAmountForTodayOrder(existingMandate, mandateTuple, isAmcIsipAllowed, subOrder, userLoginObject)
              }
            })
          })
        } else {
          bankRepository.getMandateBankDetails(userPk).flatMap(mandateTuple => {
            val bankUserId = mandateTuple._1
            val accountNo = mandateTuple._2
            val ifscCode = mandateTuple._3
            generateMandateId((bankUserId, accountNo, ifscCode, N_FLAG), isAmcIsipAllowed, subOrder, userLoginObject)
          })
        }
      })
    })

  }

  /**
    *
    * @param existingMandate
    * @param mandateParamTuple
    * @param isAmcIsipAllowed
    * @param subOrder
    * @param userLoginObject
    * @return
    */
  def checkManadateAmountForTodayOrder(existingMandate: Option[(String, String)], mandateParamTuple: (Long, String, String, String),
                                       isAmcIsipAllowed: Boolean, subOrder: FcsotRow, userLoginObject: UserLoginObject): Future[Option[(String, String)]] = {

    val mandateAmount = subOrder.sotorderamount

    bankService.validateForMandateAmount(existingMandate.get._1, mandateAmount).flatMap(isValidMandate => {
      if (isValidMandate) {
        Future.apply(existingMandate)
      } else {
        generateMandateId(mandateParamTuple, isAmcIsipAllowed, subOrder, userLoginObject)
      }
    })


  }

  /**
    *
    * @param mandateParamTuple
    * @param isAmcIsipAllowed
    * @param subOrder
    * @param userLoginObject
    * @return
    */
  def generateMandateId(mandateParamTuple: (Long, String, String, String), isAmcIsipAllowed: Boolean, subOrder: FcsotRow, userLoginObject: UserLoginObject): Future[Option[(String, String)]] = {

    val clientCode = userLoginObject.userid.get.toString
    val userPk = userLoginObject.userid.get
    val userName = userLoginObject.username.get
    val bankUserId = mandateParamTuple._1
    val accountNo = mandateParamTuple._2
    val ifscCode = mandateParamTuple._3
    val mandateType = if (mandateParamTuple._4 == Y_FLAG && isAmcIsipAllowed) ISIP_MANDATE else PHYSICAL_MANDATE
    val installmentAmount = subOrder.sotorderamount
    val mandateAmount = if (installmentAmount >= 50000) installmentAmount else 50000
    val mandateRegisterModel = XsipMandateRegisterModel(clientCode, mandateAmount, ifscCode, accountNo, MandateTypeEnum.withName(mandateType))

    bseUploadService.registerMandate(mandateRegisterModel, subOrder.id.toString, userName).flatMap(bseUploadMfApiResponseValidateWrapper => {

      val bseMandateErrorList = bseUploadMfApiResponseValidateWrapper.errorList

      if (!bseMandateErrorList.get.isEmpty) {
        logger.debug("Mandate Registration Failed in BSE")
        Future.apply(None)
      } else {
        logger.debug("Mandate Successfully Registered in BSE")
        val mandate = bseUploadMfApiResponseValidateWrapper.bseUploadMfApiResponse.referenceNumber
        val mandateModel = MandateModel(bankUserId, mandateAmount, mandateType, CREATE_MANDATE, mandate.get, DateTimeUtils.getCurrentTimeStamp,
          MANDATE_AS_AND_WHEN_PRESENTED, MAXIMUM_DEBIT_TYPE_MANDATE, Y_FLAG, BSE_DEDUCTEE_NAME, subOrder.id, mandateAmount)

        bankService.saveMandate(mandateModel, userName).map(data => {
          var mandateTypeValue = ISIP_MANDATE_VALUE
          if (mandateType == PHYSICAL_MANDATE) {
            mandateTypeValue = PHYSICAL_MANDATE_VALUE
          }
          Some(mandate.get, mandateTypeValue)
        })
      }
    })
  }

  /**
    *
    * @return
    */
  def getBSEBankDetails(): Future[JsValue] = {

    integrationRepository.getBSEBankAccountDetails.map(bankDetails => {
      Json.parse(bankDetails.actconstantvalue)
    })
  }
}
