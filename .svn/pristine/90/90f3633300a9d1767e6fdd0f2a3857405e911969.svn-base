package controllers

import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.{Inject, Named}

import constants.KycConstants
import helpers.{AuthenticatedAction, MailHelper, PoolingClientConnectionManager, ZendeskHelper}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.{BasicAuthCache, BasicCredentialsProvider}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpHost, NameValuePair}
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.zendesk.client.v2.model.{Comment, CustomFieldValue}
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repository.module.{CNDRepository, KycRepository, UserRepository, ZendeskRepository}
import repository.tables.FcubdRepo
import service.{MailService, PropertiesLoaderService, UserService, ZendeskService}
import utils.RequestUtils

import scala.collection.JavaConversions
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class KYCController @Inject()(implicit val ec: ExecutionContext, ws: WSClient, auth: AuthenticatedAction, userService: UserService, kycRepository: KycRepository,
                              userRepository: UserRepository, fcubdRepo: FcubdRepo, cndRepository: CNDRepository, mailService: MailService, mailHelper: MailHelper,
                              configuration: play.api.Configuration, zendeskHelper: ZendeskHelper, zendeskService:ZendeskService, zendeskRepository: ZendeskRepository,
                              @Named("externalPoolingClientConnectionManager") kycPoolingClient: PoolingClientConnectionManager) extends Controller with KycConstants {

  val logger, log = LoggerFactory.getLogger(classOf[KYCController])

  val LIVE_URL_HOST = PropertiesLoaderService.getConfig().getString("cams.ekyc.urlHost")
  val LIVE_URL_PATH = PropertiesLoaderService.getConfig().getString("cams.ekyc.urlPath")
  val ACCOUNT_ID = PropertiesLoaderService.getConfig().getString("cams.account.id")
  val PASSWORD = PropertiesLoaderService.getConfig().getString("cams.account.password")
  val APPID = PropertiesLoaderService.getConfig().getString("cams.account.appid")
  val INTERMEDIATERY_ID = PropertiesLoaderService.getConfig().getStringList("cams.intermediateryId")
  val RETURN_DATA_STR = PropertiesLoaderService.getConfig().getString("cams.returnDataStr")
  val RETURN_URL = PropertiesLoaderService.getConfig().getString("cams.returnUrl")
  val zendesk = zendeskHelper.zendesk


  def addKycErrorResponse = auth.Action.async(parse.json) {request =>
    val requestData = request.body
    var pan = requestData.\("pan").as[String].toUpperCase
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.addErrorPanToKycLog(pan, username.get).map(res => {
        Ok(Json.obj("success" -> true))
      })
    })
  }

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

    val httpClient = kycPoolingClient.getHttpClient//// getHttpClientWithAuth(ACCOUNT_ID, PASSWORD)
    val targetHost = new HttpHost(LIVE_URL_HOST, 443, "https")
    val credsProvider: BasicCredentialsProvider = new BasicCredentialsProvider()
    val credentials: UsernamePasswordCredentials = new UsernamePasswordCredentials(ACCOUNT_ID, PASSWORD)
    credsProvider.setCredentials(AuthScope.ANY, credentials)

    val authCache = new BasicAuthCache()
    val basicAuth = new BasicScheme()
    authCache.put(targetHost, basicAuth)
    val context = HttpClientContext.create()
    context.setCredentialsProvider(credsProvider)
    context.setAuthCache(authCache)

    val paramList: ListBuffer[NameValuePair] = ListBuffer[NameValuePair]()
    paramList.+=(new BasicNameValuePair("url", RETURN_URL))
    paramList.+=(new BasicNameValuePair("kyc_data", kyc_data))
//    val jsonObj = Json.obj("url" -> RETURN_URL, "kyc_data" -> kyc_data)
    val httpPost = new HttpPost(LIVE_URL_PATH)
    httpPost.setEntity(new UrlEncodedFormEntity(JavaConversions.seqAsJavaList[NameValuePair](paramList.toList)))
