package helpers

import java.io.{File, FileOutputStream}
import java.util.Calendar
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import org.slf4j.LoggerFactory
import play.api.libs.ws.{StreamedResponse, WSClient}
import utils.mail.MailTemplateHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Random

/**
  * Created by Fincash on 03-03-2017.
  */
class MailHelper @Inject()(mu: MailTemplateHelper, ws: WSClient, mth: MailTemplateHelper, configuration: play.api.Configuration) {

  private val logger, log = LoggerFactory.getLogger(classOf[MailHelper])
  val filepath = configuration.underlying.getString("mail.url.baseurl")
  val staticPath = configuration.underlying.getString("mail.url.staticImagePath")
  val baseUrl = configuration.underlying.getString("mail.url.baseurl")

  val fromRegMail = configuration.underlying.getString("mail.registration.verification.from")
  val replytoRegMail = configuration.underlying.getString("mail.registration.verification.from")
  val bccReg = configuration.underlying.getString("mail.registration.verification.bcc")
  val toRegMail = configuration.underlying.getString("mail.registration.verification.to")


  val fromRegStatMail = configuration.underlying.getString("mail.registration.status.from")
  val replytoReStatgMail = configuration.underlying.getString("mail.registration.status.from")
  val bccRegStat = configuration.underlying.getString("mail.registration.status.bcc")


  val bcceKYC = configuration.underlying.getString("mail.eKYC-status.bcc")


  def generateLink(username: String): String = {
    (new Random().nextInt(9999999)).toString + Calendar.getInstance.getTimeInMillis.toString + (new Random().nextInt(9999999)).toString
  }

  def generatePassword(): String = {
    (new Random().nextInt(9999999)).toString + (new Random().nextInt(9999999)).toString
  }

  def getMth(): MailTemplateHelper = {
    mth
  }

  def downLoadFile(url: String, filetype: String): Future[File] = {
    val futureResponse: Future[StreamedResponse] = ws.url(url).withFollowRedirects(true).
      withRequestTimeout(Duration(30000, "millis")).withMethod("GET").stream()

    val downloadedFile: Future[File] = futureResponse.flatMap {
      res =>
        val file = new File(configuration.getString("mail.fileDownload.path").getOrElse("") + Calendar.getInstance().getTimeInMillis + "_mail_" + (new Random().nextInt(9999999)) + "." + filetype.toLowerCase);
        val outputStream = new FileOutputStream(file)

        // The sink that writes to the output stream
        val sink = Sink.foreach[ByteString] { bytes =>
          outputStream.write(bytes.toArray)
        }
        logger.debug("Download file path:" + file.getCanonicalPath);
        // materialize and run the stream
        implicit val system = ActorSystem("FileDownloader")
        implicit val materializer = ActorMaterializer()
        res.body.runWith(sink).andThen {
          case result =>
            // Close the output stream whether there was an error or not
            outputStream.close()
            // Get the result or rethrow the error
            result.get
        }.map(_ => file)

    }
    downloadedFile
  }

}
