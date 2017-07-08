package helpers

import java.util.{Calendar, Date}
import javax.inject.Inject

import constants.DBConstants
import models.{OrderModel, SubOrder}
import org.slf4j.LoggerFactory
import repository.module.OrderRepository
import utils.DateTimeUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 17-02-2017.
  */
class OrderHelper @Inject()(implicit ec: ExecutionContext, orderRepository: OrderRepository) extends DBConstants {
  val logger, log = LoggerFactory.getLogger(classOf[OrderHelper])


  def filterBSESchemes(orderModel: OrderModel): Future[List[SubOrder]] = {

    logger.info("Filtering for BSE Schemes")
    Future.sequence(for (subOrder <- orderModel.subOrders) yield {
      orderRepository.getBSESchemeCode(subOrder.buySchemeOptionRfnum).map(bseSchemeCode => {
        val validBseSchemeCode = bseSchemeCode.trim
        if (validBseSchemeCode.trim.length > 0) {
          subOrder.copy(bseSchemeCode = Some(validBseSchemeCode))
        } else {
          logger.error("BSE Scheme code for soptrfnum " + subOrder.buySchemeOptionRfnum + " doesn't exist")
          subOrder
        }
      })
    })
  }

  def isAlreadyPlacedInBSE(orderModel: OrderModel, userId: Long): Future[List[Boolean]] = {

    val alreadyPlacedList = for (subOrder <- orderModel.subOrders) yield {
      orderRepository.isAlreadyInBSE(orderModel, subOrder, userId)
    }

    Future.sequence(alreadyPlacedList)
  }

  def isOrderCancelAllowed(orderTime: java.sql.Timestamp, category: String, amctrfnum: Long, bseCutOff: String): Future[Boolean] = {
    getOrderCancelCutOffTime(orderTime, category, amctrfnum, bseCutOff).map(cancelCutOffTime => {
      !new Date().after(cancelCutOffTime)
    })
  }

  def getOrderCancelCutOffTime(orderTime: java.sql.Timestamp, category: String, amctrfnum: Long, bseCutOff: String): Future[Date] = {

    val orderPlacedTime = Calendar.getInstance()
    orderPlacedTime.setTime(new Date(orderTime.getTime))

    val orderProcessCutOffTime = Calendar.getInstance()
    orderProcessCutOffTime.setTimeInMillis(orderTime.getTime)
    val cutOffTimeArray = bseCutOff.split(':')
    orderProcessCutOffTime.set(Calendar.HOUR_OF_DAY, cutOffTimeArray(0).toInt)
    orderProcessCutOffTime.set(Calendar.MINUTE, cutOffTimeArray(1).toInt)
    orderProcessCutOffTime.set(Calendar.SECOND, cutOffTimeArray(2).toInt)

    getBSEOrderProcessLimit(orderProcessCutOffTime, category, amctrfnum).flatMap(bseOrderProcessLimit => {
      if (orderPlacedTime.getTime.after(bseOrderProcessLimit.getTime)) {
        bseOrderProcessLimit.add(Calendar.DAY_OF_MONTH, 1)
        getBSEOrderProcessLimit(bseOrderProcessLimit, category, amctrfnum).map(orderCancelCutOff => {
          orderCancelCutOff.getTime
        })
      } else {
        Future {
          bseOrderProcessLimit.getTime
        }
      }
    })
  }

  def getBSEOrderProcessLimit(orderProcessCutOffTime: Calendar, category: String, amctrfnum: Long): Future[Calendar] = {
    val day = orderProcessCutOffTime.get(Calendar.DAY_OF_WEEK)
    if (day == SUNDAY || day == SATURDAY) {
      orderProcessCutOffTime.add(Calendar.DAY_OF_MONTH, 1)
      getBSEOrderProcessLimit(orderProcessCutOffTime, category, amctrfnum)
    }
    val orderDate = DateTimeUtils.convertCalendarToSqlDate(orderProcessCutOffTime)
    orderRepository.isHolidayExists(orderDate, category, amctrfnum).flatMap(isHoliday => {
      if (isHoliday) {
        orderProcessCutOffTime.add(Calendar.DAY_OF_MONTH, 1)
        getBSEOrderProcessLimit(orderProcessCutOffTime, category, amctrfnum)
      } else {
        Future {
          orderProcessCutOffTime
        }
      }
    })

  }

}
