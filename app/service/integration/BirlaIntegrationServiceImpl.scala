package service.integration

import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import javax.inject.{Inject, Named, Singleton}

import constants.{DBConstants, IntegrationConstants, MongoConstants, OrderConstants}
import data.model.Tables.{FcomtRow, FcsotRow}
import helpers.{OrderHelper, PoolingClientConnectionManager}
import models.{OrderModel, ProcessedSubOrderModel, SubOrder, UserLoginObject}
import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONElement, BSONObjectID, BSONValue}
import repository.module.{BankRepository, IntegrationRepository, OrderRepository, SchemeRepository}
import service._
import utils.{DateTimeUtils, RequestUtils}
import reactivemongo.bson.DefaultBSONHandlers._
import reactivemongo.play.json.BSONFormats

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by Fincash on 30-05-2017.
  */

@Singleton
class BirlaIntegrationServiceImpl @Inject()(implicit ec: ExecutionContext, configuration: play.api.Configuration, ws: WSClient,
                                            bseStarOrderEntryService: BSEStarOrderEntryServiceImpl, mongoDbService: MongoDbService,
                                            bseUploadService: BSEStarUploadServiceImpl, orderHelper: OrderHelper,
                                            @Named("externalPoolingClientConnectionManager") bslPoolingClient: PoolingClientConnectionManager,
                                            orderRepository: OrderRepository, integrationRepository: IntegrationRepository,
                                            schemeService: SchemeService, aMCService: AMCService, schemeRepository: SchemeRepository,
                                            bankRepository: BankRepository, bankService: BankService)
  extends IntegrationService(orderHelper, orderRepository) with DBConstants with MongoConstants with IntegrationConstants with OrderConstants with Controller {

  def collection(name: String) =  mongoDbService.collection(name)

  val logger, log = LoggerFactory.getLogger(classOf[BirlaIntegrationServiceImpl])
  val ARNCODE = configuration.underlying.getString("fincash.arncode")
  val URL_HOST = configuration.underlying.getString("birla.url.host")
  val FOLIO_URL = configuration.underlying.getString("birla.url.folioPath")
  val IR_URL = configuration.underlying.getString("birla.url.irPath")
  val REDEMPTION_URL = configuration.underlying.getString("birla.url.redemptionPath")
  val API_KEY = configuration.underlying.getString("birla.account.apikey")
  val IP = configuration.underlying.getString("birla.account.ip")
  val USER_ID = configuration.underlying.getString("birla.account.userid")
  val PASSWORD = configuration.underlying.getString("birla.account.password")
  val SALT = configuration.underlying.getString("birla.account.salt")
  val ENC_PWD = configuration.underlying.getString("birla.account.encrytPwd")
  val IV_VALUE = configuration.underlying.getString("birla.account.ivValue")

  /**
    *
    * @param orderModel
    * @param order
    * @param subOrder
    * @param subOrderModel
    * @param userLoginObject
    * @return
    */
  override def placeSubOrders(orderModel: OrderModel, order: FcomtRow, subOrder: FcsotRow, subOrderModel: SubOrder, orderStateChangeMap: mutable.HashMap[Long, List[Long]], userLoginObject: UserLoginObject): Future[ProcessedSubOrderModel] = {
    val ipAddress = filterIpAddress(orderModel.ipAddress)
    val userName = userLoginObject.username.getOrElse("")
    val subOrderId = subOrder.id
    val soptrfNum = subOrder.sotsoptrfnum
    val investmentMode = subOrderModel.investmentMode
    val schemeDisplayName = None
    val schemePlan = None
    val schemeOption = None
    val folioNo = subOrderModel.folioNo.getOrElse("").trim
    val amount = subOrderModel.amount
    val units = subOrderModel.quantity.getOrElse(0.0)
    var isSuccess = false
    var connectionFail = false
    var refno: Option[String] = None
    val redFlag = REDEMPTION_PARTIAL
    var unitAmtFlag = AMOUNTUNIT_AMOUNT
    var unitAmtValue = 100.00
    val userId = userLoginObject.userid.get
    val buySellTypeName = orderHelper.getBuySellTypeName(order.omtbuysell, subOrder.sotbuyselltype)
    val targetHost = new HttpHost(URL_HOST, 443, "https")
    val authCache = new BasicAuthCache()
    val basicAuth = new BasicScheme()
    authCache.put(targetHost, basicAuth)
    val context = HttpClientContext.create()
    context.setAuthCache(authCache)
    val checsumInstance = new Checksum(IV_VALUE);
    if (amount.nonEmpty) {
      unitAmtFlag = AMOUNTUNIT_AMOUNT
      unitAmtValue = amount.get
    }
    if (amount.isEmpty && units > 0.0) {
      unitAmtFlag = AMOUNTUNIT_UNIT
      unitAmtValue = units
    }
    schemeRepository.getSchemeOptionById(soptrfNum).flatMap(soptrow => {

      val schemeCode = soptrow.soptrtacode.get

      getIRDetailsForFolio(subOrderId, folioNo, ipAddress, targetHost, context, checsumInstance, userName, userId, soptrow.soptsmtrfnum).flatMap(irresponse => {
        val bankSuccess = (irresponse \ "success").as[Boolean]
        if (bankSuccess) {
          val IRDetails = (irresponse \ "response").as[JsValue]
          val bankDetails = (IRDetails \ "BankDetails").as[Seq[JsValue]]
          val IRHeaderId = (IRDetails \ "IRHeaderId").as[JsValue].asOpt[String]
          val schemeDetails = (IRDetails \ "SchemeDetails").as[JsArray]
          val bankName = (bankDetails.head \ "BankName").as[JsValue].asOpt[String]
          val branchName = (bankDetails.head \ "BranchName").as[JsValue].asOpt[String]
          val branchCity = (bankDetails.head \ "BranchCity").as[JsValue].asOpt[String]
          val accountNumber = (bankDetails.head \ "AccountNumber").as[JsValue].asOpt[String]
          val accountType = (bankDetails.head \ "AccountType").as[JsValue].asOpt[String]
          val IFSCCode = (bankDetails.head \ "IFSCCode").as[JsValue].asOpt[String]
          val payoutID = (bankDetails.head \ "PayoutID").as[JsValue].asOpt[String]


          val withdrawalRequest = Json.obj(
            "UserId" -> USER_ID,
            "Password" -> PASSWORD,
            "FolioNo" -> folioNo,
            "SchemeCode" -> schemeCode,
            "Amount" -> unitAmtValue,
            "Bank_Name" -> bankName.get,
            "BranchName" -> branchName.get,
            "BankCity" -> branchCity.get,
            "AccNo" -> accountNumber.get,
            "AccType" -> accountType.get,
            "IFSCcode" -> IFSCCode.get,
            "RedeemPayout" -> payoutID.get,
            "IsInstantRedemption" -> Y_FLAG,
            "IRHeaderId" -> IRHeaderId.get,
            "IsIRMAXAmount" -> N_FLAG, // Y_FLAG
            "ClientIpAddress" -> ipAddress.get,
            "OS" -> "Test",
            "IMEI" -> "Test",
            "UDP" -> "UDP",
            "UDP1" -> "",
            "UDP2" -> "",
            "UDP3" -> "",
            "UDP4" -> "",
            "UDP5" -> ""
          )
          val dateTimeStamp = new java.util.Date
          val headerDateTimeStamp: String = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(dateTimeStamp)
          val formatedDateTimeStamp: String = new SimpleDateFormat("ddMMyyHHmmss").format(dateTimeStamp)

          val jsonObj = Json.obj("WithdrawalAmountRequest" -> withdrawalRequest)
          val inputString: String = folioNo + "|" + unitAmtValue.toLong + "|" + soptrow.soptrtacode.get + "|" + IFSCCode.get + "|" + Y_FLAG + "|"
          logger.debug("INPUTString >>> " + inputString)
          val httpClient = bslPoolingClient.getHttpClient
          val httpPost = new HttpPost(REDEMPTION_URL)
          val newSalt = checsumInstance.GenerateSaltValue(inputString, SALT, formatedDateTimeStamp)
          logger.debug("NEW SALT >> " + newSalt)
          logger.debug("TS >> " + formatedDateTimeStamp)
          val checksum = checsumInstance.Encrypt(inputString, newSalt, ENC_PWD)
          logger.debug("CHECKSUM >> " + checksum)
          httpPost.addHeader("Content-type", "application/json")
          httpPost.addHeader("Authorization", "Bearer " + API_KEY)
          httpPost.addHeader("Checksum", checksum)
          httpPost.addHeader("DateTimeStamp", headerDateTimeStamp)
          httpPost.setEntity(new StringEntity(Json.stringify(jsonObj)))
          var camsTrNo, camsReturnCode, camsReturnMsg, camsResponseTime, entryDate, IMPSReturnMsg, IMPSStatus, returnMsg, tradeDate, udp: Option[String] = None

          // Logging request
          val reqParamNameList: ListBuffer[String] = ListBuffer[String]()
          val reqParamValueList: ListBuffer[String] = ListBuffer[String]()
          for (jsonField <- withdrawalRequest.fields) {
            reqParamNameList.+=(jsonField._1)
            reqParamValueList.+=(Json.stringify(jsonField._2))
          }
          integrationRepository.saveRequestParameters(BSL_INTEGRATION_NAME, Some(subOrderId.toString), reqParamNameList, reqParamValueList, userName)

          try {
            val httpResponse = httpClient.execute(targetHost, httpPost, context)
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
              val inputStream = httpResponse.getEntity.getContent
              val res: JsValue = Json.parse(inputStream)
              logger.debug("OUTPUT >> " + Json.stringify(res))
              val returnCode = (res \ "ReturnCode").as[JsString]
              val rcCodeOption = returnCode.asOpt[String]
              if (rcCodeOption.nonEmpty && rcCodeOption.get.equalsIgnoreCase("1")) {
                isSuccess = true
              }
              camsTrNo = (res \ "CAMSTransactionNo").as[JsValue].asOpt[String]
              camsReturnCode = (res \ "CAMS_ReturnCode").as[JsValue].asOpt[String]
              camsReturnMsg = (res \ "CAMS_ReturnMsg").as[JsValue].asOpt[String]
              camsResponseTime = (res \ "Cams_Response_Time").as[JsValue].asOpt[String]
              entryDate = (res \ "EntryDate").as[JsValue].asOpt[String]
              IMPSReturnMsg = (res \ "IMPS_ReturnMsg").as[JsValue].asOpt[String]
              IMPSStatus = (res \ "IMPS_Status").as[JsValue].asOpt[String]
              returnMsg = (res \ "ReturnMsg").as[JsValue].asOpt[String]
              tradeDate = (res \ "TradeDate").as[JsValue].asOpt[String]
              refno = (res \ "TransactionReferenceNo").as[JsValue].asOpt[String]
              udp = (res \ "UDP").as[JsValue].asOpt[String]

              // Logging response
              val resParamNameList: ListBuffer[String] = ListBuffer[String]()
              val resParamValueList: ListBuffer[String] = ListBuffer[String]()
              resParamNameList.+=("CAMSTransactionNo", "CAMS_ReturnCode", "CAMS_ReturnMsg", "Cams_Response_Time", "EntryDate", "IMPS_ReturnMsg", "IMPS_Status", "TradeDate", "TransactionReferenceNo", "UDP")
              resParamValueList.+=(camsTrNo.getOrElse(""), camsReturnCode.getOrElse(""), camsReturnMsg.getOrElse(""), camsResponseTime.getOrElse(""), entryDate.getOrElse(""), IMPSReturnMsg.getOrElse(""), IMPSStatus.getOrElse(""), tradeDate.getOrElse(""), refno.getOrElse("0"), udp.getOrElse(""))
              integrationRepository.saveResponseParameters(BSL_INTEGRATION_NAME, Some(subOrderId.toString), resParamNameList, resParamValueList, userName)
            } else {
              logger.debug(" HTTP  RESPONSE CODE [" + httpResponse.getStatusLine.getStatusCode + "]")
            }
          }
          catch {
            case s: SocketTimeoutException => {
              logger.error("Socket Timeout Exception >> ", s)
              isSuccess = true;
              refno = Some("-1")
            }
            case c: java.net.ConnectException => {
              logger.error("Connection Time Out Occurred >> ", c);
              connectionFail = true
            }
            case io: IOException => {
              logger.error("IO exception >> ", io)
            }
          }
          if (isSuccess) {
            // Update Transaction Mode in SOT
            orderRepository.updateTransactionModeForSuborder(subOrderId, BIRLA_TRNSACTION_MODE).map(value => {
              orderRepository.updateSubOrderTxnId(subOrder.id, order.id, subOrder.sotostmstaterfnum, camsTrNo.getOrElse("-1"), userName).map(updated => {
                updateSubOrderState(subOrder, order, PLACED_AT_AMC, orderStateChangeMap, ORDER_STATUS_SUCCESS, userLoginObject)
              })
              ProcessedSubOrderModel(subOrderId, ORDER_STATUS_SUCCESS, soptrfNum, investmentMode, orderModel.orderType, buySellTypeName, schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
            })
          } else {
            updateSubOrderState(subOrder, order, ORDER_FAILED, orderStateChangeMap, ORDER_STATUS_FAILURE, userLoginObject).flatMap(stateUpdated => {
              if (connectionFail) {
                Future {
                  ProcessedSubOrderModel(subOrderId, ORDER_CONNECTIVITY_FAILURE, soptrfNum, investmentMode, orderModel.orderType, buySellTypeName, schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
                }
              }
              else {
                orderRepository.updateTransactionModeForSuborder(subOrderId, BIRLA_TRNSACTION_MODE).map(trValue => {
                  ProcessedSubOrderModel(subOrderId, ORDER_STATUS_FAILURE, soptrfNum, investmentMode, orderModel.orderType, buySellTypeName, schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
                })
              }
            })
          }
        }
        else {
          println("Error in GETTING BANK Request >> TRANSACTION MODE TO REMAIN AS BSE")
          updateSubOrderState(subOrder, order, ORDER_FAILED, orderStateChangeMap, ORDER_STATUS_FAILURE, userLoginObject).map(stateUpdated => {
            ProcessedSubOrderModel(subOrderId, ORDER_STATUS_FAILURE, soptrfNum, investmentMode, orderModel.orderType, buySellTypeName, schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
          })
        }
      })
    })
  }

  /**
    *
    * @param ipAddress
    * @return
    */
  def filterIpAddress(ipAddress: Option[String]): Option[String] = {
    if (ipAddress.get.startsWith("10.1")) {
      Some(IP)
    } else {
      ipAddress
    }
  }

  /**
    *
    * @param folioNo
    * @param ipAddress
    * @param targetHost
    * @param context
    * @param checksumInstance
    * @return
    */
  def getIRDetailsForFolio(subOrderId: Long, folioNo: String, ipAddress: Option[String], targetHost: HttpHost, context: HttpClientContext, checksumInstance: Checksum, userName: String, userId: Long, smtid: Long): Future[JsValue] = {
    folioDetails(subOrderId, folioNo, ipAddress, targetHost, context, checksumInstance, userName, userId, smtid).map(folioresponse => {
      val taxCodeValid = (folioresponse \ "success").as[Boolean]
      if (taxCodeValid) {
        val dateTimeStamp = new java.util.Date
        val headerDateTimeStamp: String = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(dateTimeStamp)
        val formatedDateTimeStamp: String = new SimpleDateFormat("ddMMyyHHmmss").format(dateTimeStamp)
        val folioSchemeRequest = Json.obj(
          "FolioNo" -> folioNo,
          "UserId" -> USER_ID,
          "Password" -> PASSWORD,
          "UDP" -> "",
          "UDP1" -> "",
          "UDP2" -> "",
          "UDP3" -> "",
          "UDP4" -> "",
          "UDP5" -> "",
          "OS" -> "Test",
          "IMEI" -> "Test",
          "ClientIpAddress" -> ipAddress.get
        )
        val jsonObj = Json.obj("FolioSchemeRequest" -> folioSchemeRequest)
        val inputString = folioNo
        val httpClient = bslPoolingClient.getHttpClient
        val httpPost = new HttpPost(IR_URL)
        val newSalt = checksumInstance.GenerateSaltValue(inputString, SALT, formatedDateTimeStamp)
        logger.debug("inputString >>> " + inputString)
        logger.debug("NEW SALT >> " + newSalt)
        logger.debug("TS >> " + formatedDateTimeStamp)
        val checksum = checksumInstance.Encrypt(inputString, newSalt, ENC_PWD)
        logger.debug("CHECKSUM >> " + checksum)
        httpPost.addHeader("Content-type", "application/json")
        httpPost.addHeader("Authorization", "Bearer " + API_KEY)
        httpPost.addHeader("Checksum", checksum)
        httpPost.addHeader("DateTimeStamp", headerDateTimeStamp)
        httpPost.setEntity(new StringEntity(Json.stringify(jsonObj)))
        println("Get IRDetails HttpPost >> ", httpPost)

        // Logging Request
        val reqParamNameListIR: ListBuffer[String] = ListBuffer[String]()
        val reqParamValueListIR: ListBuffer[String] = ListBuffer[String]()
        for (jsonField <- folioSchemeRequest.fields) {
          reqParamNameListIR.+=(jsonField._1)
          reqParamValueListIR.+=(Json.stringify(jsonField._2))
        }
        integrationRepository.saveRequestParameters(BSL_INTEGRATION_NAME, Some(subOrderId.toString), reqParamNameListIR, reqParamValueListIR, userName)

        try {
          val httpResponse = httpClient.execute(targetHost, httpPost, context)
          if (httpResponse.getStatusLine().getStatusCode() == 200) {
            val inputStream = httpResponse.getEntity.getContent
            val res: JsValue = Json.parse(inputStream)
            logger.debug("OUTPUT >> " + Json.stringify(res))
            val returnCode = (res \ "ReturnCode").as[JsString]
            val rcCodeOption = returnCode.asOpt[String]
            if (rcCodeOption.nonEmpty && rcCodeOption.get.equalsIgnoreCase("1")) {
              logger.debug("getIRDetailsForFolio success")
              // Logging IR response
              val resParamNameListIR: ListBuffer[String] = ListBuffer[String]()
              val resParamValueListIR: ListBuffer[String] = ListBuffer[String]()
              resParamNameListIR.+=("ReturnCode")
              resParamValueListIR.+=(rcCodeOption.get)
              integrationRepository.saveResponseParameters(BSL_INTEGRATION_NAME, Some(subOrderId.toString), resParamNameListIR, resParamValueListIR, userName)
              Json.obj("success" -> true, "response" -> res)
            }
            else {
              logger.debug("getIRDetailsForFolio RETURNCODE NOT '1'")
              Json.obj("success" -> false)
            }
          } else {
            logger.debug(" HTTP  RESPONSE CODE [" + httpResponse.getStatusLine.getStatusCode + "]")
            Json.obj("success" -> false)
          }
        } catch {
          case e: Exception => {
            logger.error("exception >> ", e); Json.obj("success" -> false)
          }
        }
      } else {
        Json.obj("success" -> false)
      }
    })
  }

  /**
    *
    * @param folioNo
    * @param ipAddress
    * @param targetHost
    * @param context
    * @param checksumInstance
    * @return
    */
  def folioDetails(subOrderId: Long, folioNo: String, ipAddress: Option[String], targetHost: HttpHost, context: HttpClientContext, checksumInstance: Checksum, userName: String, userId: Long, smtid: Long): Future[JsValue] = {
    val dateTimeStamp = new java.util.Date
    val headerDateTimeStamp: String = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(dateTimeStamp)
    val formatedDateTimeStamp: String = new SimpleDateFormat("ddMMyyHHmmss").format(dateTimeStamp)
    val folioDetails = Json.obj(
      "UserId" -> USER_ID,
      "Password" -> PASSWORD,
      "FolioNumber" -> folioNo,
      "UDP" -> "",
      "Source" -> "",
      "ClientIpAddress" -> ipAddress.get,
      "OS" -> "",
      "IMEI" -> "",
      "UDP1" -> "",
      "UDP2" -> "",
      "UDP3" -> "",
      "UDP4" -> "",
      "UDP5" -> ""
    )
    val jsonObj = Json.obj("FolioDetails" -> folioDetails)
    val inputString = folioNo

    collection(BANK_API_COLLECTION_NAME).flatMap(coll => {
      val findQuery = BSONDocument("userId" -> userId, "folioNo" -> folioNo, "smtid" -> smtid)
      coll.find(findQuery).one[BSONDocument].map(doc => {
        if (doc.isEmpty) {
          val newSalt = checksumInstance.GenerateSaltValue(inputString, SALT, formatedDateTimeStamp)
          val httpClient = bslPoolingClient.getHttpClient
          val httpPost = new HttpPost(FOLIO_URL)
          logger.debug("inputstring >>> " + inputString)
          logger.debug("NEW SALT >> " + newSalt)
          logger.debug("TS >> " + formatedDateTimeStamp)
          val checksum = checksumInstance.Encrypt(inputString, newSalt, ENC_PWD)
          logger.debug("CHECKSUM >> " + checksum)
          httpPost.addHeader("Content-type", "application/json")
          httpPost.addHeader("Authorization", "Bearer " + API_KEY)
          httpPost.addHeader("Checksum", checksum)
          httpPost.addHeader("DateTimeStamp", headerDateTimeStamp)
          httpPost.setEntity(new StringEntity(Json.stringify(jsonObj)))
          println("FolioDetails HttpPost >> ", httpPost)

          val reqParamNameListFolio: ListBuffer[String] = ListBuffer[String]()
          val reqParamValueListFolio: ListBuffer[String] = ListBuffer[String]()
          for (jsonField <- folioDetails.fields) {
            reqParamNameListFolio.+=(jsonField._1)
            reqParamValueListFolio.+=(Json.stringify(jsonField._2))
          }
          integrationRepository.saveRequestParameters(BSL_INTEGRATION_NAME, Some(subOrderId.toString), reqParamNameListFolio, reqParamValueListFolio, userName)

          try {
            val httpResponse = httpClient.execute(targetHost, httpPost, context)
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
              val inputStream = httpResponse.getEntity.getContent
              val res: JsValue = Json.parse(inputStream)
              logger.debug("OUTPUT >> " + Json.stringify(res))
              val returnCode = (res \ "ReturnCode").as[JsString]
              val taxStatusCode = (res \ "TaxStatusCode").as[JsValue]
              val rcCodeOption = returnCode.asOpt[String]
              val taxStatusCodeOption = taxStatusCode.asOpt[String]

              // Logging Folio response
              val resParamNameListFolio: ListBuffer[String] = ListBuffer[String]()
              val resParamValueListFolio: ListBuffer[String] = ListBuffer[String]()
              resParamNameListFolio.+=("ReturnCode", "TaxStatusCode")
              resParamValueListFolio.+=(rcCodeOption.get, taxStatusCodeOption.get)
              integrationRepository.saveResponseParameters(BSL_INTEGRATION_NAME, Some(subOrderId.toString), resParamNameListFolio, resParamValueListFolio, userName)

              // Save TaxStatus in Mongo
              val modifier = BSONDocument(
                "_id" -> BSONObjectID.generate(),
                "userId" -> userId,
                "folioNo" -> folioNo,
                "smtid" -> smtid,
                "response" -> Json.stringify(res),
                "fetchDate" -> DateTimeUtils.getCurrentDate()
              )

              val writeRes: Future[WriteResult] = mongoDbService.insertDoc(coll, modifier)
              writeRes.onComplete {
                case Failure(e) => {
                  logger.error("Mongo Error :: " + e.getMessage + " in saving TaxStatus" + " for : " + userId)
                }
                case Success(writeResult) => {
                  logger.debug("successfully inserted document with result TaxStatus" + " for : " + userId)
                }
              }

              if (rcCodeOption.nonEmpty && rcCodeOption.get.equalsIgnoreCase("1") && taxStatusCodeOption.nonEmpty && (taxStatusCodeOption.get.equalsIgnoreCase("01"))) {
                logger.debug("folioDetails success")
                Json.obj("success" -> true, "response" -> res)
              }
              else {
                logger.debug("FolioDetails: Return code NOT '1' or Tax Status NOT '01'")
                Json.obj("success" -> false)
              }
            }
            else {
              logger.debug(" HTTP  RESPONSE CODE [" + httpResponse.getStatusLine.getStatusCode + "]")
              Json.obj("success" -> false)
            }
          } catch {
            case e: Exception => {
              logger.error("exception >> ", e)
              Json.obj("success" -> false)
            }
          }
        }
        else {
          logger.debug("Doc found in Mongo")
          val res = doc.get.getAs[String]("response")
          if (res.nonEmpty){
            val returnCode = (Json.parse(res.get) \ "ReturnCode").as[JsString]
            val taxStatusCode = (Json.parse(res.get) \ "TaxStatusCode").as[JsValue]
            val rcCodeOption = returnCode.asOpt[String]
            val taxStatusCodeOption = taxStatusCode.asOpt[String]
            if (rcCodeOption.nonEmpty && rcCodeOption.get.equalsIgnoreCase("1") && taxStatusCodeOption.nonEmpty && (taxStatusCodeOption.get.equalsIgnoreCase("01"))) {
              Json.obj("success" -> true, "response" -> res)
            }
            else {
              logger.debug("FolioDetails: Return code NOT '1' or Tax Status NOT '01'")
              Json.obj("success" -> false)
            }
          }
          else {
            logger.debug("'response' is empty in mongo")
            Json.obj("success" -> false)
          }
        }
      })
    })
  }

  /**
    *
    * @return
    */
  def getIRDetails = Action.async(parse.json) { request => {
    val ipAddress = filterIpAddress(Some(RequestUtils.getIpAddress(request)))
    val folioNo = (request.body \ "folioNo").as[String]
    val folioSchemeRequest = Json.obj(
      "FolioNo" -> folioNo,
      "UserId" -> USER_ID,
      "Password" -> PASSWORD, // "QVJOMTEyMzU4", //PASSWORD,
      "UDP" -> "",
      "UDP1" -> "",
      "UDP2" -> "",
      "UDP3" -> "",
      "UDP4" -> "",
      "UDP5" -> "",
      "OS" -> "Test",
      "IMEI" -> "Test",
      "ClientIpAddress" -> ipAddress.get
    )
    val jsonObj = Json.obj("FolioSchemeRequest" -> folioSchemeRequest)
    val inputString = folioNo
    val httpClient = bslPoolingClient.getHttpClient
    //    val targetHost = new HttpHost("mflive.birlasunlife.com", 443, "https")
    val targetHost = new HttpHost(URL_HOST, 443, "https")
    val authCache = new BasicAuthCache()
    val basicAuth = new BasicScheme()
    authCache.put(targetHost, basicAuth)
    val context = HttpClientContext.create()
    context.setAuthCache(authCache)
    //    val httpPost = new HttpPost("/LRedeemService.svc/GetIRDetailsForFolio")
    val httpPost = new HttpPost(IR_URL)
    val dateTimeStamp = new java.util.Date
    val headerDateTimeStamp: String = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(dateTimeStamp)
    val formatedDateTimeStamp: String = new SimpleDateFormat("ddMMyyHHmmss").format(dateTimeStamp)
    //    val checksumInstance = new Checksum("e675f725e675f112")
    val checksumInstance = new Checksum(IV_VALUE)
    //    val newSalt = checksumInstance.GenerateSaltValue(inputString, "FB1DF878-0B4B-481B-A90A-B575FC1FC358", formatedDateTimeStamp)
    val newSalt = checksumInstance.GenerateSaltValue(inputString, SALT, formatedDateTimeStamp)
    logger.debug("NEW SALT >> " + newSalt)
    logger.debug("TS >> " + formatedDateTimeStamp)
    //    val checksum = checksumInstance.Encrypt(inputString, newSalt, "Pas$@w0rd23")
    val checksum = checksumInstance.Encrypt(inputString, newSalt, ENC_PWD)
    logger.debug("CHECKSUM >> " + checksum)
    httpPost.addHeader("Content-type", "application/json")
    //    httpPost.addHeader("Authorization", "Bearer " + "e2b4aad5-4057-31ef-a7d9-cd8ff17c3190")
    httpPost.addHeader("Authorization", "Bearer " + API_KEY)
    httpPost.addHeader("Checksum", checksum)
    httpPost.addHeader("DateTimeStamp", headerDateTimeStamp)
    httpPost.setEntity(new StringEntity(Json.stringify(jsonObj)))
    folioDetails(0, folioNo, ipAddress, targetHost, context, checksumInstance, "user_name", 1, 0).map(folioresponse => {
      val taxCodeValid = (folioresponse \ "success").as[Boolean]
      //    if (taxCodeValid){
      try {
        val httpResponse = httpClient.execute(targetHost, httpPost, context)
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
          val inputStream = httpResponse.getEntity.getContent
          val res: JsValue = Json.parse(inputStream)
          logger.debug("OUTPUT >> " + Json.stringify(res))
          val returnCode = (res \ "ReturnCode").as[JsString]
          val rcCodeOption = returnCode.asOpt[String]
          if (rcCodeOption.nonEmpty && rcCodeOption.get.equalsIgnoreCase("1")) {
            val bankDetails = (res \ "BankDetails").as[JsArray]
            val IRHeaderId = (res \ "IRHeaderId").as[JsString]
            val IRPerDayAmt = (res \ "IRPerDayAmount").as[JsNumber]
            val returnMsg = (res \ "ReturnMsg").as[JsString]
            val schemeDetails = (res \ "SchemeDetails").as[JsValue]
            val udp = (res \ "UDP").as[JsString]
            Ok(res)
          }
          else {
            Ok(Json.obj("success" -> false))
          }
        } else {
          logger.debug(" HTTP  RESPONSE CODE [" + httpResponse.getStatusLine.getStatusCode + "]")
          Ok(Json.obj("success" -> false))
        }
      } catch {
        case e: Exception => {
          logger.error("exception >> ", e); Ok(Json.obj("success" -> false))
        }
      }
      /*}
      else {
        Ok(Json.obj("success" -> false, "error" -> "Error while getting folio details"))
      }*/
    })
  }}
}