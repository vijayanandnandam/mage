package models.batch

import play.api.libs.json.Json

/**
  * Created by fincash on 03-05-2017.
  */
case class PaymentStatusReqModel(clientCode: String, txnId1: Long)
case class SIPMailReqModel(subOrderId:Long, subOrderToState: Long)

object BatchReqJsonFormats {


  implicit val paymentStatusFormat = Json.format[PaymentStatusReqModel]
  implicit val sIPMailReqFormat = Json.format[SIPMailReqModel]
}
