package controllers

import javax.inject.Inject

import constants.MongoConstants
import helpers.AuthenticatedAction
import org.slf4j.LoggerFactory
import play.Configuration
import play.api.libs.ws.{WSAuthScheme, WSClient}
import play.api.mvc.Controller
import service.{AttachmentService, MailService, MongoDbService}

import scala.concurrent.ExecutionContext

class MailGunController @Inject()(implicit val ec: ExecutionContext, ws: WSClient, attachService: AttachmentService, mgService: MailService,
                                  mongoDbService: MongoDbService, config: Configuration, auth: AuthenticatedAction)
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
    val emailid = requestData.\("email").as[String];
    logger.debug("Email id>>" + emailid)
    //    val dataParts = request.body.dataParts
    //    val data = Json.toJson(dataParts)

    val url = "https://api.mailgun.net/v3/address/validate";
    val key = "pubkey-b492fb4e9e8ad8f2ce0c2ae87e12272c";
    val headers = ("Accept" -> "multipart/form-data")
    val message = Map("address" -> Seq(emailid))

    ws.url(url).withQueryString("address" -> emailid)
      .withAuth("api", key, WSAuthScheme.BASIC).withHeaders("Accept" -> "multipart/form-data")
        .get()
      .map { response =>
        logger.debug(response.body)
        Ok(response.body)
      }
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
