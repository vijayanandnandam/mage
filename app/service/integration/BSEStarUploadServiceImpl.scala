package service.integration

import java.util.Calendar
import javax.inject.{Inject, Singleton}
import javax.xml.namespace.QName

import com.fincash.integration.ws.client.bsestar.upload.{GetPassword, GetPasswordResponse, MFAPI, MFAPIResponse}
import com.google.inject.name.Named
import constants.IntegrationConstants
import helpers.integration.bse.BSEPasswordHelper
import helpers.{BSEOrderResponseHelper, BSEUploadResponseHelper, FCServiceHelper}
import models.{ClientOrderPaymentStatus, FCSTPEntryModel, FCSWPEntryModel, XsipMandateRegisterModel}
import models.integration._
import org.slf4j.LoggerFactory
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.{WebServiceMessageCallback, WebServiceTemplate}
import org.springframework.ws.soap.SoapMessage
import repository.module.{IntegrationRepository, PasswordRepository}
import repository.module.integration.bse.{BSERequestLogRepository, BSEResponseLogRepository}
import service.PropertiesLoaderService
import utils.DateTimeUtils
import utils.bse.BSEUtility

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BSEStarUploadServiceImpl @Inject()(implicit ec: ExecutionContext, @Named("bseUploadTemplate") bseUploadWebService: WebServiceTemplate,
                                         fcService: FCService,
                                         bseStarUploadPopulator: BSEStarUploadPopulator,
                                         fcServiceHelper: FCServiceHelper,
                                         bSEPasswordHelper: BSEPasswordHelper,
                                         bSERequestLogRepository: BSERequestLogRepository,
                                         bSEResponseLogRepository: BSEResponseLogRepository,
                                         passwordRepository: PasswordRepository,
                                         bSEClientRegisterHelper: BSEClientHelper,
                                         bSEFATCAUploadHelper: BSEFATCAUploadHelper,
                                         integrationRepository: IntegrationRepository,
                                         bSEUploadResponseHelper : BSEUploadResponseHelper, bSEOrderResponseHelper : BSEOrderResponseHelper,
                                         @Named("isLive") isLive: Boolean) extends BSEStarService with IntegrationConstants {

  val logger = LoggerFactory.getLogger(classOf[BSEStarUploadServiceImpl])

  /**
    *
    * @param bseMfApiGetPassword
    * @param uniqueRefNo
    * @return
    */
  def getPasswordResponse(bseMfApiGetPassword: BSEUploadGetPassword, uniqueRefNo: String): BSEPasswordValidateWrapper = {

    var passwordAction = ""
    if(isLive){
      passwordAction = getPropertiesConfig.getString("bse.live.mfUpload.getPassword.action")
    } else{
      passwordAction = getPropertiesConfig.getString("bse.mfUpload.getPassword.action")
    }

    val getPassword: GetPassword = bseStarUploadPopulator.populateGetPassword(bseMfApiGetPassword)

    val passwordResponse: GetPasswordResponse = bseUploadWebService.marshalSendAndReceive(getPassword, new WebServiceMessageCallback() {

      override def doWithMessage(message: WebServiceMessage) = {
        logger.debug("BSE Upload Get Password Request Callback called.")
        val soapMessage = message.asInstanceOf[SoapMessage]

        val soapHeader = soapMessage.getSoapHeader

        val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
        val wsaTo = soapHeader.addHeaderElement(wsaToQName)
        wsaTo.setText(bseUploadWebService.getDefaultUri)

        val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
        val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
        wsaAction.setText(passwordAction)
      }
    }).asInstanceOf[GetPasswordResponse]

    bSERequestLogRepository.saveMfApiGetPasswordRequest(getPassword, uniqueRefNo, "user")
    bSEResponseLogRepository.saveMfApiGetPasswordResponse(passwordResponse, uniqueRefNo, "user")

    val bsePasswordValidateWrapper: BSEPasswordValidateWrapper = bSEUploadResponseHelper.convertBSEResponse(passwordResponse)
    bsePasswordValidateWrapper
  }

  /**
    *
    * @param clientCode
    * @param redirectUrl
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def getPaymentGatewayResponse(clientCode: String, redirectUrl: String, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    updateApiPassword().flatMap(x => {

      fcServiceHelper.getBSEDefaultParameters().flatMap(parameterMap => {

        val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
        val memberId: String = parameterMap.get(BSE_MEMBER_ID_KEY).getOrElse("")
        val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

        val paymentGatewayParam = BSEUtility.formatValue(memberId, clientCode, redirectUrl)
        val paymentGatewayFlag = BSEUtility.getPaymentGatewayFlag

        val bseMfApiModel = BSEUploadMfApi(paymentGatewayFlag, userId, password, paymentGatewayParam)

        getMfApiResponse(bseMfApiModel, uniqueRefNo, false, userName)
      })
    })
  }

  /**
    *
    * @param clientFatcaUpload
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def getFatcaUploadResponse(clientFatcaUpload: ClientFatcaUpload, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    fcServiceHelper.getBSEDefaultParameters().flatMap(parameterMap => {

      val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
      val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

      val fatcaUploadValuesList = bSEFATCAUploadHelper.getFatcaUploadValuesList(clientFatcaUpload)
      val fatcaParamValue = BSEUtility.formatValuesList(fatcaUploadValuesList)

      val bseMfApiModel: BSEUploadMfApi = BSEUploadMfApi(BSEUtility.getFatcaUploadFlag, userId, password, fatcaParamValue)

      getMfApiResponse(bseMfApiModel, uniqueRefNo, false, userName)
    })
  }

  /**
    *
    * @param bSEClientDetailsModel
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def getUCCResponse(bSEClientDetailsModel: BSEClientDetailsModel, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {


    fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

      val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
      val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

      val clientRegValuesList = bSEClientRegisterHelper.getClientRegisterValuesList(bSEClientDetailsModel)
      val clientRegParam = BSEUtility.formatValuesList(clientRegValuesList)

      val bseMfApiModel: BSEUploadMfApi = BSEUploadMfApi(BSEUtility.getUCCFlag, userId, password, clientRegParam)

      getMfApiResponse(bseMfApiModel, uniqueRefNo, false, userName)
    }
  }

  /**
    *
    * @param newPassword
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def getChangePasswordResponse(newPassword: String, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

      val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
      val memberId: String = parameterMap.get(BSE_MEMBER_ID_KEY).getOrElse("")
      val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

      val passwordParam = BSEUtility.generateChangePasswordParam(password, newPassword)
      val changePasswordFlag = BSEUtility.getChangePasswordFlag

      val bseMfApiModel: BSEUploadMfApi = BSEUploadMfApi(changePasswordFlag, userId, password, passwordParam)

      getMfApiResponse(bseMfApiModel, uniqueRefNo, false, userName)
    }

  }

  /**
    *
    * @param xsipMandateRegisterModel
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def registerMandate(xsipMandateRegisterModel: XsipMandateRegisterModel, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

      val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
      val memberId: String = parameterMap.get(BSE_MEMBER_ID_KEY).getOrElse("")
      val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

      val mandateParam = BSEUtility.formatValue(memberId, xsipMandateRegisterModel.clientCode,
        xsipMandateRegisterModel.amount, xsipMandateRegisterModel.ifscCode,
        xsipMandateRegisterModel.accNo, xsipMandateRegisterModel.mandateType)

      val bseMfApiModel: BSEUploadMfApi = BSEUploadMfApi(BSEUtility.getMandateFlag, userId, password, mandateParam)

      getMfApiResponse(bseMfApiModel, uniqueRefNo, true, userName)
    }


  }

  /**
    *
    * @param fcSTPEntryModel
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def registerSTP(fcSTPEntryModel: FCSTPEntryModel, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    fcService.getBSESTPEntryModel(fcSTPEntryModel).flatMap(bseSTPEntryModel =>{

      val stpParam = BSEUtility.formatValue(bseSTPEntryModel.clientCode.trim, bseSTPEntryModel.fromSchemeCode.trim,
        bseSTPEntryModel.toSchemeCode.trim, bseSTPEntryModel.buySellType,
        bseSTPEntryModel.transactionMode,
        bseSTPEntryModel.folioNo.getOrElse("").trim,
        bseSTPEntryModel.internalRefNo.getOrElse("").trim,
        bseSTPEntryModel.startDate.trim,
        bseSTPEntryModel.frequencyType,
        bseSTPEntryModel.numberOfTransfers,
        bseSTPEntryModel.installmentAmt,
        bseSTPEntryModel.firstOrderFlag,
        bseSTPEntryModel.subBrCode.getOrElse("").trim,
        bseSTPEntryModel.euinDeclaration,
        bseSTPEntryModel.euin.getOrElse("").trim,
        bseSTPEntryModel.remarks.getOrElse("").trim,
        bseSTPEntryModel.subBrARN.getOrElse("").trim)
      fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

        val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
        val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

        val bseMfApiModel: BSEUploadMfApi = BSEUploadMfApi(BSEUtility.getSTPFlag, userId, password, stpParam)

        getMfApiResponse(bseMfApiModel, uniqueRefNo, true, userName)
      }
    })

  }

  /**
    *
    * @param fcSWPEntryModel
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def registerSWP(fcSWPEntryModel: FCSWPEntryModel, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    fcService.getBSESWPEntryModel(fcSWPEntryModel).flatMap(bseSWPEntryModel =>{

      val swpParam = BSEUtility.formatValue(bseSWPEntryModel.clientCode.trim, bseSWPEntryModel.schemeCode.trim,
        bseSWPEntryModel.transactionMode,
        bseSWPEntryModel.folioNo.getOrElse("").trim,
        bseSWPEntryModel.internalRefNo.getOrElse("").trim,
        bseSWPEntryModel.startDate.trim,
        bseSWPEntryModel.numberOfWithdrawls,
        bseSWPEntryModel.frequencyType,
        bseSWPEntryModel.installmentAmt,
        bseSWPEntryModel.installmentUnits.getOrElse(""),
        bseSWPEntryModel.firstOrderFlag,
        bseSWPEntryModel.subBrCode.getOrElse("").trim,
        bseSWPEntryModel.euinDeclaration,
        bseSWPEntryModel.euin.getOrElse("").trim,
        bseSWPEntryModel.remarks.getOrElse("").trim,
        bseSWPEntryModel.subBrARN.getOrElse("").trim)

      fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

        val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
        val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

        val bseMfApiModel: BSEUploadMfApi = BSEUploadMfApi(BSEUtility.getSWPFlag, userId, password, swpParam)

        getMfApiResponse(bseMfApiModel, uniqueRefNo, true, userName)
      }
    })

  }

  /**
    *
    * @param clientOrderPaymentStatus
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  def getClientOrderPaymentStatus(clientOrderPaymentStatus: ClientOrderPaymentStatus, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    updateApiPassword().flatMap(x => {

      val orderPaymentStatusParam = BSEUtility.formatValue(clientOrderPaymentStatus.clientCode,
        clientOrderPaymentStatus.orderId,
        clientOrderPaymentStatus.segment)
      fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

        val userId: Long = parameterMap.get(BSE_USER_ID_KEY).getOrElse("0").toLong
        val password: String = parameterMap.get(BSE_PASSWORD_KEY).getOrElse("")

        val bseMfApiModel: BSEUploadMfApi = BSEUploadMfApi(BSEUtility.getOrderPaymentStatusFlag, userId, password, orderPaymentStatusParam)

        getPaymentStatusResponse(bseMfApiModel, uniqueRefNo, userName)

      }
    })

  }

  /**
    *
    * @return
    */
  def updateApiPassword(): Future[Int] = {

    validateBSEAccountPassword().flatMap(accntPassUpdated =>{

      passwordRepository.getBSEEncryptedPassword(BSE_UPLOAD_API_PASS_CODE).flatMap(passRow => {

        val expiryTime = passRow._2
        val isValid = DateTimeUtils.checkBsePassTimeValidity(expiryTime)
        logger.debug("BSE Password Valid = " + isValid)
        if (!isValid) {
          passwordRepository.updateBSEPassword.map(x => x)
        } else {
          Future {
            0
          }
        }
      })
    })
  }

  /**
    *
    * @return
    */
  def validateBSEAccountPassword():Future[Boolean] = {
    integrationRepository.getByParaName(BSE_PASSWORD_KEY).flatMap(drqpList =>{
      val lastPasswordTime = Calendar.getInstance()
      lastPasswordTime.setTimeInMillis(drqpList.head.modifydate.getTime)

      val currentTime = Calendar.getInstance()
      val noOfDaysBeforeChanged = DateTimeUtils.getDiffDays(lastPasswordTime.getTime, currentTime.getTime)

      logger.debug("No of Days Before BSE Password Last Changed = " + noOfDaysBeforeChanged)

      val passChangeDaysLimit = PropertiesLoaderService.getConfig.getLong("bse.account.password.change.days")

      if(noOfDaysBeforeChanged > passChangeDaysLimit){
        changeBSEPassword()
      } else{
        Future.apply(true)
      }
    })
  }

  /**
    *
    * @return
    */
  def changeBSEPassword():Future[Boolean] = {

    val newPassword = bSEPasswordHelper.generateBsePassword()

    getChangePasswordResponse(newPassword,"",SYSTEM_USER).flatMap(bseUploadResponseWrapper =>{
      val errorList = bseUploadResponseWrapper.errorList
      if(errorList.get.isEmpty){
        integrationRepository.updateBSEDefaultParamValue(BSE_PASSWORD_KEY,newPassword).map(updated =>{
          true
        })
      } else{
        Future.apply(false)
      }
    })
  }

  /**
    *
    * @param bseMfApiModel
    * @param uniqueRefNo
    * @param referenceNumberRequired
    * @param userName
    * @return
    */
  private def getMfApiResponse(bseMfApiModel: BSEUploadMfApi, uniqueRefNo: String, referenceNumberRequired: Boolean, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    var mfApiAction = ""

    if(isLive){
      mfApiAction = getPropertiesConfig.getString("bse.live.mfUpload.mFAPI.action")
    } else{
      mfApiAction = getPropertiesConfig.getString("bse.mfUpload.mFAPI.action")
    }
    fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

      passwordRepository.getBSEEncryptedPassword(BSE_UPLOAD_API_PASS_CODE).map(encryptedPasswordTuple => {

        val encryptedPassword = encryptedPasswordTuple._1

        val mfApi: MFAPI = bseStarUploadPopulator.populateGetMfApi(bseMfApiModel, encryptedPassword)

        val mfApiResponse: MFAPIResponse = bseUploadWebService.marshalSendAndReceive(mfApi, new WebServiceMessageCallback() {

          override def doWithMessage(message: WebServiceMessage) = {
            logger.debug("BSE Upload MFAPI Request Callback called.")
            val soapMessage = message.asInstanceOf[SoapMessage]

            val soapHeader = soapMessage.getSoapHeader

            val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
            val wsaTo = soapHeader.addHeaderElement(wsaToQName)
            wsaTo.setText(bseUploadWebService.getDefaultUri)

            val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
            val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
            wsaAction.setText(mfApiAction)
          }
        }).asInstanceOf[MFAPIResponse]

        bSERequestLogRepository.saveMfApiRequest(mfApi, uniqueRefNo, userName)
        bSEResponseLogRepository.saveMfApiResponse(mfApiResponse, uniqueRefNo, userName)
        logger.debug("BSE Upload MFAPI Response Received.")
        val bseMfApiResponseValidateWrapper: BSEUploadMfApiResponseValidateWrapper = bSEUploadResponseHelper.convertBSEResponse(mfApiResponse, referenceNumberRequired)
        bseMfApiResponseValidateWrapper
      })
    }

  }

  /**
    *
    * @param bseMfApiModel
    * @param uniqueRefNo
    * @param userName
    * @return
    */
  private def getPaymentStatusResponse(bseMfApiModel: BSEUploadMfApi, uniqueRefNo: String, userName: String): Future[BSEUploadMfApiResponseValidateWrapper] = {

    var mfApiAction = ""

    if(isLive){
      mfApiAction = getPropertiesConfig.getString("bse.live.mfUpload.mFAPI.action")
    } else{
      mfApiAction = getPropertiesConfig.getString("bse.mfUpload.mFAPI.action")
    }

    fcServiceHelper.getBSEDefaultParameters().flatMap { parameterMap =>

      passwordRepository.getBSEEncryptedPassword(BSE_UPLOAD_API_PASS_CODE).map(encryptedPasswordTuple => {
        val encryptedPassword = encryptedPasswordTuple._1

        val mfApi: MFAPI = bseStarUploadPopulator.populateGetMfApi(bseMfApiModel, encryptedPassword)

        val mfApiResponse: MFAPIResponse = bseUploadWebService.marshalSendAndReceive(mfApi, new WebServiceMessageCallback() {

          override def doWithMessage(message: WebServiceMessage) = {
            logger.debug("BSE Upload MFAPI Request Callback called.")
            val soapMessage = message.asInstanceOf[SoapMessage]

            val soapHeader = soapMessage.getSoapHeader

            val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
            val wsaTo = soapHeader.addHeaderElement(wsaToQName)
            wsaTo.setText(bseUploadWebService.getDefaultUri)

            val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
            val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
            wsaAction.setText(mfApiAction)
          }
        }).asInstanceOf[MFAPIResponse]

        bSERequestLogRepository.saveMfApiRequest(mfApi, uniqueRefNo, userName)
        bSEResponseLogRepository.saveMfApiResponse(mfApiResponse, uniqueRefNo, userName)
        logger.debug("BSE Payment Status Response Received.")
        val bseMfApiResponseValidateWrapper: BSEUploadMfApiResponseValidateWrapper = bSEUploadResponseHelper.convertPaymentStatusResponse(mfApiResponse)
        bseMfApiResponseValidateWrapper
      })
    }

  }
}