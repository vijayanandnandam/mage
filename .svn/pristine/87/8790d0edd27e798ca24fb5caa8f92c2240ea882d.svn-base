package controllers

import javax.inject.Inject

import constants.KycConstants
import helpers.{AuthenticatedAction, MailHelper}
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient}
import play.api.mvc.{Action, Controller}
import repository.module.{CNDRepository, KycRepository, UserRepository}
import repository.tables.FcubdRepo
import service.{MailService, PropertiesLoaderService, UserService}
import utils.DateTimeUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

/**
  * Created by Fincash on 07-04-2017.
  */
class SavingsPlusController @Inject()(implicit val ec: ExecutionContext, ws: WSClient, auth: AuthenticatedAction, userService: UserService, kycRepository: KycRepository,
                                      userRepository: UserRepository, fcubdRepo: FcubdRepo, cndRepository: CNDRepository, mailService: MailService, mailHelper: MailHelper, configuration: play.api.Configuration) extends Controller with KycConstants {

  val logger, log = LoggerFactory.getLogger(classOf[SavingsPlusController])

  val ARNCODE = configuration.underlying.getString("fincash.arncode")
  val URL = PropertiesLoaderService.getConfig().getString("reliance.redemption.url")
  val API_KEY = PropertiesLoaderService.getConfig().getString("reliance.redemption.apikey")
  val DEVICE_ID = PropertiesLoaderService.getConfig().getString("reliance.account.deviceid")
  val APP_NAME = PropertiesLoaderService.getConfig().getString("reliance.account.appName")
  val APP_VERSION = PropertiesLoaderService.getConfig().getString("reliance.account.appVersion")
  val IP = PropertiesLoaderService.getConfig().getString("reliance.account.ip")
  val OLDIHNO = PropertiesLoaderService.getConfig().getString("reliance.account.oldihno")
  val BRANCH = PropertiesLoaderService.getConfig().getString("reliance.account.branch")

  val USER_ID = PropertiesLoaderService.getConfig().getString("reliance.moneymanager.Userid")
  val FUND = PropertiesLoaderService.getConfig().getString("reliance.moneymanager.Fund")
  val SCHEME = PropertiesLoaderService.getConfig().getString("reliance.moneymanager.scheme")
  val PLAN = PropertiesLoaderService.getConfig().getString("reliance.moneymanager.plan")
  val OPTIONS = PropertiesLoaderService.getConfig().getString("reliance.moneymanager.options")
  val T_PIN = PropertiesLoaderService.getConfig().getString("reliance.moneymanager.Tpin")
  val M_STATUS = PropertiesLoaderService.getConfig().getString("reliance.moneymanager.Mstatus")

  def makeRedemption = auth.Action.async(parse.json) { request =>
    val requestData = request.body

    var folioNo = requestData.\("folio").as[String]
    val redFlag = (requestData \ "redFlag").as[String]
    val unitAmtFlag = (requestData \ "unitamtFlag").as[String]
    val unitAmtValue = (requestData \ "unitamtValue").as[String]

    val trDate = DateTimeUtils.convertDateToFormat(DateTimeUtils.getCurrentDate(), "MM/dd/yyyy")
    val entDate = DateTimeUtils.convertDateToFormat(DateTimeUtils.getCurrentDate(), "MM/dd/yyyy")

    val redemptionUrl = URL + "Redemptionsave?" + "fund=" + FUND + "&acno=" + folioNo + "&arncode=" + ARNCODE + "&scheme=" +
                        SCHEME + "&plan=" + PLAN + "&options=" + OPTIONS + "&RedFlag=" + redFlag + "&UnitamtFlag=" + unitAmtFlag +
                        "&UnitAmtValue=" + unitAmtValue + "&userid=" + USER_ID + "&Tpin=" + T_PIN + "&Mstatus=" + M_STATUS +
                        "&Fname=&Mname=&Lname=&Cuttime=&pangno=&bank=" + getRedInvbankDetails_V1(folioNo) +
                        "&ip=" + IP + "&oldihno=" + OLDIHNO + "&trdate=" + trDate + "&entdate=" + entDate + "&branch=" + BRANCH +
                        "&proxybranch=&deviceid=" + DEVICE_ID + "&appVersion=" + APP_VERSION + "&appName=" + APP_NAME + "&apikey=" + API_KEY


    logger.debug("URL " + redemptionUrl)

    ws.url(redemptionUrl)
      .withRequestTimeout(Duration(30000, "millis")).get()
      .map { res => {
        val refno = (res.json \ "REFNO").as[String]
        val msg = (res.json \ "Return_Msg").as[String]
        val code = (res.json \ "Return_code").as[String]
      logger.debug("Result " + res)
      Ok(Json.obj("Result" -> res.json))
    }}.recover({
      case e: Exception => Ok(Json.obj("statusCode" -> "KS999"))
    })
  }

  def getRedInvbankDetails_V1(folioNo: String): String = {
    var bankName = ""
    val getBankUrl = URL + "RedInvbankDetails_V1?" + "&arncode=" + ARNCODE + "&acno=" + folioNo + "&scheme=" + SCHEME + "&plan=" + PLAN +
                    "&deviceid=" + DEVICE_ID + "&appVersion=" + APP_VERSION + "&appName=" + APP_NAME + "&apikey=" + API_KEY

    val a = ws.url(getBankUrl).withHeaders("Accept" -> "multipart/form-data")
      .get()
      .map { res => {
        bankName = (res.json.asInstanceOf[JsArray].value(0) \ "BankName").as[String]
      }}
      .recover({
      case e: Exception => "Error while retrieving Bank"
    })
    Await.result(a, Duration.Inf)
    bankName
  }

  /*def getRedInvbankDetails_V1 = Action.async(parse.json) { request => {
    val requestData = request.body
    var folioNo = requestData.\("folio").as[String]
    val getBankUrl = URL + "RedInvbankDetails_V1?" + "&arncode=" + ARNCODE + "&acno=" + folioNo + "&scheme=" + SCHEME + "&plan=" + PLAN +
      "&deviceid=" + DEVICE_ID + "&appVersion=" + APP_VERSION + "&appName=" + APP_NAME + "&apikey=" + API_KEY

    ws.url(getBankUrl).withHeaders("Accept" -> "multipart/form-data")
      .withRequestTimeout(Duration(30000, "millis")).get()
      .map { res => {
        //
        Ok(res.json.asInstanceOf[JsArray].value(0) \ "BankName")
      }
      }
  }
  }*/
}