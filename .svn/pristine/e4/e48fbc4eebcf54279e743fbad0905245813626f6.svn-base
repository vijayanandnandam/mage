package controllers

import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.{Inject, Named}

import constants.MongoConstants
import helpers.{AuthenticatedAction, PoolingClientConnectionManager}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.{HttpHost, NameValuePair}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.{BasicAuthCache, BasicCredentialsProvider}
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import play.Configuration
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient}
import play.api.mvc.Controller
import service.{AttachmentService, MailService, MongoDbService}

import scala.collection.JavaConversions
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class MailGunController @Inject()(implicit val ec: ExecutionContext, ws: WSClient, attachService: AttachmentService, mgService: MailService,
                                  mongoDbService: MongoDbService, config: Configuration, auth: AuthenticatedAction,
                                  @Named("externalPoolingClientConnectionManager") mailgunPoolingClient: PoolingClientConnectionManager)
  extends Controller with MongoConstants {

  val logger , log = LoggerFactory.getLogger(classOf[MailGunController])
  def collection(name: String) =  mongoDbService.collection(name)
  /**
    *
    * Used to receive test data from the client side and send test.
    *
    * @return
    */
  /*def sendEmail = Action.async(parse.multipartFormData) { request =>

    val files = request.body.files
    val dataParts = request.body.dataParts
    val fileDetails = attachService.saveFile(files)
    val mailId = "";//mgService.sendMail(dataParts, fileDetails)
    val data = Json.toJson(dataParts)

    def mailCollection = collection(MAIL_COLLECTION_NAME);

    val mailStatus = MailStatus(
      BSONObjectID.generate(),
      mailId,
      dataParts.get("from").get.last,
      dataParts.get("to").get.last,
      dataParts.get("subject").get.last,
      views.html.test("sumit").toString)

    mailCollection.flatMap(collection =>
      collection.insert(mailStatus.copy(_id = BSONObjectID.generate())).map(wr =>
        if (wr.hasErrors)
          InternalServerError("Some server error has occured please try again later!")
        else
          Ok("Mail Sent!")))
  }*/

  def checkmail = auth.Action.async(parse.json) { request =>
    val requestData = request.body
    logger.debug("request>>" + requestData)
    val emailid = requestData.\("email").as[String]
    logger.debug("Email id>>" + emailid)
    //    val dataParts = request.body.dataParts
    //    val data = Json.toJson(dataParts)

    val httpClient = mailgunPoolingClient.getHttpClient
    val url = "https://api.mailgun.net/v3/address/validate"
    val key = "pubkey-b492fb4e9e8ad8f2ce0c2ae87e12272c"
    val headers = ("Accept" -> "multipart/form-data")
    val message = Map("address" -> Seq(emailid))

    val paramList: ListBuffer[NameValuePair] = ListBuffer[NameValuePair]()
    paramList.+=(new BasicNameValuePair("address", emailid))
    val httpGet = new HttpGet("/v3/address/validate")
    val parameterUri = new URIBuilder(httpGet.getURI).addParameters(JavaConversions.seqAsJavaList[NameValuePair](paramList.toList)).build()
    httpGet.setURI(parameterUri)

    val targetHost = new HttpHost("api.mailgun.net", 443, "https")
    val credsProvider: BasicCredentialsProvider = new BasicCredentialsProvider()
    val credentials: UsernamePasswordCredentials = new UsernamePasswordCredentials("api", key)
    credsProvider.setCredentials(AuthScope.ANY, credentials)

    val authCache = new BasicAuthCache()
    val basicAuth = new BasicScheme()
    authCache.put(targetHost, basicAuth)
    val context = HttpClientContext.create()
    context.setCredentialsProvider(credsProvider)
    context.setAuthCache(authCache)

    try {
      val httpResponse = httpClient.execute(targetHost, httpGet, context)
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        val inputStream = httpResponse.getEntity.getContent
        //        val res: JsValue = Json.parse(inputStream)
        val response = scala.io.Source.fromInputStream(inputStream).mkString
        logger.debug(response)
        Future{Ok(Json.parse(response))}
      }
      else {
        Future{Ok(Json.obj("error" -> true))}
      }
    }
    catch {
      case s: SocketTimeoutException => {
        logger.debug("Socket Timep out Exception >> ", s);
        Future{Ok(Json.obj("error" -> true))}
      }
      case c: java.net.ConnectException => { logger.debug("Connection Time Out Occurred >> ", c); Future{Ok(Json.obj("error" -> true))}}
      case io: IOException => { logger.debug("IO exception >> ", io); Future{Ok(Json.obj("error" -> true))}}
    }

    /*try {
      val httpResponse = httpClient.execute(httpGet)
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        val inputStream = httpResponse.getEntity.getContent
        /*val lines = Source.fromInputStream(inputStream).getLines*/

        val res: JsValue = Json.parse(inputStream)
        println("OUTPUT >> ", res)
        bank = (res.asInstanceOf[JsArray].value(0) \ "BankName").as[String]
      }
    }
    catch {
      case s: SocketTimeoutException => {
        //        s.printStackTrace()
        logger.debug("Socket Timep out Exception >> ", s)
      }
      case c: java.net.ConnectException => { logger.debug("Connection Time Out Occurred >> ", c); }
      case io: IOException => { logger.debug("IO exception >> ", io)}
    }*/

    /*ws.url(url).withQueryString("address" -> emailid)
      .withAuth("api", key, WSAuthScheme.BASIC).withHeaders("Accept" -> "multipart/form-data")
        .get()
      .map { response =>
        logger.debug(response.body)
        Ok(response.body)
      }*/
  }

  //Client client = new Client();


  //       ws.addFilter(new HTTPBasicAuthFilter("api",
  //                       "pubkey-b492fb4e9e8ad8f2ce0c2ae87e12272c"));
  //       WebResource webResource =
  //               client.resource("https://api.mailgun.net/v3" +
  //                               "/address/validate");
  //       MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
  //       queryParams.add("address", "foo@mailgun.net");
  //       return webResource.queryParams(queryParams).get(ClientResponse.class);

  /**
    *
    * Used to receive test related acknowledgments form mailgun
    *
    * @param ackType
    * @return
    */
  /*def mailAck(ackType: String) = Action.async { request =>
    logger.debug("recieved request for " + ackType)
    val bodyAsJson = Json.toJson(request.body.asFormUrlEncoded)
    val body = request.body.asFormUrlEncoded.get
    val mailBody = Json.stringify(bodyAsJson)
    val msgId: String = ackType match {
      case "delivered" => body.get("Message-Id").get.last
      case default => body.get("message-id").get.last
    }

    val mailAck: MailAck = MailAck(msgId, ackType, mailBody, BSONObjectID.generate)

    def mailAckCollection = collection(MAIL_ACK_COLLECTION_NAME)

    mailAckCollection.flatMap(collection =>
      collection.insert(mailAck.copy(_id = BSONObjectID.generate)).map(wr =>
        if (wr.hasErrors)
          InternalServerError("Some server error has occured please try again later!")
        else
          Ok))
  }*/

  /**
    * This API is used to show all mails send till date
    *
    * @return
    */
  /*def showMail = Action.async {
    def mailCollection = collection(MAIL_COLLECTION_NAME)

    mailCollection.flatMap(collection =>
      collection.genericQueryBuilder.cursor[MailStatus]().collect[List]().map(mails =>
        Ok(Json.toJson(mails))).recover {
        case t: Exception =>
          InternalServerError(t.getMessage)
      })
  }*/
}
