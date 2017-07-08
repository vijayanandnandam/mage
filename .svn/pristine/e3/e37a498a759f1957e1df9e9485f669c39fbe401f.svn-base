package controllers
import javax.inject.Inject

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.`type`.PhoneNumber
import constants.BaseConstants
import helpers.{AuthenticatedAction, OTPHelper}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.{OTPService, PropertiesLoaderService, UserService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * Created by fincash on 09-02-2017.
  */
class MessageSenderController @Inject()(implicit ec:ExecutionContext, auth: AuthenticatedAction,
                                        userService: UserService, oTPService: OTPService, configuration: play.api.Configuration)extends Controller with BaseConstants{

  val AUTH_TOKEN = PropertiesLoaderService.getConfig().getString("twilio.auth.token")
  val ACCOUNT_SID = PropertiesLoaderService.getConfig().getString("twilio.account.sid")
  val twilioCallbackUrl = configuration.underlying.getString("twilio.callback.url")
  val twilioSender = configuration.underlying.getString("twilio.sender.number")

  def send(from:String,to:String,message:String) = auth.Action{
    Twilio.init(ACCOUNT_SID, AUTH_TOKEN)

    val msg = Message
      .creator(new PhoneNumber(to), new PhoneNumber(twilioSender),
        message)
      .create();
    Ok(Json.obj("messageId"->msg.getSid()))
  }

  def generateOTP(purpose:String,from:String,to:String,message:String) = auth.Action.async{request =>

    userService.getUserObjectFromReq(request).flatMap(userObject =>{
      val userId = userObject.get.userid.get
      val userName = userObject.get.username.get
      oTPService.getOTP(purpose,TWILIO_SERVICE_ID,userId).flatMap(otpTuple =>{
        val updatedMessage = message + " " + otpTuple._2
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN)

        val msg = Message
          .creator(new PhoneNumber(to), new PhoneNumber(twilioSender),
            updatedMessage).setStatusCallback(twilioCallbackUrl)
          .create()
        val messageId = msg.getSid()
        oTPService.saveMessageId(messageId,otpTuple._1,userName).map(value =>{
          Ok(Json.obj("messageId"->messageId,"otpId"->otpTuple._1))
        })
      })
    })

  }

  def messageStatus() = Action.async{request=>

    val map = request.body.asFormUrlEncoded.get

    oTPService.updateOTPStatus(map.get("MessageSid").head.head,map.get("SmsStatus").head.head).map(value =>{

      Ok
    })
  }
  def validateOTP(purpose:String,otp:String) = auth.Action.async{request =>
    userService.getUserObjectFromReq(request).flatMap(userObject =>{
      oTPService.validateOTP(otp,purpose,userObject.get.userid.get).map(value =>{
        Ok(Json.obj("valid"->value))
      })
    })
  }

  def getMessageStatus(messageId:String) = Action.async{
    oTPService.getMessageStatus(messageId).map(status =>{
      Ok(Json.obj("status"->status))
    })
  }
}