//    httpPost.addHeader("Accept", "multipart/form-data")
//    httpPost.setEntity(new StringEntity(jsonObj.toString()))


    try {
      val httpResponse = httpClient.execute(targetHost, httpPost, context)
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        val inputStream = httpResponse.getEntity.getContent
//        val res: JsValue = Json.parse(inputStream)
        val doc = Jsoup.parse(scala.io.Source.fromInputStream(inputStream).mkString)
        val result = doc.getElementById("result")
        var output = result.attr("value")
        logger.debug("output " + output)
        var arr = output.split("\\|")
        var eKycStatus = arr(1)
        var statusCode = arr(3)
        Future{Ok(Json.obj("statusCode" -> statusCode))}
      }
      else {
        Future{Ok(Json.obj("statusCode" -> "KS999"))}
      }
    }
    catch {
      case s: SocketTimeoutException => {
        logger.debug("Socket Timeout Exception >> ", s);
        Future{Ok(Json.obj("statusCode" -> "KS999"))}
      }
      case c: java.net.ConnectException => { logger.debug("Connection Time Out Occurred >> ", c); Future{Ok(Json.obj("statusCode" -> "KS999"))}}
      case io: IOException => { logger.debug("IO exception >> ", io); Future{Ok(Json.obj("statusCode" -> "KS999"))}}
    }


    /*ws.url(LIVE_URL).withAuth(ACCOUNT_ID, PASSWORD, WSAuthScheme.BASIC).withHeaders("Accept" -> "multipart/form-data")
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
    })*/
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
  }}

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
  }}

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
  }}

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
  }}

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
  }}

  def updateKycStatus = auth.Action.async(parse.json) { request => {
    var requestData = request.body
    var kycStatus = (requestData \ "kycStatus").as[String]
    var kycType = (requestData \ "kycType").as[String]

    var kycStatName = ""
    if(kycStatus.toLowerCase()== KYC_DONE.toLowerCase()){
      kycStatName = zendeskHelper.ZENDESK_TICKET_VALUE_KYC_DONE
    }else if(kycStatus.toLowerCase()== KYC_NOTDONE.toLowerCase()){
      kycStatName = zendeskHelper.ZENDESK_TICKET_VALUE_KYC_NOT_DONE
    } else if(kycStatus.toLowerCase()== KYC_UNDERPROCESS.toLowerCase()){
      kycStatName = zendeskHelper.ZENDESK_TICKET_VALUE_KYC_UNDER_PROCESS
    }
    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        kycRepository.updateuserKYCStatus(userid, username.get, kycStatus, kycType).map(status =>{
          if(status==true){
            /*zendesk ticket update logic*/
            val istktAllowed = zendeskHelper.isTktAllowed
            if(istktAllowed){
              val purpose = Some(zendeskHelper.TKT_PURPOSE_REGISTRATION)
              zendeskRepository.getTicketsByUserIdANDpurpose(userid,purpose).flatMap(fctktRows =>{
                if(fctktRows.nonEmpty && !fctktRows.head.tktticketid.equalsIgnoreCase("0")){
                  Future.apply(Some(fctktRows.head.tktticketid.toLong))
                }else {
                  var oldTktrfnum : Option[Long] = None
                  if(fctktRows.nonEmpty){
                    oldTktrfnum = Some(fctktRows.head.id)
                  }
                  val requester = zendeskHelper.createRequester
                  requester.setEmail(username.get)
                  requester.setName(username.get)
                  zendeskService.createTicket(requester, zendeskHelper.onBoardingSubject, None, Some(zendeskHelper.ZENDESK_GROUP_ONBOARDING),
                    username.getOrElse(""), username.getOrElse(""), userid,None,purpose, oldTktrfnum).map(tktIdOption =>{
                    tktIdOption
                  })
                }
              }).map(tktOption=>{
                val tktId = tktOption.getOrElse(0L)
                val customField = new CustomFieldValue(zendeskHelper.ZENDESK_TICKET_KYC_STATUS, kycStatName)
                val customFieldList = new java.util.ArrayList[CustomFieldValue]
                customFieldList.add(customField)
                val customFieldRes = zendeskService.setCustomField(tktId, customFieldList, None, Some("SYSTEM COMMENT : UPDATING KYC STATUS TO " + kycStatName), None)
                logger.debug("Customfield Status -->"+ customFieldRes)
                if(customFieldRes._2){
                  val zdUserId = zendeskHelper.getUserIdByEmail(username.get)
                  val userField = new java.util.HashMap[String, AnyRef]()
                  userField.put(zendeskHelper.ZENDESK_USER_FIELD_NAME_KYC, kycStatName)
                  zendeskService.setUserField(zdUserId, userField)
                }
                Future.apply(Ok(Json.obj("success" -> true, "message" -> "KYC STATUS Updated successfully")))
              })
            }else{
              logger.debug("tkt not allowed for this config")
            }
            Ok(Json.obj("success" -> true))
          }else{
            Ok(Json.obj("success" -> false))
          }
        })
      })
    })
  }}


  def updateFilePath = auth.Action.async(parse.json) { request => {
    val requestData = request.body
    val ipAddress = RequestUtils.getIpAddress(request)
    var path = (requestData \ "path").as[String]
    var doctypecnd = (requestData \ "cndtype").as[String]
    var dmtid = (requestData \ "dmtid").as[String]

    userService.getUsernameFromRequest(request).flatMap(username => {
      userRepository.getUserIdByUsername(username.get).flatMap(userid => {
        if (doctypecnd.nonEmpty) {
          kycRepository.updateDocumentPath(ipAddress, userid, username.get, dmtid, path, doctypecnd.toLong).map(value => {
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
  }}

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
  }}

}