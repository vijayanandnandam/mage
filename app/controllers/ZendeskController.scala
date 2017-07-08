package controllers

import javax.inject.Inject

import constants.{KycConstants, ZendeskConstants}
import helpers.ZendeskHelper
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, Controller}
import repository.module.{KycRepository, ZendeskRepository}
import service.{OrderService, ZendeskService}

import scala.concurrent.{ExecutionContext, Future}

class ZendeskController @Inject()(implicit val ec: ExecutionContext, zendeskHelper: ZendeskHelper, zendeskService: ZendeskService,
                                  zendeskRepository: ZendeskRepository, orderService: OrderService,
                                  kycRepository: KycRepository) extends Controller with ZendeskConstants with KycConstants {

  val logger, log = LoggerFactory.getLogger(classOf[ZendeskController])

  def postRequest = Action.async(parse.json) { request =>
    val requestData = request.body
    val zendeskRequestObj = (requestData \ "zendeskRequest").asOpt[JsObject]
    val message = new StringBuilder
    if (zendeskRequestObj.nonEmpty) {
      logger.debug(Json.stringify(zendeskRequestObj.get))
      //{"kycRequest":{"userId":"100508","pan":"BYFPD2456P","mobile":"9535877327", "tags" : "" ,"kyc":"kyc_under_process"}}
      val kycRequest = (zendeskRequestObj.get \ "kycRequest").asOpt[JsObject]
      val paymentRequest = (zendeskRequestObj.get \ "paymentRequest").asOpt[JsObject]
      if (kycRequest.nonEmpty) {
        zendeskService.updateKycFromZendeskRequest(kycRequest.get).map(tuple => {
          logger.debug(tuple._2)
          if (tuple._1) {
            Ok
          } else {
            NotAcceptable(Json.obj("success" -> tuple._1, "message" -> tuple._2))
          }
        })
      } else if (paymentRequest.isDefined) {
        initiatePaymentMailFromZendeskRequest(paymentRequest.get).map(response => {
         if(response) {
           Ok
         } else {
           NotAcceptable(Json.obj("success" -> false, "message" -> "Unable to send mail"))
         }
        })
      } else {
        Future.apply(NotAcceptable(Json.obj("success" -> false, "message" -> "Unidentified request type")))
      }
    } else {
      Future.apply(NotAcceptable(Json.obj("success" -> false, "message" -> "Unidentified request type")))
    }
  }


  def initiatePaymentMailFromZendeskRequest(paymentMailRequest : JsObject): Future[Boolean] = {
    val zendeskTicketIdOption= (paymentMailRequest \ "ticketId").asOpt[JsValue];
    val zendeskTicketId = zendeskTicketIdOption.get.as[String].trim
    if(zendeskTicketId.length > 0) {
      zendeskRepository.getOrderByZendeskTktId(zendeskTicketId).flatMap(tktRowOption => {
        if(tktRowOption.isDefined) {
          orderService.sendPaymentMailBySubOrderId(tktRowOption.get.tktsotrfnum.get,tktRowOption.get.tktubdrfnum.get).map(response => {
            logger.debug(response)
            true
          })
        } else {
          Future.apply(false)
        }
      })
    } else {
      Future.apply(false)
    }

  }

}