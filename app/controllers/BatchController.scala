package controllers

import javax.inject.Inject

import constants.{DBConstants, KycConstants, ZendeskConstants}
import helpers.AuthenticatedAction
import models.OrderJsonFormats._
import models.{SubOrderUnitDetails, UserLoginObject}
import models.batch.BatchReqJsonFormats._
import models.batch.{PaymentStatusReqModel, SIPMailReqModel}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.Controller
import service.{OrderService, PaymentService, UserService, ZendeskService}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 03-05-2017.
  */
class BatchController @Inject()(implicit ec: ExecutionContext, zendeskService: ZendeskService, orderService: OrderService,
                                paymentService: PaymentService, userService: UserService, auth: AuthenticatedAction)
  extends Controller with DBConstants with KycConstants with ZendeskConstants{

  val logger, log = LoggerFactory.getLogger(classOf[BatchController])


  def getBSEPaymentStatus() = auth.Action.async(parse.json) { request =>

    val payReqModel = request.body.as[PaymentStatusReqModel]

    paymentService.getBSEPaymentStatus(payReqModel, SYSTEM_USER).map(paymenStatus => {
      Ok(Json.obj("paymentStatus" -> paymenStatus))
    })
  }

  def sendSIPMail() = auth.Action.async(parse.json) { request =>
    val sipReqModel = request.body.as[SIPMailReqModel]
    userService.sendISIPMandateMail(sipReqModel.subOrderId, sipReqModel.subOrderToState).map(mailResp => {
      Ok(Json.obj("sipMailResponse" -> mailResp))
    })
  }

  def sendZendeskOrderUpdateTicket() = auth.Action.async(parse.json) { request =>
    val subOrderUnitDetails = request.body.as[SubOrderUnitDetails];
    orderService.getOrderStateDisplayName(subOrderUnitDetails.orderState).flatMap(orderDisplayName => {
      zendeskService.postOrderUnitUpdate(subOrderUnitDetails.copy(orderStateName = Some(orderDisplayName))).map(isTicketGenerated => {
        Ok(Json.obj("ticketGenerated" -> isTicketGenerated))
      })
    })
  }

  def placeQueuedOrders = auth.Action.async {

    orderService.placeQueuedOrders().map(value => {
      Ok
    })
  }

  def sendPaymentLinkTest = auth.Action.async { request =>

    val useLoginObj: UserLoginObject = new UserLoginObject(Some("sumit.agarwal@fincash.com"), Some(100000), None, Some("Sumit"), None, None, None)
    /*userLoginObject: UserLoginObject, bsePaymentLink: String, linkValidTime: String, totalAmount: Double,
                            isOrderAmountGreaterThan50K: Boolean, cutoffTime: Option[String] = None*/
    orderService.sendPaymentLinkOnMail(useLoginObj, "http://www.google.com", Some("29-06-2017 09:00:00"),10000D).map(_ =>
      Ok
    )
  }

  def updateKycTicket = auth.Action.async(parse.json) { request =>
    val requestData = request.body
    val pan = (requestData \ "pan").as[String];
    val status = (requestData \ "status").as[String];
    logger.debug("Pan:" + pan + "status:" + status);

    val zdConstantaStatus = status match {
      case KYC_DONE => {
        ZENDESK_TICKET_VALUE_KYC_DONE
      };
      case default => {
        ZENDESK_TICKET_VALUE_KYC_NOT_DONE
      }
    }

    userService.getUserByPan(pan).flatMap(users => {
      val zendeskFtr = for (user <- users) yield {
        zendeskService.updateZendeskTicketForKyc(user.id, user.ubdemailid, zdConstantaStatus)
      }

      Future.sequence(zendeskFtr).map(_ => {
        Ok
      })
    })
  }
}
