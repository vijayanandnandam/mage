package service.integration

import data.model.Tables.{FcomtRow, FcsotRow}
import models.{OrderModel, ProcessedSubOrderModel, UserLoginObject}

import scala.concurrent.Future

/**
  * Created by fincash on 19-04-2017.
  */
trait IntegrationService {

  def placeSubOrders(orderModel: OrderModel, order: FcomtRow, subOrderList: List[FcsotRow], userLoginObject: UserLoginObject): Future[List[ProcessedSubOrderModel]]
}
