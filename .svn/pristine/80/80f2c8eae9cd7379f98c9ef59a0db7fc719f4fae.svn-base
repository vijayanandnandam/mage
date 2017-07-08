package repository.module

import java.sql.Timestamp
import javax.xml.namespace.QName

import com.fincash.integration.ws.client.bsestar.{GetPassword, GetPasswordResponse}
import com.google.inject.Inject
import com.google.inject.name.Named
import constants.IntegrationConstants
import helpers.{BSEOrderResponseHelper, BSEUploadResponseHelper}
import models.integration.{BSEGetPassword, BSEPasswordValidateWrapper, BSEUploadGetPassword}
import org.slf4j.LoggerFactory
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.{WebServiceMessageCallback, WebServiceTemplate}
import org.springframework.ws.soap.SoapMessage
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables.FcpassRepo
import service.PropertiesLoaderService
import service.integration.{BSEStarOrderPopulator, BSEStarUploadPopulator}
import slick.jdbc.JdbcProfile
import utils.bse.BSEUtility

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 16-02-2017.
  */
class PasswordRepository @Inject()(implicit ec: ExecutionContext, protected val dbConfigProvider: DatabaseConfigProvider,
                                   integrationRepository: IntegrationRepository,
                                   bseStarpopulator: BSEStarOrderPopulator,
                                   bseStarUploadPopulator: BSEStarUploadPopulator,
                                   @Named("bseOrderTemplate") bseOrderWebService: WebServiceTemplate,
                                   @Named("bseUploadTemplate") bseUploadWebService: WebServiceTemplate,
                                   fcpassRepo: FcpassRepo,
                                   @Named("isLive") isLive: Boolean)
  extends IntegrationConstants with HasDatabaseConfigProvider[JdbcProfile] {

  val logger, log = LoggerFactory.getLogger(classOf[PasswordRepository])



  import profile.api._

  val propertiesLoader = PropertiesLoaderService.getConfig

  def getBSEPassword(bsePassCode: String): Future[(String, Timestamp, String)] = {
    val query = {
      sql"""SELECT PASSKEY,PASSEXPIRYDATE, PASSWORDPASSKEY FROM FCPASS WHERE PASSCODE = ${bsePassCode} FOR UPDATE""".as[(String, Timestamp, String)]
    }

    db.run(query.transactionally).map(values => {
      values.head
    })
  }

  def updateBSEPassword(): Future[Int] = {

    val bseDefaultParamList: ListBuffer[String] = ListBuffer[String](BSE_USER_ID_KEY, BSE_MEMBER_ID_KEY, BSE_PASSWORD_KEY)

    integrationRepository.getDefaultParamValues(bseDefaultParamList, BSE_INTEGRATION_NAME).flatMap(parameterMap => {
      val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
      val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")
      val memberId: String = parameterMap.get(BSE_MEMBER_ID_KEY).getOrElse("")
      var output = ("", "")


      val query = {
        sql"""SELECT PASSKEY, PASSEXPIRYDATE, PASSWORDPASSKEY FROM FCPASS WHERE PASSCODE = ${BSE_ORDER_API_PASS_CODE} FOR UPDATE""".as[(String, Timestamp, String)]

      } andThen {

        val passKey = BSEUtility.generatePassKey
        val getBSEPassword = BSEGetPassword(userId.toString, password, passKey)
        val bsePasswordResponse = getPasswordResponse(getBSEPassword)
        val encryptedPassword = bsePasswordResponse.getBSEPasswordResponse.encryptedPassword

        sqlu"""UPDATE FCPASS SET PASSKEY = ${encryptedPassword}, PASSEXPIRYDATE = NOW() + INTERVAL 280 SECOND,PASSWORDPASSKEY = ${passKey} WHERE PASSCODE = ${BSE_ORDER_API_PASS_CODE} """

      } andThen {
        val passKey = BSEUtility.generatePassKey
        val getBSEPassword = BSEUploadGetPassword(userId, memberId, password, passKey)
        val bsePasswordResponse = getPasswordResponse(getBSEPassword)
        val encryptedPassword = bsePasswordResponse.getBSEPasswordResponse.encryptedPassword

        sqlu"""UPDATE FCPASS SET PASSKEY = ${encryptedPassword}, PASSEXPIRYDATE = NOW() + INTERVAL 280 SECOND,PASSWORDPASSKEY = ${passKey} WHERE PASSCODE = ${BSE_UPLOAD_API_PASS_CODE} """
      }.transactionally.withPinnedSession


      db.run(query).map(values => {
        values
      })
    })
  }

  def getPasswordResponse(getBSEPassword: BSEGetPassword): BSEPasswordValidateWrapper = {

    val getPassword: GetPassword = bseStarpopulator.populateGetPassword(getBSEPassword)
    val passwordResponse: GetPasswordResponse = bseOrderWebService.marshalSendAndReceive(getPassword, new WebServiceMessageCallback() {

      override def doWithMessage(message: WebServiceMessage) = {
        logger.debug("Get Password Request Callback called.")
        val soapMessage = message.asInstanceOf[SoapMessage]

        val soapHeader = soapMessage.getSoapHeader

        val wsaToQName = new QName(propertiesLoader.getString("bse.ws.qName"), "To", "wsa")
        val wsaTo = soapHeader.addHeaderElement(wsaToQName)
        wsaTo.setText(bseOrderWebService.getDefaultUri)

        val wsaActionQName = new QName(propertiesLoader.getString("bse.ws.qName"), "Action", "wsa")
        val wsaAction = soapHeader.addHeaderElement(wsaActionQName)

        wsaAction.setText(propertiesLoader.getString("bse.mfOrderEntry.getPassword.action"))
      }
    }).asInstanceOf[GetPasswordResponse]

    //bSERequestLogRepository.saveGetPasswordRequest(getPassword, uniqueRefNo, "user")
    //bSEResponseLogRepository.saveGetPasswordResponse(passwordResponse, uniqueRefNo, "user")

    val bsePasswordValidateWrapper: BSEPasswordValidateWrapper = BSEOrderResponseHelper.convertBSEResponse(passwordResponse)
    bsePasswordValidateWrapper
  }

  def getPasswordResponse(bseMfApiGetPassword: BSEUploadGetPassword): BSEPasswordValidateWrapper = {

    var passwordAction = ""

    if(isLive){
      passwordAction = propertiesLoader.getString("bse.live.mfUpload.getPassword.action")
    } else{
      passwordAction = propertiesLoader.getString("bse.mfUpload.getPassword.action")
    }

    val getPassword: com.fincash.integration.ws.client.bsestar.upload.GetPassword = bseStarUploadPopulator.populateGetPassword(bseMfApiGetPassword)

    val passwordResponse: com.fincash.integration.ws.client.bsestar.upload.GetPasswordResponse = bseUploadWebService.marshalSendAndReceive(getPassword, new WebServiceMessageCallback() {

      override def doWithMessage(message: WebServiceMessage) = {
        logger.debug("BSE Upload Get Password Request Callback called.")
        val soapMessage = message.asInstanceOf[SoapMessage]

        val soapHeader = soapMessage.getSoapHeader

        val wsaToQName = new QName(propertiesLoader.getString("bse.ws.qName"), "To", "wsa")
        val wsaTo = soapHeader.addHeaderElement(wsaToQName)
        wsaTo.setText(bseUploadWebService.getDefaultUri)

        val wsaActionQName = new QName(propertiesLoader.getString("bse.ws.qName"), "Action", "wsa")
        val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
        wsaAction.setText(passwordAction)
      }
    }).asInstanceOf[com.fincash.integration.ws.client.bsestar.upload.GetPasswordResponse]

    //bSERequestLogRepository.saveMfApiGetPasswordRequest(getPassword, uniqueRefNo, "user")
    //bSEResponseLogRepository.saveMfApiGetPasswordResponse(passwordResponse, uniqueRefNo, "user")

    val bsePasswordValidateWrapper: BSEPasswordValidateWrapper = BSEUploadResponseHelper.convertBSEResponse(passwordResponse)
    bsePasswordValidateWrapper
  }
}
