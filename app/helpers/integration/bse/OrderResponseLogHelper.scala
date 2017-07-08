package helpers.integration.bse

import com.fincash.integration.ws.client.bsestar._

import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 24-01-2017.
  */
object OrderResponseLogHelper {

  def getPasswordParameters(getPasswordResponse: GetPasswordResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(getPasswordResponse.getGetPasswordResult.getName.getLocalPart)
    paramValueList.+=(getPasswordResponse.getGetPasswordResult.getValue)

    (paramNameList,paramValueList)

  }
  def getOrderParameters(orderEntryParamResponse: OrderEntryParamResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(orderEntryParamResponse.getOrderEntryParamResult.getName.getLocalPart)
    paramValueList.+=(orderEntryParamResponse.getOrderEntryParamResult.getValue)

    (paramNameList,paramValueList)
  }

  def getSipOrderParameters(sipOrderEntryParamResponse: SipOrderEntryParamResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(sipOrderEntryParamResponse.getSipOrderEntryParamResult.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParamResponse.getSipOrderEntryParamResult.getValue)

    (paramNameList,paramValueList)
  }

  def getXSipOrderParameters(xsipOrderEntryParamResponse: XsipOrderEntryParamResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(xsipOrderEntryParamResponse.getXsipOrderEntryParamResult.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParamResponse.getXsipOrderEntryParamResult.getValue)

    (paramNameList,paramValueList)
  }

  def getSpreadOrderParameters(spreadOrderEntryParamResponse: SpreadOrderEntryParamResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(spreadOrderEntryParamResponse.getSpreadOrderEntryParamResult.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParamResponse.getSpreadOrderEntryParamResult.getValue)

    (paramNameList,paramValueList)
  }

  def getSwitchOrderParameters(switchOrderEntryParamResponse: SwitchOrderEntryParamResponse):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(switchOrderEntryParamResponse.getSwitchOrderEntryParamResult.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParamResponse.getSwitchOrderEntryParamResult.getValue)

    (paramNameList,paramValueList)
  }
}
