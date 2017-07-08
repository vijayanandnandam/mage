package repository.module.integration.bse

import com.fincash.integration.ws.client.bsestar.upload.MFAPIResponse
import com.fincash.integration.ws.client.bsestar.{GetPasswordResponse, SipOrderEntryParamResponse, SpreadOrderEntryParamResponse, SwitchOrderEntryParamResponse, XsipOrderEntryParamResponse, _}
import com.google.inject.Inject
import helpers.integration.bse.{AdditionalResponseLogHelper, OrderResponseLogHelper}
import repository.module.IntegrationRepository

import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 24-01-2017.
  */
class BSEResponseLogRepository @Inject()(integrationRepository: IntegrationRepository) {

  def saveGetPasswordResponse(getPasswordResponse: GetPasswordResponse, integrationId: String, userName: String) = {

    val paramListTuple = OrderResponseLogHelper.getPasswordParameters(getPasswordResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  def saveOrderEntryResponse(orderEntryParamResponse: OrderEntryParamResponse, integrationId: String, userName: String) = {

    val paramListTuple = OrderResponseLogHelper.getOrderParameters(orderEntryParamResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  def saveSipOrderEntryResponse(sipOrderEntryParamResponse: SipOrderEntryParamResponse, integrationId: String, userName: String) = {

    val paramListTuple = OrderResponseLogHelper.getSipOrderParameters(sipOrderEntryParamResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  def saveXSipOrderEntryResponse(xsipOrderEntryParamResponse: XsipOrderEntryParamResponse, integrationId: String, userName: String) = {

    val paramListTuple = OrderResponseLogHelper.getXSipOrderParameters(xsipOrderEntryParamResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  def saveSpreadOrderEntryResponse(spreadOrderEntryParamResponse: SpreadOrderEntryParamResponse, integrationId: String, userName: String) = {

    val paramListTuple = OrderResponseLogHelper.getSpreadOrderParameters(spreadOrderEntryParamResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  def saveSwitchOrderEntryResponse(switchOrderEntryParamResponse: SwitchOrderEntryParamResponse, integrationId: String, userName: String) = {

    val paramListTuple = OrderResponseLogHelper.getSwitchOrderParameters(switchOrderEntryParamResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  def saveMfApiGetPasswordResponse(passwordResponse: com.fincash.integration.ws.client.bsestar.upload.GetPasswordResponse, integrationId: String, userName: String) = {

    val paramListTuple = AdditionalResponseLogHelper.getPasswordParameters(passwordResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  def saveMfApiResponse(mFAPIResponse: MFAPIResponse, integrationId: String, userName: String) = {

    val paramListTuple = AdditionalResponseLogHelper.getMfApiParameters(mFAPIResponse)

    saveResponseLog(paramListTuple, integrationId, userName)
  }

  private def saveResponseLog(paramListTuple: (ListBuffer[String], ListBuffer[String]), integrationId: String, userName: String) = {

    val paramNameList: ListBuffer[String] = paramListTuple._1
    val paramValueList: ListBuffer[String] = paramListTuple._2

    integrationRepository.saveBSEResponseParameters(Some(integrationId), paramNameList, paramValueList, userName)
  }
}
