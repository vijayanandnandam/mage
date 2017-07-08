package service.integration

import javax.inject.Inject

import constants.{DBConstants, IntegrationConstants, OrderConstants}
import data.model.Tables.{FcomtRow, FcsotRow}
import models.integration.enumerations._
import models._
import org.slf4j.LoggerFactory
import repository.module.{BankRepository, OrderRepository}
import service.{AMCService, BankService, SchemeService}
import utils.DateTimeUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by fincash on 19-04-2017.
  */
class BSEIntegrationServiceImpl @Inject()(implicit ec: ExecutionContext,
                                          bseStarOrderEntryService: BSEStarOrderEntryServiceImpl,
                                          bseUploadService: BSEStarUploadServiceImpl,
                                          orderRepository: OrderRepository,
                                          schemeService: SchemeService, aMCService: AMCService,
                                          bankRepository: BankRepository, bankService: BankService) extends IntegrationService with DBConstants with IntegrationConstants with OrderConstants {

  val logger, log = LoggerFactory.getLogger(classOf[BSEIntegrationServiceImpl])

  override def placeSubOrders(orderModel: OrderModel, order: FcomtRow, subOrderList: List[FcsotRow], userLoginObject: UserLoginObject): Future[List[ProcessedSubOrderModel]] = {
    placeBseSuborders(orderModel, order, subOrderList, userLoginObject)
  }

  def placeBseSuborders(orderModel: OrderModel, order: FcomtRow, subOrderList: List[FcsotRow], userLoginObject: UserLoginObject): Future[List[ProcessedSubOrderModel]] = {
    var subOrderIdx = -1
    var mandateTuple: Option[(String, String)] = None

    for (subOrder <- subOrderList) {
      subOrderIdx = subOrderIdx + 1

      var ftr: Future[Option[(String, String)]] = Future[Option[(String, String)]](None)

      val subOrderModel = orderModel.subOrders(subOrderIdx)
      if (subOrder.sotinvestmentmode == SIP_INVESTMENT_MODE) {
        if (subOrderModel.existingMmtRfnum.isEmpty) {
          ftr = processMandate(orderModel.buaRfnum, subOrder, userLoginObject)
        } else {
          ftr = bankService.getMandateId(subOrderModel.existingMmtRfnum.get)
        }

        Await.result(ftr.map(mId => mandateTuple = mId), Duration.Inf)
        logger.debug("Mandate id is: " + mandateTuple.getOrElse(""))
      }
    }

    subOrderIdx = -1

    val futureList = for (subOrder <- subOrderList) yield {
      subOrderIdx = subOrderIdx + 1
      val subOrderModel = orderModel.subOrders(subOrderIdx)
      if (subOrder.sotinvestmentmode == SIP_INVESTMENT_MODE) {
        processSipOrder(subOrder, order, subOrderModel, orderModel, mandateTuple, userLoginObject)
      }
      else {
        placeBSELumpSumOrder(subOrder, order, subOrderModel, orderModel, userLoginObject)
      }
    }

    Future.sequence(futureList)

  }

  def processSipOrder(subOrder: FcsotRow, order: FcomtRow, subOrderModel: SubOrder, orderModel: OrderModel,
                      mandateTuple: Option[(String, String)], userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {

    if (mandateTuple.isEmpty) {
      Future {
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_FAILURE, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode)
      }
    } else {
      val subOrderAmount = subOrder.sotorderamount
      bankService.populateMandateUsageLog(subOrder.id, mandateTuple.get._1, subOrderAmount, userLoginObject.username.get).flatMap(data => {
        placeSipOrder(subOrder, order, subOrderModel, orderModel, mandateTuple, userLoginObject)
      })
    }

  }

  def placeSipOrder(subOrder: FcsotRow, order: FcomtRow, subOrderModel: SubOrder, orderModel: OrderModel,
                    mandateTuple: Option[(String, String)], userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {

    val userName = userLoginObject.username.get
    val clientCode = userLoginObject.userid.get.toString
    val subOrderAmount = subOrder.sotorderamount
    val bseSchemeCode = subOrderModel.bseSchemeCode.get
    val folioNo = subOrderModel.folioNo
    val dpTxnMode = DPTxnEnum.withName(orderModel.cdslNdslPhysicalTxnMode.getOrElse(PHYSICAL_MODE))
    val transactionMode = TransactionModeEnum.withName(subOrderModel.dematPhysicalMode.getOrElse(PHYSICAL_MODE))

    var fcXsipOrderEntry = FCXsipOrderEntryModel(subOrder.id.toString, TransactionCodeEnum.NEW, bseSchemeCode, clientCode,
      DateTimeUtils.getEstimatedSIPDate(subOrder.sotsipdayofmonth.get),
      OrderConstants.FREQUENCY_MAP.getOrElse(subOrder.sotsipfrequency.get, ""), transactionMode, dpTxnMode, Some(order.id.toString),
      OrderConstants.ROLLING_FREQUENCY, subOrderAmount, subOrder.sotsipinstallments.get, None, None,
      FirstOrderEnum.Y, None, orderModel.ipAddress, folioNo)

    if (mandateTuple.get._2 == PHYSICAL_MANDATE_VALUE) {
      fcXsipOrderEntry = fcXsipOrderEntry.copy(mandateId = Some(mandateTuple.get._1.toLong))
    } else {
      fcXsipOrderEntry = fcXsipOrderEntry.copy(isipMandateId = Some(mandateTuple.get._1))
    }

    bseStarOrderEntryService.getXsipOrderEntryParamResponse(fcXsipOrderEntry, userName).map(bseXsipOrderValidateWrapper => {
      val errorList = bseXsipOrderValidateWrapper.errorList
      val bseOrderId = bseXsipOrderValidateWrapper.bseXsipOrderEntryParamResponse.xsipRegId

      if (!errorList.get.isEmpty) {
        logger.debug("Sip Order Not Placed in BSE")
        orderRepository.setCancelStateForSubOrder(subOrder.id, order.id, subOrder.sotostmstaterfnum, userName)
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_FAILURE, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode)
      } else {
        logger.debug("Sip Order Placed successfully in BSE")
        orderRepository.updateSipSubOrder(subOrder.id, order.id, subOrder.sotostmstaterfnum, bseOrderId, userName)
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_SUCCESS, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode)
      }
    })

  }

  def placeBSELumpSumOrder(subOrder: FcsotRow, order: FcomtRow, subOrderModel: SubOrder, orderModel: OrderModel, userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {

    val userName = userLoginObject.username.get
    val clientCode = userLoginObject.userid.get.toString
    val bseSchemeCode = subOrderModel.bseSchemeCode.get
    val folioNo = subOrderModel.folioNo
    val dpTransaction = DPTxnEnum.withName(orderModel.cdslNdslPhysicalTxnMode.getOrElse(PHYSICAL_MODE))

    val fcOrderEntry = FCOrderEntryModel(subOrder.id.toString, TransactionCodeEnum.NEW, None, bseSchemeCode, clientCode,
      orderModel.orderType, OrderConstants.BUY_SELL_TYPEMAP.getOrElse(subOrder.sotbuyselltype, ""), dpTransaction,
      Some(order.id.toString), Some(subOrder.sotorderamount), None, orderModel.ipAddress, folioNo)

    bseStarOrderEntryService.getOrderEntryParamResponse(fcOrderEntry, userName).map(bseOrderValidateWrapper => {
      val bseOrderId = bseOrderValidateWrapper.bseOrderEntryParamResponse.orderNumber.get.toString

      val errorList = bseOrderValidateWrapper.errorList
      if (!errorList.get.isEmpty) {
        logger.debug("LumpSum Order Not Placed in BSE")
        orderRepository.setCancelStateForSubOrder(subOrder.id, order.id, subOrder.sotostmstaterfnum, userName)
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_FAILURE, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode)
      } else {
        logger.debug("LumpSum Order Placed successfully in BSE")
        orderRepository.updateTransactionIdForSubOrder(subOrder.id, order.id, subOrder.sotostmstaterfnum, bseOrderId, userName)
        ProcessedSubOrderModel(subOrder.id, ORDER_STATUS_SUCCESS, subOrder.sotsoptrfnum, subOrder.sotinvestmentmode)
      }
    })

  }

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
        logger.debug("Mandate Successfully Registration in BSE")
        val mandate = bseUploadMfApiResponseValidateWrapper.bseUploadMfApiResponse.referenceNumber
        val mandateModel = MandateModel(bankUserId, mandateAmount, mandateType, CREATE_MANDATE, mandate.get, DateTimeUtils.getCurrentTimeStamp,
          MANDATE_AS_AND_WHEN_PRESENTED, MAXIMUM_DEBIT_TYPE_MANDATE, Y_FLAG, BSE_DEDUCTEE_NAME, subOrder.id, mandateAmount)

        bankService.saveMandate(mandateModel, userName).map(data => {
          Some(mandate.get, mandateType)
        })
      }
    })
  }
}
