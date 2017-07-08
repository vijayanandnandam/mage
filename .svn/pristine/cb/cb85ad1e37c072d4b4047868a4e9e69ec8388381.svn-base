package service

import java.io.File
import javax.inject.Inject

import org.slf4j.LoggerFactory
import play.api.libs.mailer.{Attachment, AttachmentFile, Email, MailerClient}

import scala.collection.mutable.{HashMap, ListBuffer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailService @Inject()(implicit mailerClient: MailerClient, configuration: play.api.Configuration) {

  val logger, log = LoggerFactory.getLogger(classOf[MailService])

  def sendMail(to: String, subject: String, bodyText: Option[String] = None,
               bodyHTML: Option[String] = None, replyTo: Option[String] = None, from: Option[String] = None,
               filePath: Option[String] = None, attachmentFiles: Option[HashMap[String, String]] = None,
               cc: Option[ListBuffer[String]] = None, bcc: Option[ListBuffer[String]] = None): Future[String] = {

    logger.info("Got Send Mail Request")
    logger.debug("TO:" + to)
    logger.debug("FROM:" + from)
    logger.debug("SUBJECT:" + subject)
    //logger.debug("BODY HTML:" + bodyHTML);
    /*logger.debug("BODY TEXT:" + bodyText);*/
    logger.debug("attachmentFiles:" + attachmentFiles.toString);
    //preparing attachments
    var attachments: Seq[Attachment] = Seq.empty
    if (attachmentFiles.isDefined) {
      for (key <- attachmentFiles.get.keySet) {
        val value = attachmentFiles.get.get(key);
        if(value.nonEmpty){
          val file = new File(value.get)
          if(file.exists()){
            logger.debug("Attachment File exists ["+file.getCanonicalPath+"]")
            attachments = attachments :+ AttachmentFile(key, file)
          }else{
            logger.debug("Attachment File not exists ["+file.getCanonicalPath+"]")
          }
        }
      }
    }
    val bounceAddress: Option[String] = Some(configuration.underlying.getString("mail.default.bounce.address"))

    val cc_s = cc.getOrElse(ListBuffer.empty[String])
    val bcc_s = bcc.getOrElse(ListBuffer.empty[String])
    val from_s = from.getOrElse(configuration.underlying.getString("mail.default.from"))
    val to_s = Seq.apply[String](to)

    logger.debug(" CC >>> " + cc_s)
    logger.debug(" BCC >>>  " + bcc_s)
    logger.debug(" from >>>  " + from_s)
    logger.debug(" to >>>  " + to_s)
    try {
      val email = Email(
        subject,
        from_s,
        to_s,
        bodyText,
        bodyHTML,
        Some("UTF-8"),
        cc_s,
        bcc_s,
        Some(replyTo.getOrElse(configuration.underlying.getString("mail.default.reply.to"))),
        bounceAddress,
        attachments
      )
      Future.apply(mailerClient.send(email));
    } catch {
      case exception: Exception => {
        exception.printStackTrace();
        logger.error(exception.getMessage)
        throw exception
      }
    }
  }

  def manageMailAck() = {

  }

}