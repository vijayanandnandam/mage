package service

import javax.inject.Inject

import constants._
import data.model.Tables.{FcomtRow, FcsmtRow, FcsotRow}
import helpers.{OrderHelper, SchemeHelper}
import models.{PaymentStatus, _}
import org.slf4j.LoggerFactory
import repository.module.{BankRepository, OrderRepository, PasswordRepository, SchemeRepository}
import service.integration.{BSEStarUploadServiceImpl, IntegrationServiceImpl}
import utils.DateTimeUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 15-02-2017.
  */
class OrderService @Inject()(implicit ec: ExecutionContext, orderRepository: OrderRepository,
                             passwordRepository: PasswordRepository,
                             orderCancelService: OrderCancelService,
                             bseUploadService: BSEStarUploadServiceImpl, orderHelper: OrderHelper,
                             bankRepository: BankRepository,
                             integrationServiceImpl: IntegrationServiceImpl,
                             schemeRepository: SchemeRepository, schemeHelper: SchemeHelper) extends DBConstants with IntegrationConstants with OrderConstants {

  val logger, log = LoggerFactory.getLogger(classOf[OrderService])

  def placeNewOrder(orderModel: OrderModel, userLoginObject: UserLoginObject): Future[ProcessedOrderModel] = {

    orderHelper.filterBSESchemes(orderModel).flatMap(modifiedSubOrderList => {

      var modifiedOrderModel = orderModel.copy(subOrders = modifiedSubOrderList)
      orderRepository.placeNewOrder(modifiedOrderModel, userLoginObject).flatMap(orderSuborderListTuple => {

        val order = orderSuborderListTuple._1
        val subOrderList = orderSuborderListTuple._2
        val subOrderObjList = orderSuborderListTuple._3
        modifiedOrderModel = orderModel.copy(subOrders = subOrderObjList)
        passwordRepository.getBSEPassword(BSE_ORDER_API_PASS_CODE).flatMap(passRow => {

          val expiryTime = passRow._2
          val isValid = DateTimeUtils.checkBsePassTimeValidity(expiryTime)

          if (isValid) {
            placeSubOrders(modifiedOrderModel, order, subOrderList, userLoginObject)
          } else {
            passwordRepository.updateBSEPassword().flatMap(_ => {
              placeSubOrders(modifiedOrderModel, order, subOrderList, userLoginObject)
            })
          }
        })
      })
    })
  }

  def validateAlreadyInBSE(orderModel: OrderModel, userLoginObject: UserLoginObject): Future[List[Boolean]] = {
    val userId = userLoginObject.userid.get
    orderHelper.isAlreadyPlacedInBSE(orderModel, userId)
  }

  def placeSubOrders(modifiedOrderModel: OrderModel, order: FcomtRow, subOrderList: List[FcsotRow], userLoginObject: UserLoginObject): Future[ProcessedOrderModel] = {

    integrationServiceImpl.placeSubOrders(modifiedOrderModel, order, subOrderList, userLoginObject).map(processedSubOrdersList => {
      updateOrderState(order.id, order.omtostmstaterfnum, subOrderList, userLoginObject)
      ProcessedOrderModel(order.id, processedSubOrdersList)
    })
  }


  def cancelOrder(cancelSubOrder: CancelSubOrder, userLoginObject: UserLoginObject): Future[Boolean] = {

    orderCancelService.cancelOrder(cancelSubOrder, userLoginObject).map(cancelled => {
      cancelled
    })
  }


  def generatePaymentGatewayLink(orderId: Long, paymentReturnUrl: String, userLoginObject: UserLoginObject): Future[String] = {

    val userName = userLoginObject.username.get
    val clientCode = userLoginObject.userid.get.toString
    bseUploadService.getPaymentGatewayResponse(clientCode, paymentReturnUrl, orderId.toString, userName).map(bseMfApiResponse => {

      val bsePaymentGatewayErrorList = bseMfApiResponse.errorList
      if (!bsePaymentGatewayErrorList.get.isEmpty) {
        logger.debug("Error Occured while generating payment link")
        ""
      } else {
        logger.debug("Payment link generated")
        bseMfApiResponse.bseUploadMfApiResponse.response
      }
    })
  }

  def prepareOrderDetails(omtrfnum: Long, userLoginObject: UserLoginObject): Future[OrderModel] = {

    orderRepository.getOrderDetails(omtrfnum).flatMap(orderTupleSeq => {

      val subOrderFutureList = for (orderTuple <- orderTupleSeq) yield {

        val subOrderObj = orderTuple._2
        schemeRepository.getSchemeOptionById(subOrderObj.sotsoptrfnum).flatMap(soptRow => {

          val smtrfnum = soptRow.soptsmtrfnum
          for {
            paymentStatus <- getPaymentStatus(subOrderObj, userLoginObject)
            smtAmctTuple <- schemeRepository.getSchemeWithAmcDetails(smtrfnum)
            subOrderStateObj <- schemeRepository.getOrderStateName(subOrderObj.sotostmstaterfnum)
          } yield {

            val smtRow = smtAmctTuple.head._1
            val amcRow = smtAmctTuple.head._2
            var subOrderAmount = subOrderObj.sotorderamount
            val schemePlan = schemeHelper.getSchemeOption(soptRow.soptschemeplan, soptRow.soptdividendfrqn)
            val schemeOption = schemeHelper.getDivOption(soptRow.soptdivioptiontype)
            val schemeDetails = SchemeDetails(subOrderObj.sotsoptrfnum, smtRow.smtstdname,
              smtRow.smtdisplayname, amcRow.amctlegalname, amcRow.amctdisplayname.getOrElse(""), schemePlan, schemeOption)

            var subOrderState: Option[String] = None
            if (!subOrderStateObj.isEmpty) {
              subOrderState = Some(subOrderStateObj.get.ostmdisplayname)
            }

            SubOrder(subOrderObj.sotsequence.toLong, subOrderAmount, subOrderObj.sotinvestmentmode, subOrderObj.sotsoptrfnum,
              None, subOrderObj.sotpaymentmode,
              subOrderObj.sotsipinstallments, subOrderObj.sotsipfrequency, None, None,
              subOrderObj.sotsipdayofmonth,
              subOrderObj.sotorderquantity, Some(subOrderObj.sottype), Some(subOrderObj.sottranscnmode),
              Some(subOrderObj.sotbuyselltype),
              None, subOrderState, Some(paymentStatus))
          }
        })
      }
      Future.sequence(subOrderFutureList).flatMap(subOrderSeq => {
        val orderObj = orderTupleSeq(0)._1
        schemeRepository.getOrderStateName(orderObj.omtostmstaterfnum).map(orderStateRow => {
          var orderState: Option[String] = None
          if (!orderStateRow.isEmpty) {
            orderState = Some(orderStateRow.get.ostmdisplayname)
          }
          OrderModel(orderObj.omtbuysell, orderObj.omttotalamount, orderObj.omtipadd, subOrderSeq.toList,
            orderObj.omtorderdevice, None, orderObj.omtsnapshotpath, Some(orderObj.omtdptranscn), None, orderState)
        })
      })
    })
  }


  def getOrderAcknowledgeDetails(omtrfnum: Long, userLoginObject: UserLoginObject): Future[ProcessedOrderModel] = {

    orderRepository.getOrderDetails(omtrfnum).flatMap(orderTupleSeq => {

      val orderObj = orderTupleSeq.head._1
      val subOrderFutureList = for (orderTuple <- orderTupleSeq) yield {

        val subOrderObj = orderTuple._2
        schemeRepository.getSchemeOptionById(subOrderObj.sotsoptrfnum).flatMap(soptRow => {

          val smtrfnum = soptRow.soptsmtrfnum
          for {
            smtRow <- schemeRepository.getSchemeById(smtrfnum)
          } yield {

            var subOrderAmount = subOrderObj.sotorderamount
            val schemePlan = schemeHelper.getSchemeOption(soptRow.soptschemeplan, soptRow.soptdividendfrqn)
            val schemeOption = schemeHelper.getDivOption(soptRow.soptdivioptiontype)
            var orderProcessed = ORDER_STATUS_SUCCESS
            if (subOrderObj.sotostmstaterfnum != PLACED_AT_EXCHANGE) {
              orderProcessed = ORDER_STATUS_FAILURE
            }
            ProcessedSubOrderModel(subOrderObj.id, orderProcessed, subOrderObj.sotsoptrfnum, subOrderObj.sotinvestmentmode,
              schemeDisplayName = Some(smtRow.smtdisplayname), schemePlan = Some(schemePlan), schemeOption = Some(schemeOption), amount = Some(subOrderAmount),
              sipNoOfInstallments = subOrderObj.sotsipinstallments, sipFrequency = subOrderObj.sotsipfrequency, sipDayOfMonth = subOrderObj.sotsipdayofmonth,
              quantity = subOrderObj.sotorderquantity)
          }
        })
      }
      Future.sequence(subOrderFutureList).map(subOrderSeq => {
        val currentTime = DateTimeUtils.convertSqlTimestampToString(orderObj.modifydate)
        ProcessedOrderModel(omtrfnum, subOrderSeq.toList, orderTime = Some(currentTime))
      })
    })
  }

  def populateOrderDetails(omtrfnum: Long, userLoginObject: UserLoginObject): Future[OrderDetails] = {

    val userPk: Long = userLoginObject.userid.getOrElse(0)
    val userName: String = userLoginObject.username.getOrElse("")

    orderRepository.getOrderDetails(omtrfnum).flatMap(orderTupleSeq => {
      val orderObj = orderTupleSeq.head._1
      orderRepository.getStateDetails(orderObj.omtostmstaterfnum).flatMap(ostmRow => {

        val subOrderFutureList = for (orderTuple <- orderTupleSeq) yield {

          val sotRow = orderTuple._2
          schemeRepository.getSchemeOptionById(sotRow.sotsoptrfnum).flatMap(soptRow => {

            val smtrfnum = soptRow.soptsmtrfnum
            for {
              smtRow <- schemeRepository.getSchemeById(smtrfnum)
              mmtRow <- bankRepository.getSubOrderMandateDetails(sotRow.id)
              oshtRowSeq <- orderRepository.getSubOrderHistoryDetails(sotRow.id)
              subOrderState <- orderRepository.getSubOrderState(sotRow.id, userName)
            } yield {

              var subOrderAmount = sotRow.sotorderamount
              val schemePlan = schemeHelper.getSchemeOption(soptRow.soptschemeplan, soptRow.soptdividendfrqn)
              val schemeOption = schemeHelper.getDivOption(soptRow.soptdivioptiontype)

              var orderProcessed = ORDER_STATUS_SUCCESS
              if (sotRow.sotostmstaterfnum != PLACED_AT_EXCHANGE) {
                orderProcessed = ORDER_STATUS_FAILURE
              }
              var sipFrequency: Option[String] = None
              if (!sotRow.sotsipfrequency.isEmpty) {
                sipFrequency = Some(OrderConstants.FREQUENCY_MAP.getOrElse(sotRow.sotsipfrequency.get, ""))
              }
              val investmentMode = InvestmentConstants.INVESTMENT_MODE_MAP.getOrElse(sotRow.sotinvestmentmode, "")
              val createDate = DateTimeUtils.convertSqlTimestampToString(sotRow.createdate.get)
              var subOrderDetail = SubOrderDetails(sotRow.id, orderProcessed, investmentMode, createDate,
                schemeName = Some(smtRow.smtdisplayname), schemePlan = Some(schemePlan), schemeOption = Some(schemeOption), amount = Some(subOrderAmount),
                sipNoOfInstallments = sotRow.sotsipinstallments, sipFrequency = sipFrequency, sipDayOfMonth = sotRow.sotsipdayofmonth,
                quantity = sotRow.sotorderquantity, subOrderHistoryList = Some(oshtRowSeq.toList), stateName = Some(subOrderState))
              if (!mmtRow.isEmpty) {
                val mmtRowDetails = mmtRow.head
                val mandateDetails = SubOrderMandateDetails(mmtRowDetails.id, mmtRowDetails.mmtexternalid.get, mmtRowDetails.mmtmandatetype.get)
                subOrderDetail = subOrderDetail.copy(mandateDetails = Some(mandateDetails))
              }
              subOrderDetail
            }
          })
        }
        Future.sequence(subOrderFutureList).flatMap(subOrderSeq => {
          orderRepository.getOrderHistoryDetails(omtrfnum).map(orderHistoryList => {
            val createDate = DateTimeUtils.convertSqlTimestampToString(orderObj.createdate.get)
            val orderState = ostmRow.get.ostmdisplayname
            OrderDetails(omtrfnum, orderObj.omtbuysell, subOrderSeq.toList, createDate, orderObj.omttotalamount, snapshotPath = orderObj.omtsnapshotpath,
              orderHistoryList = Some(orderHistoryList.toList), stateName = Some(orderState))
          })
        })
      })
    })

  }

  def updateOrderState(sotrfnum: Long, orderCurrentState: Long, subOrderList: List[FcsotRow], userLoginObject: UserLoginObject): Future[String] = {

    val userName = userLoginObject.username.getOrElse("")
    orderRepository.getCancelledSubOrders(sotrfnum).flatMap(cancelledSubOrders => {
      if (cancelledSubOrders.isEmpty) {
        orderRepository.updateOrderState(sotrfnum, orderCurrentState, MAIN_ORDER_IN_PROCESS, userName)
      } else {
        if (cancelledSubOrders.size == subOrderList.size) {
          orderRepository.updateOrderState(sotrfnum, orderCurrentState, ORDER_CANCELLED, userName)
        } else {
          orderRepository.updateOrderState(sotrfnum, orderCurrentState, MAIN_ORDER_IN_PROCESS, userName)
        }
      }
    })
  }

  def updateOrderIntermediateGatewayState(sotrfnum: Long, userLoginObject: UserLoginObject): Future[Boolean] = {

    val userPk: Long = userLoginObject.userid.getOrElse(0)
    val userName = userLoginObject.username.getOrElse("")
    orderRepository.getSubOrderDetails(sotrfnum, userPk).flatMap(subOrderDetailsSeq => {
      val subOrder = subOrderDetailsSeq.head
      if (subOrder.sotostmstaterfnum != ORDER_CANCELLED) {
        orderRepository.updateSubOrderState(sotrfnum, subOrder.sotomtrfnum, subOrder.sotostmstaterfnum, PLACED_AT_EXCHANGE_PG, userName).map(orderState => {
          true
        })
      } else {
        Future {
          false
        }
      }
    })
  }

  def checkOrderPaymentStatus(sotrfnum: Long, userLoginObject: UserLoginObject): Future[PaymentStatus] = {

    val userPk: Long = userLoginObject.userid.getOrElse(0)
    val userName = userLoginObject.username.getOrElse("")
    val clientCode = userLoginObject.userid.get.toString

    orderRepository.getSubOrderDetails(sotrfnum, userPk).flatMap(subOrderDetailsSeq => {

      val subOrder = subOrderDetailsSeq.head

      if (subOrder.sotostmstaterfnum == ORDER_CANCELLED) {
        orderRepository.getStateDetails(ORDER_CANCELLED).map(stateDetail => {
          PaymentStatus(ORDER_CANCELLED, stateDetail.get.ostmdisplayname)
        })
      } else {
        if (subOrder.sotinvestmentmode == SIP_INVESTMENT_MODE) {
          orderRepository.getStateDetails(PLACED_AT_EXCHANGE).map(stateDetail => {
            PaymentStatus(PLACED_AT_EXCHANGE, stateDetail.get.ostmdisplayname)
          })
        } else {
          val transactionId = subOrder.sottrnsctionid1.get
          val clientOrderPaymentStatus = ClientOrderPaymentStatus(clientCode, transactionId, BSE_PAYMENT_MF_SEGMENT)

          bseUploadService.getClientOrderPaymentStatus(clientOrderPaymentStatus, sotrfnum.toString, userName).flatMap(bseMfApiResponse => {

            if (!bseMfApiResponse.errorList.get.isEmpty) {
              orderRepository.getSubOrderState(sotrfnum, userName).map(orderState => {
                PaymentStatus(subOrder.sotostmstaterfnum, orderState)
              })
            } else {
              val paymentStatus = bseMfApiResponse.bseUploadMfApiResponse.response
              var subOrderState = -1L
              if (paymentStatus.indexOf(BSE_AWAITING_FUNDS_CONFIRMATION) != -1) {
                subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_AWAITING_FUNDS_CONFIRMATION, -1)
              } else if (paymentStatus.indexOf(BSE_PAYMENT_NOT_INITIATED) != -1) {
                subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_PAYMENT_NOT_INITIATED, -1)
              } else if (paymentStatus.indexOf(BSE_PAYMENT_APPROVED) != -1) {
                subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_PAYMENT_APPROVED, -1)
              } else {
                subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_PAYMENT_REJECTED, -1)
              }
              orderRepository.updateSubOrderState(sotrfnum, subOrder.sotomtrfnum, subOrder.sotostmstaterfnum, subOrderState, userName).map(orderState => {
                PaymentStatus(subOrderState, orderState)
              })
            }
          })
        }
      }
    })
  }

  def getPaymentStatus(subOrder: FcsotRow, userLoginObject: UserLoginObject): Future[String] = {

    val userName = userLoginObject.username.getOrElse("")
    val clientCode = userLoginObject.userid.get.toString

    if (subOrder.sotinvestmentmode == SIP_INVESTMENT_MODE) {
      Future {
        SIP_IN_PROCESS
      }
    } else {
      val transactionId = subOrder.sottrnsctionid1.get
      val clientOrderPaymentStatus = ClientOrderPaymentStatus(clientCode, transactionId, BSE_PAYMENT_MF_SEGMENT)
      bseUploadService.getClientOrderPaymentStatus(clientOrderPaymentStatus, subOrder.id.toString, userName).map(bseMfApiResponse => {

        val paymentStatus = bseMfApiResponse.bseUploadMfApiResponse.response
        paymentStatus
      })
    }
  }


  def getSchemeDetails(sotrfnum: Long, userPk: Long): Future[FcsmtRow] = {
    orderRepository.getSubOrderDetails(sotrfnum, userPk).flatMap(fcsot => {
      val sotsoptrfnum = fcsot.headOption.get.sotsoptrfnum
      schemeRepository.getSchemeOptionById(sotsoptrfnum).flatMap(soptRow => {
        val smtrfnum = soptRow.soptsmtrfnum
        schemeRepository.getSchemeWithAmcDetails(smtrfnum).map(smtAmctTuple => {
          val smtRow = smtAmctTuple.head._1
          smtRow
        })
      })
    })
  }
}
