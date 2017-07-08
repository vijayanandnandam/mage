package service.integration

import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.{Inject, Named, Singleton}

import constants.{DBConstants, IntegrationConstants, MongoConstants, OrderConstants}
import data.model.Tables.{FcomtRow, FcsotRow}
import helpers.{OrderHelper, PoolingClientConnectionManager}
import models.{OrderModel, ProcessedSubOrderModel, SubOrder, UserLoginObject}
import org.apache.http.NameValuePair
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.message.BasicNameValuePair
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.api.libs.ws.WSClient
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONValue}
import reactivemongo.play.json.BSONFormats
import repository.module.{BankRepository, IntegrationRepository, OrderRepository, SchemeRepository}
import service.{AMCService, BankService, MongoDbService, SchemeService}
import utils.DateTimeUtils

import scala.collection.mutable.ListBuffer
import scala.collection.{JavaConversions, mutable}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by Fincash on 26-04-2017.
  */

@Singleton
class RelianceIntegrationServiceImpl @Inject()(implicit ec: ExecutionContext, configuration: play.api.Configuration, ws: WSClient,
                                               bseStarOrderEntryService: BSEStarOrderEntryServiceImpl, mongoDbService: MongoDbService,
                                               bseUploadService: BSEStarUploadServiceImpl,orderHelper: OrderHelper,
                                               @Named("externalPoolingClientConnectionManager") relPoolingClient: PoolingClientConnectionManager,
                                               orderRepository: OrderRepository, integrationRepository: IntegrationRepository,
                                               schemeService: SchemeService, aMCService: AMCService, schemeRepository: SchemeRepository,
                                               bankRepository: BankRepository, bankService: BankService) extends IntegrationService(orderHelper,orderRepository)
  with DBConstants with IntegrationConstants with OrderConstants with MongoConstants{

  val logger, log = LoggerFactory.getLogger(classOf[RelianceIntegrationServiceImpl])

  def collection(name: String) =  mongoDbService.collection(name)

  val ARNCODE = configuration.underlying.getString("fincash.arncode")
  val URL = configuration.underlying.getString("reliance.redemption.url")
  val BANK_URL = configuration.underlying.getString("reliance.bankdetail.url")
  val FUND = configuration.underlying.getString("reliance.Fund")
  val API_KEY = configuration.underlying.getString("reliance.redemption.apikey")
  val DEVICE_ID = configuration.underlying.getString("reliance.account.deviceid")
  val APP_NAME = configuration.underlying.getString("reliance.account.appName")
  val APP_VERSION = configuration.underlying.getString("reliance.account.appVersion")
  val IP = configuration.underlying.getString("reliance.account.ip")


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
    val trDate = DateTimeUtils.convertDateToFormat(DateTimeUtils.getCurrentDate(), "MM/dd/yyyy")
    val entDate = DateTimeUtils.convertDateToFormat(DateTimeUtils.getCurrentDate(), "MM/dd/yyyy")
    val ipAddress = orderModel.ipAddress.getOrElse(IP)
    val userName = userLoginObject.username.getOrElse("")
    val userId = userLoginObject.userid.get
    val subOrderId = subOrder.id
    val soptrfNum = subOrder.sotsoptrfnum
    val investmentMode = subOrderModel.investmentMode
    val schemeDisplayName = None
    val schemePlan = None
    val schemeOption = None
    val buySellTypeName = orderHelper.getBuySellTypeName(order.omtbuysell, subOrder.sotbuyselltype)
    val folioNo = subOrderModel.folioNo.getOrElse("")
    val amount = subOrderModel.amount
    val units = subOrderModel.quantity.getOrElse(0.0)
    var bank : Option[String] = None
    var acNo: Option[String] = None
    var isSuccess = false
    var connectionFail = false
    var refno : Option[String] = None
    val httpClient = relPoolingClient.getHttpClient
    val redFlag = REDEMPTION_PARTIAL
    var unitAmtFlag = AMOUNTUNIT_AMOUNT
    var unitAmtValue = 100.00

    if (amount.nonEmpty) {
      unitAmtFlag = AMOUNTUNIT_AMOUNT
      unitAmtValue = amount.get
    }
    if (amount.isEmpty && units > 0.0) {
      unitAmtFlag = AMOUNTUNIT_UNIT
      unitAmtValue = units
    }

    val redemptionUrl: String = URL
    val bankUrl: String = BANK_URL
    val paramList: ListBuffer[NameValuePair] = ListBuffer[NameValuePair]()
    val bankParamList: ListBuffer[NameValuePair] = ListBuffer[NameValuePair]()
    getRelDefaultParameters().flatMap(parameterMap =>{
      schemeRepository.getSchemeOptionById(soptrfNum).flatMap(soptrow => {
        val schemeCode = soptrow.soptschemecode
        val planCode = soptrow.soptplancode
        val optionCode = soptrow.soptoptioncode
        parameterMap.foreach(pair => {
          paramList.+=(new BasicNameValuePair(pair._1, pair._2))
          bankParamList.+=(new BasicNameValuePair(pair._1, pair._2))
        })
        paramList.update(1, new BasicNameValuePair("scheme", schemeCode.getOrElse("LF")))
        paramList.update(2, new BasicNameValuePair("plan", planCode.getOrElse("IG")))
        paramList.update(3, new BasicNameValuePair("options", optionCode.getOrElse("G")))
        bankParamList.+=(new BasicNameValuePair(REL_ACNO_KEY, folioNo))

        collection(BANK_API_COLLECTION_NAME).flatMap(coll => {
          val findQuery = BSONDocument("userId" -> userId, "folioNo" -> folioNo, "smtid" -> soptrow.soptsmtrfnum)
          coll.find(findQuery).one[BSONDocument].flatMap(doc => {
            if (doc.isEmpty){
              val bankHttpGet = new HttpGet(bankUrl)
              val bankParameterUri = new URIBuilder(bankHttpGet.getURI).addParameters(JavaConversions.seqAsJavaList[NameValuePair](bankParamList.toList)).build()
              bankHttpGet.setURI(bankParameterUri)
              //Logging bank parameters
              val reqParamNameListBank: ListBuffer[String] = ListBuffer[String]()
              val reqParamValueListBank: ListBuffer[String] = ListBuffer[String]()
              for(parameters <- paramList){
                reqParamNameListBank.+=(parameters.getName)
                reqParamValueListBank.+=(parameters.getValue)
              }
              integrationRepository.saveRequestParameters(RELIANCE_INTEGRATION_NAME, Some(subOrderId.toString), reqParamNameListBank, reqParamValueListBank, userName)
              //        println("BankHttpGet >> ", bankHttpGet)
              try {
                val httpResponse = httpClient.execute(bankHttpGet)
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                  val inputStream = httpResponse.getEntity.getContent
                  /*val lines = Source.fromInputStream(inputStream).getLines*/
                  val res: JsValue = Json.parse(inputStream)
                  logger.debug("Bank OUTPUT >> {}", Json.stringify(res))
                  var isError = false
                  val isErrorUndefined = (res \ "error").isInstanceOf[JsUndefined]
                  if(!isErrorUndefined){
                    val errorCode = (res \ "error").as[JsValue].asOpt[Long]
                    val errorMessage = (res \ "message").as[JsValue].asOpt[String]
                    logger.debug("API ERROR " + errorMessage.getOrElse(""))
                    if(errorCode.nonEmpty && errorCode.get<0){
                      isError = true
                    }
                  }
                  if(isError){
                    connectionFail = true
                    isSuccess = false
                  }else{
                    bank = (res.asInstanceOf[JsArray].value(0) \ "BankName").as[JsValue].asOpt[String]
                    acNo = (res.asInstanceOf[JsArray].value(0) \ "BankAccNo").as[JsValue].asOpt[String]
                    val resParamNameListBank: ListBuffer[String] = ListBuffer[String]()
                    val resParamValueListBank: ListBuffer[String] = ListBuffer[String]()
                    resParamNameListBank.+=("BankName", "BankAccNo", "Return_code")
                    resParamValueListBank.+=(bank.getOrElse(""), acNo.getOrElse(""))
                    integrationRepository.saveResponseParameters(RELIANCE_INTEGRATION_NAME, Some(subOrderId.toString), resParamNameListBank, resParamValueListBank, userName)
                    if (bank.nonEmpty && acNo.nonEmpty){
                      // Save bank in Mongo
                        val modifier = BSONDocument(
                          "_id" -> BSONObjectID.generate(),
                          "userId" -> userId,
                          "folioNo" -> folioNo,
                          "smtid" -> soptrow.soptsmtrfnum,
                          "fetchDate" -> DateTimeUtils.getCurrentDate(),
                          "bankName" -> bank.get,
                          "BankAccNo" -> acNo.get,
                          "bank" -> Json.stringify(res)
                        )
                        val writeRes: Future[WriteResult] = mongoDbService.insertDoc(coll, modifier)
                        writeRes.onComplete {
                          case Failure(e) => {
                            logger.error("Mongo Error :: " + e.getMessage + " in saving >>> " + bank.get + " for : " + userId)
                          }
                          case Success(writeResult) => {
                            logger.debug("successfully inserted document with result " + bank.get + " for : " + userId)
                          }
                        }
                    }
                  }
                }else{
                  logger.debug(" HTTP  RESPONSE CODE ["+ httpResponse.getStatusLine.getStatusCode+"]")
                }
              }
              catch {
                case s: SocketTimeoutException => {
                  logger.error("Socket Timeout Exception in Getting bank details>> ", s)
                }
                case c: java.net.ConnectException => { logger.error("Connection Time Out Occurred in get Bank details>> ", c); }
                case io: IOException => { logger.error("IO exception in get bank details>> ", io)}
                case e: Exception => { logger.error("exception in Get Bank details>> ", e)}
              }
            }
            else {
              logger.debug("Doc found in Mongo")
//              bank = (BSONFormats.BSONDocumentFormat.writes(doc.get).as[JsObject] \ "bankName").asOpt[String]
              bank = doc.get.getAs[String]("bankName")
            }

            // If BankName is empty
            if(bank.nonEmpty){
              paramList.+=(new BasicNameValuePair(REL_ACNO_KEY, folioNo))
              paramList.+=(new BasicNameValuePair("RedFlag", redFlag))
              paramList.+=(new BasicNameValuePair("UnitamtFlag", unitAmtFlag))
              paramList.+=(new BasicNameValuePair("UnitAmtValue", String.valueOf(unitAmtValue)))
              paramList.+=(new BasicNameValuePair("bank", bank.get))
              paramList.+=(new BasicNameValuePair("trdate", trDate))
              paramList.+=(new BasicNameValuePair("entdate", entDate))
              paramList.+=(new BasicNameValuePair("ip", ipAddress))
              val httpGet = new HttpGet(redemptionUrl)
              val parameterUri = new URIBuilder(httpGet.getURI).addParameters(JavaConversions.seqAsJavaList[NameValuePair](paramList.toList)).build()
              var returnCode : Option[String] = Some("-100")
              httpGet.setURI(parameterUri)

              // Logging request
              val reqParamNameList: ListBuffer[String] = ListBuffer[String]()
              val reqParamValueList: ListBuffer[String] = ListBuffer[String]()
              for(parameters <- paramList){
                reqParamNameList.+=(parameters.getName)
                reqParamValueList.+=(parameters.getValue)
              }
              integrationRepository.saveRequestParameters(RELIANCE_INTEGRATION_NAME, Some(subOrderId.toString), reqParamNameList, reqParamValueList, userName)
              try {
                val httpResponse = httpClient.execute(httpGet)
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                  val inputStream = httpResponse.getEntity.getContent
                  val res: JsValue = Json.parse(inputStream)
                  logger.debug("OUTPUT >> " + Json.stringify(res))
                  refno = (res \ "REFNO").as[JsValue].asOpt[String]
                  val msg = (res \ "Return_Msg").as[JsValue].asOpt[String]
                  returnCode = (res \ "Return_code").as[JsValue].asOpt[String]
                  val amt = (res \ "Amount").as[JsValue].asOpt[String]
                  val units = (res \ "UNITS").as[JsValue].asOpt[String]
                  val date_time = (res \ "Date_Time").as[JsValue].asOpt[String]
                  val optionCode = (res \ "OptionCode").as[JsValue].asOpt[String]
                  val planCode = (res \ "PlanCode").as[JsValue].asOpt[String]
                  val schemCode = (res \ "SchemeCode").as[JsValue].asOpt[String]

                  if (!returnCode.get.equalsIgnoreCase("-100")) {
                    isSuccess = true
                  }
                  // Log response
                  val resParamNameList: ListBuffer[String] = ListBuffer[String]()
                  val resParamValueList: ListBuffer[String] = ListBuffer[String]()
                  resParamNameList.+=("REFNO", "Return_Msg", "Return_code", "Amount", "UNITS", "Date_Time", "OptionCode", "PlanCode", "SchemeCode")
                  resParamValueList.+=(refno.getOrElse("0"), msg.get, returnCode.get, amt.get, units.get, date_time.get, optionCode.get, planCode.get, schemCode.get)
                  integrationRepository.saveResponseParameters(RELIANCE_INTEGRATION_NAME, Some(subOrderId.toString), resParamNameList, resParamValueList, userName)
                }else{
                  logger.debug(" HTTP  RESPONSE CODE ["+ httpResponse.getStatusLine.getStatusCode+"]")
                }
              }
              catch {
                case s: SocketTimeoutException => {
                  logger.error("Socket Timeout Exception >> ", s)
                  isSuccess = true; refno = Some("-1")
                }
                case c: java.net.ConnectException => { logger.error("Connection Time Out Occurred >> ", c); connectionFail = true}
                case io: IOException => { logger.error("IO exception >> ", io)}
              }
            }

            // If success
            if (isSuccess) {
              // Update Transaction Mode in SOT
              orderRepository.updateTransactionModeForSuborder(subOrderId, RELIANCE_TRANSACTION_MODE).map(value => {
                orderRepository.updateSubOrderTxnId(subOrder.id, order.id, subOrder.sotostmstaterfnum, refno.getOrElse("0"), userName).map(updated =>{
                  updateSubOrderState(subOrder,order,PLACED_AT_AMC,orderStateChangeMap,ORDER_STATUS_SUCCESS,userLoginObject)
                  //orderHelper.notifyZendesk(subOrder,order,ORDER_STATUS_SUCCESS,userName,userId)
                })
                ProcessedSubOrderModel(subOrderId, ORDER_STATUS_SUCCESS, soptrfNum, investmentMode,orderModel.orderType, buySellTypeName,schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
              })
            }
            else {
              updateSubOrderState(subOrder,order,ORDER_FAILED,orderStateChangeMap,ORDER_STATUS_FAILURE,userLoginObject).flatMap(stateUpdated =>{
                if (connectionFail) {
                  Future{ProcessedSubOrderModel(subOrderId, ORDER_CONNECTIVITY_FAILURE, soptrfNum, investmentMode,orderModel.orderType, buySellTypeName,schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)}
                }
                else {
                  orderRepository.updateTransactionModeForSuborder(subOrderId, RELIANCE_TRANSACTION_MODE).map(trValue => {
                    ProcessedSubOrderModel(subOrderId, ORDER_STATUS_FAILURE, soptrfNum, investmentMode,orderModel.orderType, buySellTypeName,schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
                  })
                }
              })
              /*orderRepository.updateStateForSubOrder(subOrder.id, order.id, subOrder.sotostmstaterfnum,ORDER_FAILED, userName).map(updated =>{
                orderHelper.notifyZendesk(subOrder,order,ORDER_STATUS_FAILURE,userName,userId)
                if (connectionFail) {
                  ProcessedSubOrderModel(subOrderId, ORDER_CONNECTIVITY_FAILURE, soptrfNum, investmentMode,orderModel.orderType, buySellTypeName,schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
                }
                else {
                  ProcessedSubOrderModel(subOrderId, ORDER_STATUS_FAILURE, soptrfNum, investmentMode,orderModel.orderType, buySellTypeName,schemeDisplayName, schemePlan, schemeOption, amount, None, None, None, None)
                }
              })*/
            }
          })
        })
      })
    })
  }

  /**
    *
    * @param transactionId
    * @return
    */
  def getRelianceStatus(transactionId: String): Boolean = {
    val redemptionUrl: String = URL
    val parameterUrl = "fund=" + FUND + "&refno=" + transactionId + "&deviceid=" + DEVICE_ID  +  "&appVersion=" + APP_VERSION + "&appName=" + APP_NAME + "&apikey=" + API_KEY
    val finalUrl = redemptionUrl + "Getrefnostatus?" + parameterUrl

    val httpGet = new HttpGet(finalUrl)
    //httpGet.setURI(finalUrl)
    val httpClient = relPoolingClient.getHttpClient
    try {
      val httpResponse = httpClient.execute(httpGet)
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        val inputStream = httpResponse.getEntity.getContent
        val res: JsValue = Json.parse(inputStream)
        println("OUTPUT >> ", res)
        val returnMsg = (res \ "ReturnMsg").as[JsValue].asOpt[String]
        val returnCode = (res \ "ReturnCode").as[JsValue].asOpt[String]
        val instaStatus = (res \ "InstaStatus").as[JsValue].asOpt[String]
        val status = (res \ "Status").as[JsValue].asOpt[String]
        val instaRemarks = (res \ "InstaRemarks").as[JsValue].asOpt[String]

        return returnMsg.getOrElse("").toLowerCase() == "success"
//        returnMsg.compareToIgnoreCase("success")
      }else{
        logger.debug(" HTTP  RESPONSE CODE ["+ httpResponse.getStatusLine.getStatusCode+"]")
        false
      }
    }
    catch {
      case io: IOException => { logger.error("IO exception >> ", io); false}
    }
  }

  def getRelDefaultParameters(): Future[mutable.LinkedHashMap[String, String]] = {
    integrationRepository.getDefaultParamValuesForAPIs(RELIANCE_INTEGRATION_NAME)
  }
}