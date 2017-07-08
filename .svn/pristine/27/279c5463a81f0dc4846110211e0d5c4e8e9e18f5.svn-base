package service.integration

import javax.inject.Inject

import constants.OrderConstants
import data.model.Tables.{FcomtRow, FcsotRow}
import helpers.OrderHelper
import models.{OrderModel, ProcessedSubOrderModel, SubOrder, UserLoginObject}
import org.slf4j.LoggerFactory
import repository.module.OrderRepository

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by fincash on 19-04-2017.
  */
abstract class IntegrationService @Inject()(orderHelper: OrderHelper, orderRepository: OrderRepository) extends OrderConstants{

  val integrationIogger = LoggerFactory.getLogger(classOf[IntegrationService])

  def placeSubOrders(orderModel: OrderModel, order: FcomtRow, subOrder: FcsotRow, subOrderModel: SubOrder, orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel]

  def updateSubOrderState(subOrder: FcsotRow, order: FcomtRow, newState:Long, orderStateChangeMap: mutable.HashMap[Long, List[Long]],
                          orderProcessed:Long,userLoginObject: UserLoginObject):Future[(Boolean,String)] = {

    val userName = userLoginObject.username.get
    val userId = userLoginObject.userid.get
    val isStateChangeAllowed = orderHelper.isStateChangeAllowed(subOrder.sotostmstaterfnum,newState,orderStateChangeMap)
    if(isStateChangeAllowed) {
      orderRepository.updateStateForSubOrder(subOrder.id, order.id, subOrder.sotostmstaterfnum,newState, userName).map(updated =>{
        orderHelper.notifyZendesk(subOrder,order,orderProcessed,userName,userId)
        (true,STATE_CHANGED_SUCCESSFULLY)
      })
    } else{
      integrationIogger.debug("Suborder State Update failed from "+ subOrder.sotostmstaterfnum + " to " + newState)
      Future.apply((false,STATE_CHANGE_NOT_ALLOWED))
    }

  }

}
