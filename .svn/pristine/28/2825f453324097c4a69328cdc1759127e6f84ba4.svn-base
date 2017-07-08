package service.integration

import javax.inject.Inject
import javax.xml.namespace.QName

import com.fincash.integration.ws.client.bsestar._
import com.google.inject.name.Named
import constants.IntegrationConstants
import helpers.BSEOrderResponseHelper
import models._
import models.integration._
import org.slf4j.LoggerFactory
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.{WebServiceMessageCallback, WebServiceTemplate}
import org.springframework.ws.soap.SoapMessage
import repository.module.PasswordRepository
import repository.module.integration.bse.{BSERequestLogRepository, BSEResponseLogRepository}

import scala.concurrent.{ExecutionContext, Future}

class BSEStarOrderEntryServiceImpl @Inject()(implicit ec: ExecutionContext, @Named("bseOrderTemplate") bseOrderWebService: WebServiceTemplate,
                                             fcService: FCService,
                                             bseStarpopulator: BSEStarOrderPopulator,
                                             bSERequestLogRepository: BSERequestLogRepository,
                                             bSEResponseLogRepository: BSEResponseLogRepository,passwordRepository: PasswordRepository) extends BSEStarService
  with IntegrationConstants{

  val log = LoggerFactory.getLogger(classOf[BSEStarOrderEntryServiceImpl])

  /**
    * Gets Password Response
    * from BSE Web Service
    */
  def getPasswordResponse(getBSEPassword: BSEGetPassword,uniqueRefNo:String): BSEPasswordValidateWrapper = {

    val getPassword: GetPassword = bseStarpopulator.populateGetPassword(getBSEPassword)
    val passwordResponse: GetPasswordResponse = bseOrderWebService.marshalSendAndReceive(getPassword, new WebServiceMessageCallback() {

      override def doWithMessage(message: WebServiceMessage) = {
        log.debug("Get Password Request Callback called.")
        val soapMessage = message.asInstanceOf[SoapMessage]

        val soapHeader = soapMessage.getSoapHeader

        val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
        val wsaTo = soapHeader.addHeaderElement(wsaToQName)
        wsaTo.setText(bseOrderWebService.getDefaultUri)

        val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
        val wsaAction = soapHeader.addHeaderElement(wsaActionQName)

        wsaAction.setText(getPropertiesConfig.getString("bse.mfOrderEntry.getPassword.action"))
      }
    }).asInstanceOf[GetPasswordResponse]

    //bSERequestLogRepository.saveGetPasswordRequest(getPassword, uniqueRefNo, "user")
    //bSEResponseLogRepository.saveGetPasswordResponse(passwordResponse, uniqueRefNo, "user")

    val bsePasswordValidateWrapper: BSEPasswordValidateWrapper = BSEOrderResponseHelper.convertBSEResponse(passwordResponse)
    bsePasswordValidateWrapper
  }

  /**
    * Gets Order Entry Response
    * from BSE Web Service
    */
  def getOrderEntryParamResponse(fcOrderEntry: FCOrderEntryModel, userName:String): Future[BSEOrderValidateWrapper] = {

    fcService.getBSEOrderEntryParam(fcOrderEntry).flatMap { bseOrderEntryParam =>

      passwordRepository.getBSEPassword(BSE_ORDER_API_PASS_CODE).map(encryptedPasswordTuple =>{

        val encryptedPassword = encryptedPasswordTuple._1
        val passKey = encryptedPasswordTuple._3
        val updatedBseOrderEntryParam = bseOrderEntryParam.copy(passKey = passKey)

        val orderEntryParam: OrderEntryParam = bseStarpopulator.populateOrderEntryParam(updatedBseOrderEntryParam, encryptedPassword)

        val orderEntryParamResponse: OrderEntryParamResponse = bseOrderWebService.marshalSendAndReceive(orderEntryParam, new WebServiceMessageCallback() {

          override def doWithMessage(message: WebServiceMessage) = {
            log.debug("BSE Order Entry Request Callback called.")

            val soapMessage = message.asInstanceOf[SoapMessage]
            val soapHeader = soapMessage.getSoapHeader

            val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
            val wsaTo = soapHeader.addHeaderElement(wsaToQName)
            wsaTo.setText(bseOrderWebService.getDefaultUri)

            val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
            val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
            wsaAction.setText(getPropertiesConfig.getString("bse.mfOrderEntry.orderEntryParam.action"))
          }
        }).asInstanceOf[OrderEntryParamResponse]

        bSERequestLogRepository.saveOrderEntryRequest(orderEntryParam, fcOrderEntry.uniqueRefNo, userName)
        bSEResponseLogRepository.saveOrderEntryResponse(orderEntryParamResponse, fcOrderEntry.uniqueRefNo, userName)
        log.debug("Order Response Received")
        val bseOrderValidateWrapper: BSEOrderValidateWrapper = BSEOrderResponseHelper.convertBSEResponse(orderEntryParamResponse)
        bseOrderValidateWrapper
      })
    }


  }

  /**
    * Gets Sip Order Entry Response
    * from BSE Web Service
    */
  def getSipOrderEntryParamResponse(fcSipOrderEntry: FCSipOrderEntryModel, userName:String): Future[BSESipOrderValidateWrapper] = {

    fcService.getBSESipOrderEntryParam(fcSipOrderEntry).flatMap { bseSipOrderEntryParam =>

      passwordRepository.getBSEPassword(BSE_ORDER_API_PASS_CODE).map(encryptedPasswordTuple => {

        val encryptedPassword = encryptedPasswordTuple._1
        val passKey = encryptedPasswordTuple._3
        val updatedBseSipOrderEntryParam = bseSipOrderEntryParam.copy(passKey = passKey)

        val sipOrderEntryParam: SipOrderEntryParam = bseStarpopulator.populateSipOrderEntryParam(updatedBseSipOrderEntryParam, encryptedPassword)

        val sipOrderEntryParamResponse: SipOrderEntryParamResponse = bseOrderWebService.marshalSendAndReceive(sipOrderEntryParam, new WebServiceMessageCallback() {

          override def doWithMessage(message: WebServiceMessage) = {
            log.debug("BSE SIP Order Entry Request Callback called.")

            val soapMessage = message.asInstanceOf[SoapMessage]
            val soapHeader = soapMessage.getSoapHeader

            val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
            val wsaTo = soapHeader.addHeaderElement(wsaToQName)
            wsaTo.setText(bseOrderWebService.getDefaultUri)

            val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
            val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
            wsaAction.setText(getPropertiesConfig.getString("bse.mfOrderEntry.sipOrderEntryParam.action"))
          }
        }).asInstanceOf[SipOrderEntryParamResponse]

        bSERequestLogRepository.saveSipOrderEntryRequest(sipOrderEntryParam, fcSipOrderEntry.uniqueRefNo, userName)
        bSEResponseLogRepository.saveSipOrderEntryResponse(sipOrderEntryParamResponse, fcSipOrderEntry.uniqueRefNo, userName)
        val bseSipOrderValidateWrapper: BSESipOrderValidateWrapper = BSEOrderResponseHelper.convertBSEResponse(sipOrderEntryParamResponse)
        bseSipOrderValidateWrapper
      })
    }


  }

  /**
    * Gets Xsip Order Entry Response
    * from BSE Web Service
    */
  def getXsipOrderEntryParamResponse(fcXsipOrderEntry: FCXsipOrderEntryModel, userName:String): Future[BSEXsipOrderValidateWrapper] = {

    fcService.getBSEXsipOrderEntryParam(fcXsipOrderEntry).flatMap{bseXsipOrderEntryParam =>

      passwordRepository.getBSEPassword(BSE_ORDER_API_PASS_CODE).map(encryptedPasswordTuple =>{
        val encryptedPassword = encryptedPasswordTuple._1
        val passKey = encryptedPasswordTuple._3
        val updatedBseXsipOrderEntryParam = bseXsipOrderEntryParam.copy(passKey = passKey)
        val xsipOrderEntryParam: XsipOrderEntryParam = bseStarpopulator.populateXsipOrderEntryParam(updatedBseXsipOrderEntryParam, encryptedPassword)

        val xsipOrderEntryParamResponse: XsipOrderEntryParamResponse = bseOrderWebService.marshalSendAndReceive(xsipOrderEntryParam, new WebServiceMessageCallback() {

          override def doWithMessage(message: WebServiceMessage) = {
            log.debug("BSE Xsip Order Entry Request Callback called.")

            val soapMessage = message.asInstanceOf[SoapMessage]
            val soapHeader = soapMessage.getSoapHeader

            val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
            val wsaTo = soapHeader.addHeaderElement(wsaToQName)
            wsaTo.setText(bseOrderWebService.getDefaultUri)

            val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
            val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
            wsaAction.setText(getPropertiesConfig.getString("bse.mfOrderEntry.xsipOrderEntryParam.action"))
          }
        }).asInstanceOf[XsipOrderEntryParamResponse]

        bSERequestLogRepository.saveXSipOrderEntryRequest(xsipOrderEntryParam, fcXsipOrderEntry.uniqueRefNo, userName)
        bSEResponseLogRepository.saveXSipOrderEntryResponse(xsipOrderEntryParamResponse, fcXsipOrderEntry.uniqueRefNo, userName)
        log.debug("Xsip Response Received")
        val bseXsipOrderValidateWrapper: BSEXsipOrderValidateWrapper = BSEOrderResponseHelper.convertBSEResponse(xsipOrderEntryParamResponse)
        bseXsipOrderValidateWrapper
      })

    }

  }

  /**
    * Gets Spread Order Entry Response
    * from BSE Web Service
    */
  def getSpreadOrderEntryParamResponse(fCSpreadOrderEntryModel: FCSpreadOrderEntryModel, userName:String): Future[BSESpreadOrderValidateWrapper] = {

    fcService.getBSESpreadOrderEntryParam(fCSpreadOrderEntryModel).flatMap{bseSpreadOrderEntryParam =>

      passwordRepository.getBSEPassword(BSE_ORDER_API_PASS_CODE).map(encryptedPasswordTuple => {
        val encryptedPassword = encryptedPasswordTuple._1
        val passKey = encryptedPasswordTuple._3
        val updatedBseSpreadOrderEntryParam = bseSpreadOrderEntryParam.copy(passKey = passKey)

        val spreadOrderEntryParam: SpreadOrderEntryParam = bseStarpopulator.populateSpreadOrderEntryParam(updatedBseSpreadOrderEntryParam, encryptedPassword)

        val spreadOrderEntryParamResponse: SpreadOrderEntryParamResponse = bseOrderWebService.marshalSendAndReceive(spreadOrderEntryParam, new WebServiceMessageCallback() {

          override def doWithMessage(message: WebServiceMessage) = {
            log.debug("BSE Spread Order Entry Request Callback called.")

            val soapMessage = message.asInstanceOf[SoapMessage]
            val soapHeader = soapMessage.getSoapHeader

            val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
            val wsaTo = soapHeader.addHeaderElement(wsaToQName)
            wsaTo.setText(bseOrderWebService.getDefaultUri)

            val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
            val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
            wsaAction.setText(getPropertiesConfig.getString("bse.mfOrderEntry.spreadOrderEntryParam.action"))
          }
        }).asInstanceOf[SpreadOrderEntryParamResponse]

        bSERequestLogRepository.saveSpreadOrderEntryRequest(spreadOrderEntryParam, fCSpreadOrderEntryModel.uniqueRefNo, userName)
        bSEResponseLogRepository.saveSpreadOrderEntryResponse(spreadOrderEntryParamResponse, fCSpreadOrderEntryModel.uniqueRefNo, userName)
        val bseSpreadOrderValidateWrapper: BSESpreadOrderValidateWrapper = BSEOrderResponseHelper.convertBSEResponse(spreadOrderEntryParamResponse)
        bseSpreadOrderValidateWrapper
      })
    }

  }

  /**
    * Gets Switch Order Entry Response
    * from BSE Web Service
    */
  def getSwitchOrderEntryParamResponse(fcSwitchOrderEntryModel: FCSwitchOrderEntryModel, userName:String): Future[BSESwitchOrderValidateWrapper] = {

    fcService.getBSESwitchOrderEntryParam(fcSwitchOrderEntryModel).flatMap{bseSwitchOrderEntryParam =>

      passwordRepository.getBSEPassword(BSE_ORDER_API_PASS_CODE).map(encryptedPasswordTuple => {
        val encryptedPassword = encryptedPasswordTuple._1
        val passKey = encryptedPasswordTuple._3
        val updatedBseSwitchOrderEntryParam = bseSwitchOrderEntryParam.copy(passKey = passKey)

        val switchOrderEntryParam: SwitchOrderEntryParam = bseStarpopulator.populateSwitchOrderEntryParam(updatedBseSwitchOrderEntryParam, encryptedPassword)

        val switchOrderEntryParamResponse: SwitchOrderEntryParamResponse = bseOrderWebService.marshalSendAndReceive(switchOrderEntryParam, new WebServiceMessageCallback() {

          override def doWithMessage(message: WebServiceMessage) = {
            log.debug("BSE Switch Order Entry Request Callback called.")

            val soapMessage = message.asInstanceOf[SoapMessage]
            val soapHeader = soapMessage.getSoapHeader

            val wsaToQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "To", "wsa")
            val wsaTo = soapHeader.addHeaderElement(wsaToQName)
            wsaTo.setText(bseOrderWebService.getDefaultUri)

            val wsaActionQName = new QName(getPropertiesConfig.getString("bse.ws.qName"), "Action", "wsa")
            val wsaAction = soapHeader.addHeaderElement(wsaActionQName)
            wsaAction.setText(getPropertiesConfig.getString("bse.mfOrderEntry.switchOrderEntryParam.action"))
          }
        }).asInstanceOf[SwitchOrderEntryParamResponse]

        bSERequestLogRepository.saveSwitchOrderEntryRequest(switchOrderEntryParam, fcSwitchOrderEntryModel.uniqueRefNo, userName)
        bSEResponseLogRepository.saveSwitchOrderEntryResponse(switchOrderEntryParamResponse, fcSwitchOrderEntryModel.uniqueRefNo, userName)
        val bseSwitchOrderValidateWrapper: BSESwitchOrderValidateWrapper = BSEOrderResponseHelper.convertBSEResponse(switchOrderEntryParamResponse)
        bseSwitchOrderValidateWrapper
      })
    }

  }
}