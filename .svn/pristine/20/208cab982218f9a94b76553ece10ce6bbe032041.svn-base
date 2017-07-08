package service

import javax.inject.Inject

import constants.{DBConstants, IntegrationConstants, OrderConstants}
import data.model.Tables.{FcsmtRow, FcsoptRow, FcsotRow}
import helpers.OrderHelper
import models.{CancelSubOrder, FCOrderEntryModel, FCXsipOrderEntryModel, UserLoginObject}
import models.integration.enumerations.{DPTxnEnum, FirstOrderEnum, TransactionCodeEnum, TransactionModeEnum}
import org.slf4j.LoggerFactory
import repository.module.{OrderRepository, PasswordRepository, SchemeRepository}
import service.integration.BSEStarOrderEntryServiceImpl
import utils.DateTimeUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 31-03-2017.
  */
class OrderCancelService @Inject()(implicit ec: ExecutionContext, orderRepository: OrderRepository,
                                   bseStarOrderEntryService: BSEStarOrderEntryServiceImpl,
                                   passwordRepository: PasswordRepository,
                                   orderHelper: OrderHelper, schemeRepository: SchemeRepository) extends DBConstants with IntegrationConstants with OrderConstants {

  val logger, log = LoggerFactory.getLogger(classOf[OrderCancelService])


  def isCancellationAllowed(subOrderIdList: ListBuffer[Long], userPk: Long): Future[mutable.HashMap[Long,Boolean]] = {

    orderRepository.getBSECutOff().flatMap(bseCutOff => {
      orderRepository.getSubOrderListDetails(subOrderIdList, userPk).flatMap(subOrderList => {
        checkSubOrderCancelAllowed(subOrderList.toList, bseCutOff).map(sotCancelTuple => {
          val subOrderCancelMap = mutable.HashMap[Long,Boolean]()
          sotCancelTuple.foreach(cancelTuple =>{
            subOrderCancelMap.+=(cancelTuple._1->cancelTuple._2)
          })
          subOrderCancelMap
        })
      })
    })
  }

  def checkSubOrderCancelAllowed(subOrderList: List[FcsotRow], bseCutOff: String): Future[List[(Long, Boolean)]] = {

    val soptIdList = ListBuffer[Long]()
    subOrderList.foreach(subOrder => {
      soptIdList.+=(subOrder.sotsoptrfnum)
    })
    schemeRepository.getParentCategoryMap().flatMap(categoryMap => {
      schemeRepository.getSchemeOptionsByIdList(soptIdList.toList).flatMap(soptRowSeq => {
        val smtIdList = ListBuffer[Long]()
        val soptMap = mutable.HashMap[Long, FcsoptRow]()
        soptRowSeq.foreach(soptRow => {
          smtIdList.+=(soptRow.soptsmtrfnum)
          soptMap.+=(soptRow.id -> soptRow)
        })
        schemeRepository.getSchemesByIdList(smtIdList.toList).flatMap(smtRowSeq => {
          val smtMap = mutable.HashMap[Long, FcsmtRow]()
          smtRowSeq.foreach(smtRow => {
            smtMap.+=(smtRow.id -> smtRow)
          })
          Future.sequence(for (subOrder <- subOrderList) yield {
            val smtRow = smtMap.get(soptMap.get(subOrder.sotsoptrfnum).get.soptsmtrfnum).get
            val category = categoryMap.get(smtRow.smtctmtrfnum).get
            orderHelper.isOrderCancelAllowed(subOrder.createdate.get, category, smtRow.smtamctrfnum, bseCutOff).map(isCancelAllowed => {
              (subOrder.id,isCancelAllowed)
            })
          })
        })
      })
    })
  }

  def cancelOrder(cancelSubOrder: CancelSubOrder, userLoginObject: UserLoginObject): Future[Boolean] = {

    passwordRepository.getBSEPassword(BSE_ORDER_API_PASS_CODE).flatMap(passRow => {

      val expiryTime = passRow._2
      val isValid = DateTimeUtils.checkBsePassTimeValidity(expiryTime)

      if (isValid) {
        getInvModeAndCancelOrder(cancelSubOrder, userLoginObject)
      } else {
        passwordRepository.updateBSEPassword().flatMap(_ => {
          getInvModeAndCancelOrder(cancelSubOrder, userLoginObject)
        })
      }
    })
  }

  def getInvModeAndCancelOrder(cancelSubOrder: CancelSubOrder, userLoginObject: UserLoginObject): Future[Boolean] = {

    val subOrderId = cancelSubOrder.subOrderId
    val userName = userLoginObject.username.get
    val userPk = userLoginObject.userid.get
    orderRepository.getSubOrderDetails(subOrderId, userPk).flatMap(subOrderList => {
      val subOrder = subOrderList.head
      if (subOrder.sotinvestmentmode == LUMPSUM_INVESTMENT_MODE) {
        cancelLumpSumOrder(subOrder, cancelSubOrder, userLoginObject).map(cancelled => {
          cancelled
        })
      } else {
        cancelSipOrder(subOrder, cancelSubOrder, userLoginObject).map(cancelled => {
          cancelled
        })
      }
    })
  }

  def cancelLumpSumOrder(subOrder: FcsotRow, cancelSubOrder: CancelSubOrder, userLoginObject: UserLoginObject): Future[Boolean] = {

    val userName = userLoginObject.username.get
    orderRepository.getBSESchemeCode(subOrder.sotsoptrfnum).flatMap(bseSchemeCode => {
      orderRepository.getOrderMasterDetails(subOrder.sotomtrfnum).flatMap(omtRow => {

        val order = omtRow.get
        val clientCode = userLoginObject.userid.get.toString
        val dpTransaction = DPTxnEnum.withName(order.omtdptranscn)

        val fcOrderEntry = FCOrderEntryModel("0" + subOrder.id.toString, TransactionCodeEnum.CXL, Some(subOrder.sottrnsctionid1.get.toLong), bseSchemeCode, clientCode,
          order.omtbuysell, OrderConstants.BUY_SELL_TYPEMAP.getOrElse(subOrder.sotbuyselltype, ""), dpTransaction,
          Some(subOrder.sotomtrfnum.toString), Some(subOrder.sotorderamount), None, cancelSubOrder.ipaddress, None)

        bseStarOrderEntryService.getOrderEntryParamResponse(fcOrderEntry, userName).map(bseOrderValidateWrapper => {

          val errorList = bseOrderValidateWrapper.errorList
          if (!errorList.get.isEmpty) {
            logger.debug("LumpSum Order Not Cancelled in BSE")
            false
          } else {
            logger.debug("LumpSum Order Cancelled successfully in BSE")
            orderRepository.updateSubOrderState(subOrder.id, subOrder.sotomtrfnum, subOrder.sotostmstaterfnum, ORDER_CANCELLED, userName)
            true
          }
        })
      })
    })
  }

  def cancelSipOrder(subOrder: FcsotRow, cancelSubOrder: CancelSubOrder, userLoginObject: UserLoginObject): Future[Boolean] = {

    val userName = userLoginObject.username.get

    orderRepository.getBSESchemeCode(subOrder.sotsoptrfnum).flatMap(bseSchemeCode => {
      orderRepository.getOrderMasterDetails(subOrder.sotomtrfnum).flatMap(omtRow => {

        val order = omtRow.get
        val clientCode = userLoginObject.userid.get.toString
        val dpTransaction = DPTxnEnum.withName(order.omtdptranscn)
        val transactionMode = TransactionModeEnum.withName(subOrder.sottype)

        var fcXsipOrderEntry = FCXsipOrderEntryModel("0" + subOrder.id.toString, TransactionCodeEnum.CXL, bseSchemeCode, clientCode,
          DateTimeUtils.getEstimatedSIPDate(subOrder.sotsipdayofmonth.get),
          OrderConstants.FREQUENCY_MAP.getOrElse(subOrder.sotsipfrequency.get, ""), transactionMode, dpTransaction, Some(order.id.toString),
          OrderConstants.ROLLING_FREQUENCY, subOrder.sotorderamount, subOrder.sotsipinstallments.get, None, None,
          FirstOrderEnum.Y, Some(subOrder.sottrnsctionid2.get.toLong), cancelSubOrder.ipaddress, None)

        bseStarOrderEntryService.getXsipOrderEntryParamResponse(fcXsipOrderEntry, userName).map(bseXsipOrderValidateWrapper => {

          val errorList = bseXsipOrderValidateWrapper.errorList
          if (!errorList.get.isEmpty) {
            logger.debug("Sip Order Not Cancelled in BSE")
            false
          } else {
            logger.debug("Sip Order Cancelled successfully in BSE")
            orderRepository.updateSubOrderState(subOrder.id, subOrder.sotomtrfnum, subOrder.sotostmstaterfnum, ORDER_CANCELLED, userName)
            true
          }
        })
      })
    })
  }
}
