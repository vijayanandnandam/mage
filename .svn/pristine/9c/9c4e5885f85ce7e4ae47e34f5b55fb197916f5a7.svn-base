package controllers

import javax.inject.Inject

import constants.KycConstants
import helpers.{AuthenticatedAction, MailHelper}
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.libs.ws.{WSAuthScheme, WSClient}
import play.api.mvc.Controller
import repository.module.{CNDRepository, KycRepository, UserRepository}
import repository.tables.FcubdRepo
import service.{MailService, PropertiesLoaderService, UserService}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class KYCController @Inject()(implicit val ec: ExecutionContext, ws: WSClient, auth: AuthenticatedAction, userService: UserService, kycRepository: KycRepository,
                              userRepository: UserRepository, fcubdRepo: FcubdRepo, cndRepository: CNDRepository, mailService: MailService, mailHelper: MailHelper, configuration: play.api.Configuration) extends Controller with KycConstants {

  val logger, log = LoggerFactory.getLogger(classOf[KYCController])

  val LIVE_URL = PropertiesLoaderService.getConfig().getString("cams.ekyc.url")
  val ACCOUNT_ID = PropertiesLoaderService.getConfig().getString("cams.account.id")
  val PASSWORD = PropertiesLoaderService.getConfig().getString("cams.account.password")
  val APPID = PropertiesLoaderService.getConfig().getString("cams.account.appid")
  val INTERMEDIATERY_ID = PropertiesLoaderService.getConfig().getStringList("cams.intermediateryId")
  val RETURN_DATA_STR = PropertiesLoaderService.getConfig().getString("cams.returnDataStr")
  val RETURN_URL = PropertiesLoaderService.getConfig().getString("cams.returnUrl")



  def checkKYCFromPAN = auth.Action.async(parse.json) { request =>
    val requestData = request.body

    var pan = requestData.\("pan").as[String].toUpperCase
    //u.ubdpan
    var email = ""
    //u.ubdemailid
    var mob = ""
    //u.ubdmobileno
    println(INTERMEDIATERY_ID)
    var kyc_data = pan + "|" + email + "|" + mob + "|" + APPID + "|" + ACCOUNT_ID + "|" + PASSWORD +
      "|" + INTERMEDIATERY_ID.get(Random.nextInt(INTERMEDIATERY_ID.size)) + "|" + RETURN_DATA_STR

    logger.debug("kyc_data " + kyc_data)

    ws.url(LIVE_URL).withAuth(ACCOUNT_ID, PASSWORD, WSAuthScheme.BASIC).withHeaders("Accept" -> "multipart/form-data")
      .withRequestTimeout(Duration(30000, "millis")).post(Map(
      "url" -> Seq(RETURN_URL),
      "kyc_data" -> Seq(kyc_data)
    )).map { res => {
      val doc = Jsoup.parse(res.body)
      val result = doc.getElementById("result")
      var output = result.attr("value")
      logger.debug("output " + output)
      var arr = output.split("\\|")
      var eKycStatus = arr(1)
      var statusCode = arr(3)
      Ok(Json.obj("statusCode" -> statusCode))
    }
    }.recover({
      case e: Exception => Ok(Json.obj("statusCode" -> "KS999"))
    })
  }

  def checkKycStatus = auth.Action.async { request => {
    var kycstatus = KYC_NOTDONE
    userService.getUseridFromRequest(request).flatMap(userid => {
      kycRepository.getDmtIds(userid.get).flatMap(dmtObj => {
        kycRepository.getUserKYCStatus(userid.get.toLong).map(kycRowList => {
          if (kycRowList.isEmpty) {
            logger.error("Kyc Status for user pk " + userid.toString + " not found")
            kycstatus = KYC_NOTDONE
          } else {
            kycstatus = kycRowList.head.kycstatus
          }
          Ok(Json.obj("kycstatus" -> kycstatus,
            "photodmtid" -> dmtObj._1,
            "pandmtid" -> dmtObj._2,
            "addressdmtid" -> dmtObj._3,
            "bankdmtid" -> dmtObj._4,
            "signaturedmtid" -> dmtObj._5
          ))
        })
      })
    })
  }
  }

  def checkIfBankUpdated = auth.Action.async { request => {
    var bankupdated = false
    userService.getUseridFromRequest(request).flatMap(userid => {
      userRepository.getUserBank(userid.get).map(userBank => {
        if (!userBank.buaid.get.isEmpty) {
          bankupdated = true
        }
        Ok(Json.obj("isbankupdated" -> bankupdated))
      })
    })
  }
  }

  def checkIfFatcaUpdated = auth.Action.async { request => {
    var fatcaupdated = false
    userService.getUseridFromRequest(request).flatMap(userid => {
      userRepository.getUserFatca(userid.get).map(userFatca => {
        if (!userFatca.futid.get.isEmpty) {
          fatcaupdated = true
        }
        Ok(Json.obj("isfatcaupdated" -> fatcaupdated))
      })
    })
  }
  }

  def checkIfBasicDetailsUpdated = auth.Action.async { request => {
    var basicdetailsupdated = false
    userService.getUseridFromRequest(request).flatMap(userid => {
      userRepository.getUserBasic(userid.get).map(userBasic => {
        if (!userBasic.gender.get.isEmpty && !userBasic.pan.get.isEmpty && !userBasic.panName.get.isEmpty && !userBasic.dob.get.isEmpty
          && !userBasic.contact.isEmpty && !userBasic.fatherName.get.isEmpty && !userBasic.motherName.get.isEmpty && !userBasic.maritalStatus.get.isEmpty) {
          basicdetailsupdated = true
        }
        Ok(Json.obj("isbasicdetailsupdated" -> basicdetailsupdated))
      })
    })
  }
  }

  def checkIfAddressUpdated = auth.Action.async { request => {
    var addressupdated = false
    userService.getUseridFromRequest(request).flatMap(userid => {
      userRepository.getUserAddress(userid.get).map(userAddress => {
        if (userAddress.permanentAddress.nonEmpty && userAddress.permanentAddress.get.amtid.nonEmpty && userAddress.permanentAddress.get.amtid.get.length > 0) {
          addressupdated = true
        }
        Ok(Json.obj("isaddressupdated" -> addressupdated))
      })
    })
  }
  }

  def updateKycStatus = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    var kycStatus = (requestData \ "kycStatus").as[String]
    var kycType = (requestData \ "kycType").as[String]
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        kycRepository.updateuserKYCStatus(userid, username.get, kycStatus, kycType).map(status =>{
          if(status==true){
            Ok(Json.obj("success" -> true))
          }else{
            Ok(Json.obj("success" -> false))
          }
        })
      })
    })
  }
  }

  def updateFilePath = auth.Action.async(parse.json) { request => {
    val requestData = request.body
    var path = (requestData \ "path").as[String]
    var doctypecnd = (requestData \ "cndtype").as[String]
    var dmtid = (requestData \ "dmtid").as[String]

    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        if (doctypecnd.nonEmpty) {
          kycRepository.updateDocumentPath(userid, username.get, dmtid, path, doctypecnd.toLong).map(value => {
            Ok(Json.obj("dmtid" -> value._1,
              "cndtype" -> value._2,
              "path" -> value._3
            ))
          })
        }
        else {
          Future.apply(Ok(Json.obj("error" -> "Error occurred while saving file. Please try again")))
        }
      })
    })
  }
  }

  def getDmtIds = auth.Action.async { request => {
    userService.getUseridFromRequest(request).flatMap(userid => {
      kycRepository.getDmtIds(userid.get).map(dmtObj => {
        logger.debug("" + dmtObj)
        Ok(Json.obj(
          "photodmtid" -> dmtObj._1,
          "pandmtid" -> dmtObj._2,
          "addressdmtid" -> dmtObj._3,
          "bankdmtid" -> dmtObj._4,
          "signaturedmtid" -> dmtObj._5,
          "photocndtype" -> dmtObj._6,
          "pancndtype" -> dmtObj._7,
          "addresscndtype" -> dmtObj._8,
          "bankcndtype" -> dmtObj._9,
          "signaturecndtype" -> dmtObj._10,
          "photopath" -> dmtObj._11,
          "panpath" -> dmtObj._12,
          "addresspath" -> dmtObj._13,
          "bankpath" -> dmtObj._14,
          "signaturepath" -> dmtObj._15,
          "photostatus" -> dmtObj._16,
          "panstatus" -> dmtObj._17,
          "addressstatus" -> dmtObj._18,
          "bankstatus" -> dmtObj._19,
          "signaturestatus" -> dmtObj._20
        ))
      })
    })
  }
  }


}