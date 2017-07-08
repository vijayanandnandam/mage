package controllers

import javax.inject.Inject

import constants.BaseConstants
import helpers.MailHelper
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import play.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.mvc.Security.AuthenticatedAction
import repository.module.UserRepository
import repository.tables.FcubdRepo
import service._
import utils.RequestUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


class PasswordController @Inject()(implicit ec: ExecutionContext, auth: AuthenticatedAction, implicit val ws: WSClient, attachService: AttachmentService, mailService: MailService, config: Configuration, userService: UserService,
                                    userRepository: UserRepository, mailHelper: MailHelper, fcubdRepo: FcubdRepo, configuration: play.api.Configuration)
  extends Controller with BaseConstants {

  val logger, log = LoggerFactory.getLogger(classOf[PasswordController])


  val staticImagePath = configuration.underlying.getString("mail.url.staticImagePath")
  val baseUrl = configuration.underlying.getString("mail.url.baseurl")
  val resetPassUrl = baseUrl+PropertiesLoaderService.getConfig().getString("url.resetPasswordRelPath")
  val subj = PropertiesLoaderService.getConfig().getString("mail.password-reset.default.subject")


  def sendPassResetMail = Action.async(parse.json) { request =>
    val requestData = request.body
    val email = (requestData \ "email").as[String]

    //check if user exists
    userRepository.checkUser(email).flatMap(userStatus => {
      val token = mailHelper.generateLink(email)
      if (userStatus) {
        userRepository.getUserIdByUsername(email).flatMap { userid => {
          fcubdRepo.getById(userid).flatMap(ubdRowOption => {
            if (ubdRowOption.nonEmpty) {
              val ubdRow = ubdRowOption.get
              /*mail template building*/
              val mailHeaderTemplate = views.html.mailHeader("Password Assistance", mailHelper.getMth)
              val link = resetPassUrl + token
              val mailBodyTemplate = views.html.resetPassword(ubdRow, link, mailHelper.getMth)
              val mailTemplate = views.html.mail(mailHeaderTemplate, mailBodyTemplate, mailHelper.getMth)
              val bodyHTML = mailTemplate.toString()
              val bodyText = views.html.resetPasswordTxt(ubdRow, link).toString()
              val ipAddress = RequestUtils.getIpAddress(request)
              userRepository.saveMailLink(userid, token, PASS_RESET_MAIL_VALID_TIME, "N", email,ipAddress).map(retval => {
                mailService.sendMail(email, subj, Some(bodyText), Some(bodyHTML)).map(_messageid => {
                  logger.debug("Message id >>> " + _messageid)
                })
                Ok(Json.obj("exists" -> userStatus))
              })
            } else {
              Future.apply(Ok(Json.obj("exists" -> false)))
            }
          })
        }
        }
      } else {
        Future.apply(Ok(Json.obj("exists" -> userStatus)))
      }
    })
  }


  def getMailKeyValidity = Action.async(parse.json) { request =>

    val requestData = request.body
    val actCode = (requestData \ "key").as[String]

    userRepository.verifyActivationCode(actCode).flatMap { retValTuple => {
      if (retValTuple.nonEmpty) {
        val realTuple = retValTuple.get
        val userid = realTuple._1
        val uactRfnum = realTuple._2
        userRepository.getUsernameByUserid(userid).map { username => {
          Ok(Json.obj("success" -> true, "username" -> username))
        }
        }
      } else {
        Future {
          Ok(Json.obj("success" -> false))
        }
      }
    }
    }
  }

  def getGoogleCaptchaValidity = Action.async(parse.json){ request =>
    val requestData = request.body
    val gReponse = (requestData \ "response").as[String]
    val secret = configuration.underlying.getString("google.recaptcha.secret")
    var url = configuration.underlying.getString("google.recaptcha.verifyurl")
    url  = url+"?secret="+secret+"&response="+gReponse
    this.logger.debug("@@@@ google reCaptcha URL ->"+url)
    var status:Boolean = false
    val a = ws.url(url)
      .withRequestTimeout(Duration(10000, "millis"))
      .withHeaders(("Content-Type" -> "application/json"))
      .get()
      .map{ res => {
        logger.debug("@@@@@@ reCaptcha Result " + res.json)
        status = (res.json \ "success").as[Boolean]
      }}
      .recover({
        case e: Exception => "Error while retrieving reCaptcha response validity"
      })
    Await.result(a, Duration.Inf)
    Future.apply(Ok(Json.obj("success" -> status)))
  }


  def resetPassword = Action.async(parse.json) { request => {

    val requestData = request.body
    val password = (requestData \ "password").as[String]
    val key = (requestData \ "key").as[String]
    val pwHash = BCrypt.hashpw(password, BCrypt.gensalt())

    if (key.nonEmpty && key.length > 0) {
      userRepository.updatePassword(key, pwHash).map(retval => {
        Ok(Json.obj("success" -> retval, "message" -> "Password changed successfully"))
      })
    } else {
      Future.apply(Ok(Json.obj("success" -> false, "message" -> "Something is missing")))
    }

  }
  }
}
