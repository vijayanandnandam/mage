package service

import java.util.Calendar
import javax.inject.{Inject, Singleton}

import constants.{DBConstants, IntegrationConstants, OrderConstants}
import data.model.Tables.FcsotRow
import helpers.MarketHelper
import models.ClientOrderPaymentStatus
import models.batch.PaymentStatusReqModel
import org.slf4j.LoggerFactory
import repository.module.OrderRepository
import service.integration.{BSEStarUploadServiceImpl, RelianceIntegrationServiceImpl}
import utils.DateTimeUtils

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 26-04-2017.
  */

@Singleton
class PaymentService @Inject()(implicit ec: ExecutionContext,
                               orderRepository: OrderRepository,
                               bSEStarUploadServiceImpl: BSEStarUploadServiceImpl,
                               relianceIntegrationServiceImpl: RelianceIntegrationServiceImpl,
                               marketHelper: MarketHelper) extends DBConstants with OrderConstants with IntegrationConstants{

  val logger, log = LoggerFactory.getLogger(classOf[PaymentService])

  def getPaymentAllowedSubOrders(userPk: Long): Future[List[FcsotRow]] = {

    orderRepository.getBSECutOff(BSE_PAY_START_CUTOFF_KEY).flatMap(lastOrderCutOffTime => {
      orderRepository.getBSECutOff(BSE_PAY_CUTOFF_KEY).flatMap(lastPayCutOffTime => {

        for {
          equityMarketSubOrders <- getPaymentAllowedSegmentSubOrders(userPk, EQUITY_MARKET_SEGMENT, lastOrderCutOffTime, lastPayCutOffTime)
          debtMarketSubOrders <- getPaymentAllowedSegmentSubOrders(userPk, DEBT_MARKET_SEGMENT, lastOrderCutOffTime, lastPayCutOffTime)
        } yield {
          equityMarketSubOrders ++ debtMarketSubOrders
        }
      })
    })
  }

  def getPaymentAllowedSegmentSubOrders(userPk: Long, marketSegment: String, lastOrderCutOffTime: String, lastPayCutOffTime: String): Future[List[FcsotRow]] = {

    val todayTime = Calendar.getInstance()
    todayTime.set(Calendar.MILLISECOND, 1)
    val currTime = todayTime.clone.asInstanceOf[Calendar]
    currTime.set(Calendar.MILLISECOND, 1)
    marketHelper.getPrevWorkingDay(currTime, marketSegment).flatMap(prevWorkingDay => {
      val prevDay = prevWorkingDay.clone.asInstanceOf[Calendar]
      prevDay.set(Calendar.MILLISECOND, 1)
      marketHelper.getPrevWorkingDay(prevDay, marketSegment).flatMap(prevToPrevWorkingDay => {
        val prevToPrevWorkingDayCutOff = marketHelper.addCuttOffTimeToWorkingDay(prevToPrevWorkingDay, lastOrderCutOffTime)
        val prevWorkingDayCutOff = marketHelper.addCuttOffTimeToWorkingDay(prevWorkingDay, lastOrderCutOffTime)
        val currWorkingTime = todayTime.clone.asInstanceOf[Calendar]
        val currWorkingDayCutOff = marketHelper.addCuttOffTimeToWorkingDay(currWorkingTime, lastPayCutOffTime)
        var prevWorkingDaySqlTime = DateTimeUtils.convertCalendarToSqlTimestamp(prevToPrevWorkingDayCutOff)
        marketHelper.isWorkingHoliday(todayTime, marketSegment).flatMap(isHolidayToday => {
          if (!isHolidayToday && todayTime.getTime.after(currWorkingDayCutOff.getTime)) {
            prevWorkingDaySqlTime = DateTimeUtils.convertCalendarToSqlTimestamp(prevWorkingDayCutOff)
          }
          logger.debug("Last Payment Cut off time = " + prevWorkingDaySqlTime + " Segment = " + marketSegment)
          orderRepository.getSegmentPaymentAllowedSubOrders(userPk, prevWorkingDaySqlTime, marketSegment)
        })
      })
    })
  }

  def getBSEPaymentStatus(payReqModel: PaymentStatusReqModel, userName: String): Future[Long] = {

    orderRepository.getSubOrderFromTxnId1(Some(payReqModel.txnId1.toString)).flatMap(subOrders =>{
      val subOrder = subOrders.head
      if(subOrder.sottranscnmode == RELIANCE_TRANSACTION_MODE){
        val success = relianceIntegrationServiceImpl.getRelianceStatus(payReqModel.txnId1.toString)
        var subOrderState = ORDER_FAILED
        if(success){
          subOrderState = REDEMPTION_APPROVED
        }
        Future.apply(subOrderState)
      } else{
        val uniqueRefNo = if(subOrders.isEmpty) "" else subOrders.head.id.toString
        val clientOrderPaymentStatus: ClientOrderPaymentStatus = ClientOrderPaymentStatus(payReqModel.clientCode,payReqModel.txnId1.toString,BSE_PAYMENT_MF_SEGMENT)
        bSEStarUploadServiceImpl.getClientOrderPaymentStatus(clientOrderPaymentStatus, uniqueRefNo, userName).map(bseMfApiResponse => {
          if (bseMfApiResponse.errorList.get.isEmpty) {
            val paymentStatus = bseMfApiResponse.bseUploadMfApiResponse.response
            var subOrderState = -1L
            if (paymentStatus.indexOf(BSE_AWAITING_FUNDS_CONFIRMATION) != -1) {
              subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_AWAITING_FUNDS_CONFIRMATION, -1L)
            } else if (paymentStatus.indexOf(BSE_PAYMENT_NOT_INITIATED) != -1) {
              subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_PAYMENT_NOT_INITIATED, -1L)
            } else if (paymentStatus.indexOf(BSE_PAYMENT_APPROVED) != -1) {
              subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_PAYMENT_APPROVED, -1L)
            } else {
              subOrderState = OrderConstants.ORDER_STATE_MAP.getOrElse(BSE_PAYMENT_REJECTED, -1L)
            }
            subOrderState

          } else {
            -1L
          }
        })
      }
    })
  }

  def getUniqueRefNo(transactionId: Long):Future[String] = {
    orderRepository.getSubOrderFromTxnId1(Some(transactionId.toString)).map(subOrders =>{
      if(subOrders.isEmpty) "" else subOrders.head.id.toString
    })
  }

  def getSubOrdersMap(subOrderList: List[FcsotRow]): mutable.HashMap[Long, Boolean] = {
    val subOrderMap: mutable.HashMap[Long, Boolean] = new mutable.HashMap[Long, Boolean]()
    for (subOrder <- subOrderList) {
      subOrderMap.+=(subOrder.id -> true)
    }

    subOrderMap
  }
}
