package helpers.integration.bse

import com.fincash.integration.ws.client.bsestar.{GetPassword, _}

import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 23-01-2017.
  */
object OrderRequestLogHelper {

  def getPasswordParameters(getPassword: GetPassword):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(getPassword.getUserId.getName.getLocalPart)
    paramValueList.+=(getPassword.getUserId.getValue)

    paramNameList.+=(getPassword.getPassword.getName.getLocalPart)
    paramValueList.+=(getPassword.getPassword.getValue)

    paramNameList.+=(getPassword.getPassKey.getName.getLocalPart)
    paramValueList.+=(getPassword.getPassKey.getValue)

    (paramNameList,paramValueList)
  }
  def getOrderParameters(orderEntryParam:OrderEntryParam):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(orderEntryParam.getTransCode.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getTransCode.getValue)

    paramNameList.+= (orderEntryParam.getTransNo.getName.getLocalPart)
    paramValueList.+= (orderEntryParam.getTransNo.getValue)

    paramNameList.+=(orderEntryParam.getOrderId.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getOrderId.getValue)

    paramNameList.+=(orderEntryParam.getUserID.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getUserID.getValue)

    paramNameList.+=(orderEntryParam.getMemberId.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getMemberId.getValue)

    paramNameList.+=(orderEntryParam.getClientCode.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getClientCode.getValue)

    paramNameList.+=(orderEntryParam.getSchemeCd.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getSchemeCd.getValue)

    paramNameList.+=(orderEntryParam.getBuySell.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getBuySell.getValue)

    paramNameList.+=(orderEntryParam.getBuySellType.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getBuySellType.getValue)

    paramNameList.+=(orderEntryParam.getDPTxn.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getDPTxn.getValue)

    paramNameList.+=(orderEntryParam.getOrderVal.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getOrderVal.getValue)

    paramNameList.+=(orderEntryParam.getQty.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getQty.getValue)

    paramNameList.+=(orderEntryParam.getAllRedeem.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getAllRedeem.getValue)

    paramNameList.+=(orderEntryParam.getFolioNo.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getFolioNo.getValue)

    paramNameList.+=(orderEntryParam.getRemarks.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getRemarks.getValue)

    paramNameList.+=(orderEntryParam.getKYCStatus.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getKYCStatus.getValue)

    paramNameList.+=(orderEntryParam.getRefNo.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getRefNo.getValue)

    paramNameList.+=(orderEntryParam.getSubBrCode.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getSubBrCode.getValue)

    paramNameList.+=(orderEntryParam.getEUIN.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getEUIN.getValue)

    paramNameList.+=(orderEntryParam.getEUINVal.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getEUINVal.getValue)

    paramNameList.+=(orderEntryParam.getMinRedeem.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getMinRedeem.getValue)

    paramNameList.+=(orderEntryParam.getDPC.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getDPC.getValue)

    paramNameList.+=(orderEntryParam.getIPAdd.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getIPAdd.getValue)

    paramNameList.+=(orderEntryParam.getPassword.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getPassword.getValue)

    paramNameList.+=(orderEntryParam.getPassKey.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getPassKey.getValue)

    paramNameList.+=(orderEntryParam.getParma1.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getParma1.getValue)

    paramNameList.+=(orderEntryParam.getParam2.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getParam2.getValue)

    paramNameList.+=(orderEntryParam.getParam3.getName.getLocalPart)
    paramValueList.+=(orderEntryParam.getParam3.getValue)

    (paramNameList,paramValueList)
  }

  def getSipOrderParameters(sipOrderEntryParam:SipOrderEntryParam):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(sipOrderEntryParam.getTransactionCode.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getTransactionCode.getValue)

    paramNameList.+=(sipOrderEntryParam.getUniqueRefNo.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getUniqueRefNo.getValue)

    paramNameList.+=(sipOrderEntryParam.getSchemeCode.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getSchemeCode.getValue)

    paramNameList.+=(sipOrderEntryParam.getMemberCode.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getMemberCode.getValue)

    paramNameList.+=(sipOrderEntryParam.getClientCode.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getClientCode.getValue)

    paramNameList.+=(sipOrderEntryParam.getUserID.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getUserID.getValue)

    paramNameList.+=(sipOrderEntryParam.getInternalRefNo.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getInternalRefNo.getValue)

    paramNameList.+=(sipOrderEntryParam.getTransMode.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getTransMode.getValue)

    paramNameList.+=(sipOrderEntryParam.getDpTxnMode.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getDpTxnMode.getValue)

    paramNameList.+=(sipOrderEntryParam.getStartDate.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getStartDate.getValue)

    paramNameList.+=(sipOrderEntryParam.getFrequencyType.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getFrequencyType.getValue)

    paramNameList.+=(sipOrderEntryParam.getFrequencyAllowed.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getFrequencyAllowed.getValue)

    paramNameList.+=(sipOrderEntryParam.getInstallmentAmount.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getInstallmentAmount.getValue)

    paramNameList.+=(sipOrderEntryParam.getNoOfInstallment.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getNoOfInstallment.getValue)

    paramNameList.+=(sipOrderEntryParam.getRemarks.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getRemarks.getValue)

    paramNameList.+=(sipOrderEntryParam.getFolioNo.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getFolioNo.getValue)

    paramNameList.+=(sipOrderEntryParam.getFirstOrderFlag.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getFirstOrderFlag.getValue)

    paramNameList.+=(sipOrderEntryParam.getSubberCode.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getSubberCode.getValue)

    paramNameList.+=(sipOrderEntryParam.getEuin.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getEuin.getValue)

    paramNameList.+=(sipOrderEntryParam.getEuinVal.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getEuinVal.getValue)

    paramNameList.+=(sipOrderEntryParam.getDPC.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getDPC.getValue)

    paramNameList.+=(sipOrderEntryParam.getRegId.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getRegId.getValue)

    paramNameList.+=(sipOrderEntryParam.getIPAdd.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getIPAdd.getValue)

    paramNameList.+=(sipOrderEntryParam.getPassword.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getPassword.getValue)

    paramNameList.+=(sipOrderEntryParam.getPassKey.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getPassKey.getValue)

    paramNameList.+=(sipOrderEntryParam.getParam1.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getParam1.getValue)

    paramNameList.+=(sipOrderEntryParam.getParam2.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getParam2.getValue)

    paramNameList.+=(sipOrderEntryParam.getParam3.getName.getLocalPart)
    paramValueList.+=(sipOrderEntryParam.getParam3.getValue)

    (paramNameList,paramValueList)
  }

  def getXSipOrderParameters(xsipOrderEntryParam: XsipOrderEntryParam):(ListBuffer[String],ListBuffer[String]) = {

    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(xsipOrderEntryParam.getTransactionCode.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getTransactionCode.getValue)

    paramNameList.+=(xsipOrderEntryParam.getUniqueRefNo.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getUniqueRefNo.getValue)

    paramNameList.+=(xsipOrderEntryParam.getSchemeCode.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getSchemeCode.getValue)

    paramNameList.+=(xsipOrderEntryParam.getMemberCode.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getMemberCode.getValue)

    paramNameList.+=(xsipOrderEntryParam.getClientCode.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getClientCode.getValue)

    paramNameList.+=(xsipOrderEntryParam.getUserId.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getUserId.getValue)

    paramNameList.+=(xsipOrderEntryParam.getInternalRefNo.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getInternalRefNo.getValue)

    paramNameList.+=(xsipOrderEntryParam.getTransMode.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getTransMode.getValue)

    paramNameList.+=(xsipOrderEntryParam.getDpTxnMode.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getDpTxnMode.getValue)

    paramNameList.+=(xsipOrderEntryParam.getStartDate.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getStartDate.getValue)

    paramNameList.+=(xsipOrderEntryParam.getFrequencyType.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getFrequencyType.getValue)

    paramNameList.+=(xsipOrderEntryParam.getFrequencyAllowed.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getFrequencyAllowed.getValue)

    paramNameList.+=(xsipOrderEntryParam.getInstallmentAmount.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getInstallmentAmount.getValue)

    paramNameList.+=(xsipOrderEntryParam.getNoOfInstallment.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getNoOfInstallment.getValue)

    paramNameList.+=(xsipOrderEntryParam.getRemarks.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getRemarks.getValue)

    paramNameList.+=(xsipOrderEntryParam.getFolioNo.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getFolioNo.getValue)

    paramNameList.+=(xsipOrderEntryParam.getFirstOrderFlag.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getFirstOrderFlag.getValue)

    paramNameList.+=(xsipOrderEntryParam.getBrokerage.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getBrokerage.getValue)

    paramNameList.+=(xsipOrderEntryParam.getMandateID.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getMandateID.getValue)

    paramNameList.+=(xsipOrderEntryParam.getSubberCode.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getSubberCode.getValue)

    paramNameList.+=(xsipOrderEntryParam.getEuin.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getEuin.getValue)

    paramNameList.+=(xsipOrderEntryParam.getEuinVal.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getEuinVal.getValue)

    paramNameList.+=(xsipOrderEntryParam.getDPC.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getDPC.getValue)

    paramNameList.+=(xsipOrderEntryParam.getXsipRegID.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getXsipRegID.getValue)

    paramNameList.+=(xsipOrderEntryParam.getIPAdd.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getIPAdd.getValue)

    paramNameList.+=(xsipOrderEntryParam.getPassword.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getPassword.getValue)

    paramNameList.+=(xsipOrderEntryParam.getPassKey.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getPassKey.getValue)

    paramNameList.+=(xsipOrderEntryParam.getParam1.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getParam1.getValue)

    paramNameList.+=(xsipOrderEntryParam.getParam2.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getParam2.getValue)

    paramNameList.+=(xsipOrderEntryParam.getParam3.getName.getLocalPart)
    paramValueList.+=(xsipOrderEntryParam.getParam3.getValue)

    (paramNameList,paramValueList)
  }

  def getSpreadOrderParameters(spreadOrderEntryParam:SpreadOrderEntryParam):(ListBuffer[String],ListBuffer[String]) = {
    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(spreadOrderEntryParam.getTransactionCode.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getTransactionCode.getValue)

    paramNameList.+=(spreadOrderEntryParam.getUniqueRefNo.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getUniqueRefNo.getValue)

    paramNameList.+=(spreadOrderEntryParam.getOrderID.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getOrderID.getValue)

    paramNameList.+=(spreadOrderEntryParam.getUserID.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getUserID.getValue)

    paramNameList.+=(spreadOrderEntryParam.getMemberId.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getMemberId.getValue)

    paramNameList.+=(spreadOrderEntryParam.getClientCode.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getClientCode.getValue)

    paramNameList.+=(spreadOrderEntryParam.getSchemeCode.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getSchemeCode.getValue)

    paramNameList.+=(spreadOrderEntryParam.getBuySell.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getBuySell.getValue)

    paramNameList.+=(spreadOrderEntryParam.getBuySellType.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getBuySellType.getValue)

    paramNameList.+=(spreadOrderEntryParam.getDPTxn.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getDPTxn.getValue)

    paramNameList.+=(spreadOrderEntryParam.getOrderValue.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getOrderValue.getValue)

    paramNameList.+=(spreadOrderEntryParam.getRedemptionAmt.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getRedemptionAmt.getValue)

    paramNameList.+=(spreadOrderEntryParam.getAllUnitFlag.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getAllUnitFlag.getValue)

    paramNameList.+=(spreadOrderEntryParam.getRedeemDate.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getRedeemDate.getValue)

    paramNameList.+=(spreadOrderEntryParam.getFolioNo.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getFolioNo.getValue)

    paramNameList.+=(spreadOrderEntryParam.getRemarks.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getRemarks.getValue)

    paramNameList.+=(spreadOrderEntryParam.getKYCStatus.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getKYCStatus.getValue)

    paramNameList.+=(spreadOrderEntryParam.getRefNo.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getRefNo.getValue)

    paramNameList.+=(spreadOrderEntryParam.getSubBroCode.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getSubBroCode.getValue)

    paramNameList.+=(spreadOrderEntryParam.getEUIN.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getEUIN.getValue)

    paramNameList.+=(spreadOrderEntryParam.getEUINVal.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getEUINVal.getValue)

    paramNameList.+=(spreadOrderEntryParam.getMinRedeem.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getMinRedeem.getValue)

    paramNameList.+=(spreadOrderEntryParam.getDPC.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getDPC.getValue)

    paramNameList.+=(spreadOrderEntryParam.getIPAddress.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getIPAddress.getValue)

    paramNameList.+=(spreadOrderEntryParam.getPassword.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getPassword.getValue)

    paramNameList.+=(spreadOrderEntryParam.getPassKey.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getPassKey.getValue)

    paramNameList.+=(spreadOrderEntryParam.getParam1.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getParam1.getValue)

    paramNameList.+=(spreadOrderEntryParam.getParam2.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getParam2.getValue)

    paramNameList.+=(spreadOrderEntryParam.getParam3.getName.getLocalPart)
    paramValueList.+=(spreadOrderEntryParam.getParam3.getValue)

    (paramNameList,paramValueList)
  }

  def getSwitchOrderParameters(switchOrderEntryParam:SwitchOrderEntryParam):(ListBuffer[String],ListBuffer[String]) = {
    val paramNameList:ListBuffer[String] = ListBuffer[String]()
    val paramValueList:ListBuffer[String] = ListBuffer[String]()

    paramNameList.+=(switchOrderEntryParam.getTransCode.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getTransCode.getValue)

    paramNameList.+=(switchOrderEntryParam.getTransNo.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getTransNo.getValue)

    paramNameList.+=(switchOrderEntryParam.getOrderId.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getOrderId.getValue)

    paramNameList.+=(switchOrderEntryParam.getUserId.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getUserId.getValue)

    paramNameList.+=(switchOrderEntryParam.getMemberId.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getMemberId.getValue)

    paramNameList.+=(switchOrderEntryParam.getClientCode.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getClientCode.getValue)

    paramNameList.+=(switchOrderEntryParam.getFromSchemeCd.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getFromSchemeCd.getValue)

    paramNameList.+=(switchOrderEntryParam.getToSchemeCd.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getToSchemeCd.getValue)

    paramNameList.+=(switchOrderEntryParam.getBuySell.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getBuySell.getValue)

    paramNameList.+=(switchOrderEntryParam.getBuySellType.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getBuySellType.getValue)

    paramNameList.+=(switchOrderEntryParam.getDPTxn.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getDPTxn.getValue)

    paramNameList.+=(switchOrderEntryParam.getOrderVal.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getOrderVal.getValue)

    paramNameList.+=(switchOrderEntryParam.getSwitchUnits.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getSwitchUnits.getValue)

    paramNameList.+=(switchOrderEntryParam.getAllUnitsFlag.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getAllUnitsFlag.getValue)

    paramNameList.+=(switchOrderEntryParam.getFolioNo.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getFolioNo.getValue)

    paramNameList.+=(switchOrderEntryParam.getRemarks.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getRemarks.getValue)

    paramNameList.+=(switchOrderEntryParam.getKYCStatus.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getKYCStatus.getValue)

    paramNameList.+=(switchOrderEntryParam.getSubBrCode.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getSubBrCode.getValue)

    paramNameList.+=(switchOrderEntryParam.getEUIN.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getEUIN.getValue)

    paramNameList.+=(switchOrderEntryParam.getEUINVal.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getEUINVal.getValue)

    paramNameList.+=(switchOrderEntryParam.getMinRedeem.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getMinRedeem.getValue)

    paramNameList.+=(switchOrderEntryParam.getIPAdd.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getIPAdd.getValue)

    paramNameList.+=(switchOrderEntryParam.getPassword.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getPassword.getValue)

    paramNameList.+=(switchOrderEntryParam.getPassKey.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getPassKey.getValue)

    paramNameList.+=(switchOrderEntryParam.getParma1.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getParma1.getValue)

    paramNameList.+=(switchOrderEntryParam.getParam2.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getParam2.getValue)

    paramNameList.+=(switchOrderEntryParam.getParam3.getName.getLocalPart)
    paramValueList.+=(switchOrderEntryParam.getParam3.getValue)

    (paramNameList,paramValueList)
  }
}
