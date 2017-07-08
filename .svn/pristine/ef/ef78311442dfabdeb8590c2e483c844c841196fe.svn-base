package service.integration

import javax.inject.Inject

import com.fincash.integration.ws.client.bsestar.ObjectFactory
import com.google.inject.name.Named
import com.fincash.integration.ws.client.bsestar.SpreadOrderEntryParam

import com.fincash.integration.ws.client.bsestar.GetPassword
import com.fincash.integration.ws.client.bsestar.SwitchOrderEntryParam
import com.fincash.integration.ws.client.bsestar.SipOrderEntryParam
import com.fincash.integration.ws.client.bsestar.OrderEntryParam
import com.fincash.integration.ws.client.bsestar.XsipOrderEntryParam
import models.integration.BSESpreadOrderEntryParam
import models.integration.BSEXsipOrderEntryParam
import models.integration.BSESipOrderEntryParam
import models.integration.BSESwitchOrderEntryParam
import models.integration.BSEGetPasswordResponse
import models.integration.BSEGetPassword
import models.integration.BSEOrderEntryParam


class BSEStarOrderPopulator @Inject()(@Named("bseOrderObjectFactory")bseOrderObjectFactory:ObjectFactory){
  
  def populateGetPassword(getBSEPassword:BSEGetPassword):GetPassword = {
    val getPassword = new GetPassword
    
    getPassword.setUserId(bseOrderObjectFactory.createGetPasswordUserId(getBSEPassword.userId))
    getPassword.setPassword(bseOrderObjectFactory.createGetPasswordPassword(getBSEPassword.password))
    getPassword.setPassKey(bseOrderObjectFactory.createGetPasswordPassKey(getBSEPassword.passKey))
    
    getPassword
  }
  
  def populateOrderEntryParam(bseOrderEntryParam:BSEOrderEntryParam,encryptedPassword:String):OrderEntryParam = {
    
    val orderEntryParam = new OrderEntryParam
    
    orderEntryParam.setTransCode(bseOrderObjectFactory.createOrderEntryParamTransCode(bseOrderEntryParam.transCode.toString))
    orderEntryParam.setTransNo(bseOrderObjectFactory.createOrderEntryParamTransNo(bseOrderEntryParam.transNo))
    orderEntryParam.setOrderId(bseOrderObjectFactory.createOrderEntryParamOrderId(bseOrderEntryParam.orderId.getOrElse("").toString))
    orderEntryParam.setUserID(bseOrderObjectFactory.createOrderEntryParamUserID(bseOrderEntryParam.userId.toString))
    orderEntryParam.setMemberId(bseOrderObjectFactory.createOrderEntryParamMemberId(bseOrderEntryParam.memberId))
    orderEntryParam.setClientCode(bseOrderObjectFactory.createOrderEntryParamClientCode(bseOrderEntryParam.clientCode))
    orderEntryParam.setSchemeCd(bseOrderObjectFactory.createOrderEntryParamSchemeCd(bseOrderEntryParam.schemeCd))
    orderEntryParam.setBuySell(bseOrderObjectFactory.createOrderEntryParamBuySell(bseOrderEntryParam.buySell))
    orderEntryParam.setBuySellType(bseOrderObjectFactory.createOrderEntryParamBuySellType(bseOrderEntryParam.buySellType.toString))
    orderEntryParam.setDPTxn(bseOrderObjectFactory.createOrderEntryParamDPTxn(bseOrderEntryParam.dpTxn.toString))
    orderEntryParam.setOrderVal(bseOrderObjectFactory.createOrderEntryParamOrderVal(bseOrderEntryParam.orderVal.getOrElse("").toString))
    orderEntryParam.setQty(bseOrderObjectFactory.createOrderEntryParamQty(bseOrderEntryParam.qty.getOrElse("").toString))
    orderEntryParam.setAllRedeem(bseOrderObjectFactory.createOrderEntryParamAllRedeem(bseOrderEntryParam.allRedeem.toString))
    orderEntryParam.setFolioNo(bseOrderObjectFactory.createOrderEntryParamFolioNo(bseOrderEntryParam.folioNo.getOrElse("")))
    orderEntryParam.setRemarks(bseOrderObjectFactory.createOrderEntryParamRemarks(bseOrderEntryParam.remarks.getOrElse("")))
    orderEntryParam.setKYCStatus(bseOrderObjectFactory.createOrderEntryParamKYCStatus(bseOrderEntryParam.kycStatus))
    orderEntryParam.setRefNo(bseOrderObjectFactory.createOrderEntryParamRefNo(bseOrderEntryParam.refNo.getOrElse("")))
    orderEntryParam.setSubBrCode(bseOrderObjectFactory.createOrderEntryParamSubBrCode(bseOrderEntryParam.subBrCode.getOrElse("")))
    orderEntryParam.setEUIN(bseOrderObjectFactory.createOrderEntryParamEUIN(bseOrderEntryParam.euin.getOrElse("")))
    orderEntryParam.setEUINVal(bseOrderObjectFactory.createOrderEntryParamEUINVal(bseOrderEntryParam.euinVal.toString))
    orderEntryParam.setMinRedeem(bseOrderObjectFactory.createOrderEntryParamMinRedeem(bseOrderEntryParam.minRedeem.toString))
    orderEntryParam.setDPC(bseOrderObjectFactory.createOrderEntryParamDPC(bseOrderEntryParam.dpc.toString))
    orderEntryParam.setIPAdd(bseOrderObjectFactory.createOrderEntryParamIPAdd(bseOrderEntryParam.ipAdd))
    orderEntryParam.setPassword(bseOrderObjectFactory.createOrderEntryParamPassword(encryptedPassword))
    orderEntryParam.setPassKey(bseOrderObjectFactory.createOrderEntryParamPassKey(bseOrderEntryParam.passKey))
    orderEntryParam.setParma1(bseOrderObjectFactory.createOrderEntryParamParma1(bseOrderEntryParam.param1.getOrElse("")))
    orderEntryParam.setParam2(bseOrderObjectFactory.createOrderEntryParamParam2(bseOrderEntryParam.param2.getOrElse("")))
    orderEntryParam.setParam3(bseOrderObjectFactory.createOrderEntryParamParam3(bseOrderEntryParam.param3.getOrElse("")))
    
    orderEntryParam
  }
  
  def populateSipOrderEntryParam(bseSipOrderEntryParam:BSESipOrderEntryParam,encryptedPassword:String):SipOrderEntryParam = {
    val sipOrderEntryParam = new SipOrderEntryParam
    
    sipOrderEntryParam.setTransactionCode(bseOrderObjectFactory.createSipOrderEntryParamTransactionCode(bseSipOrderEntryParam.transCode.toString))
    sipOrderEntryParam.setUniqueRefNo(bseOrderObjectFactory.createSipOrderEntryParamUniqueRefNo(bseSipOrderEntryParam.uniqueRefNo))
    sipOrderEntryParam.setSchemeCode(bseOrderObjectFactory.createSipOrderEntryParamSchemeCode(bseSipOrderEntryParam.schemeCd))
    sipOrderEntryParam.setMemberCode(bseOrderObjectFactory.createSipOrderEntryParamMemberCode(bseSipOrderEntryParam.memberCode))
    sipOrderEntryParam.setClientCode(bseOrderObjectFactory.createSipOrderEntryParamClientCode(bseSipOrderEntryParam.clientCode))
    sipOrderEntryParam.setUserID(bseOrderObjectFactory.createSipOrderEntryParamUserID(bseSipOrderEntryParam.userId.toString))
    sipOrderEntryParam.setInternalRefNo(bseOrderObjectFactory.createSipOrderEntryParamInternalRefNo(bseSipOrderEntryParam.internalRefNo.getOrElse("")))
    sipOrderEntryParam.setTransMode(bseOrderObjectFactory.createSipOrderEntryParamTransMode(bseSipOrderEntryParam.transMode.toString))
    sipOrderEntryParam.setDpTxnMode(bseOrderObjectFactory.createSipOrderEntryParamDpTxnMode(bseSipOrderEntryParam.dpTxnMode.toString))
    sipOrderEntryParam.setStartDate(bseOrderObjectFactory.createSipOrderEntryParamStartDate(bseSipOrderEntryParam.startDate))
    sipOrderEntryParam.setFrequencyType(bseOrderObjectFactory.createSipOrderEntryParamFrequencyType(bseSipOrderEntryParam.frequencyType.toString))
    sipOrderEntryParam.setFrequencyAllowed(bseOrderObjectFactory.createSipOrderEntryParamFrequencyAllowed(bseSipOrderEntryParam.frequencyAllowed.toString))
    sipOrderEntryParam.setInstallmentAmount(bseOrderObjectFactory.createSipOrderEntryParamInstallmentAmount(bseSipOrderEntryParam.installmentAmount.toString))
    sipOrderEntryParam.setNoOfInstallment(bseOrderObjectFactory.createSipOrderEntryParamNoOfInstallment(bseSipOrderEntryParam.noOfInstallments.toString))
    sipOrderEntryParam.setRemarks(bseOrderObjectFactory.createSipOrderEntryParamRemarks(bseSipOrderEntryParam.remarks.getOrElse("")))
    sipOrderEntryParam.setFolioNo(bseOrderObjectFactory.createSipOrderEntryParamFolioNo(bseSipOrderEntryParam.folioNo.getOrElse("")))
    sipOrderEntryParam.setFirstOrderFlag(bseOrderObjectFactory.createSipOrderEntryParamFirstOrderFlag(bseSipOrderEntryParam.firstOrderFlag.toString))
    sipOrderEntryParam.setSubberCode(bseOrderObjectFactory.createSipOrderEntryParamSubberCode(bseSipOrderEntryParam.subBrCode.getOrElse("")))
    sipOrderEntryParam.setEuin(bseOrderObjectFactory.createSipOrderEntryParamEuin(bseSipOrderEntryParam.euin.getOrElse("")))
    sipOrderEntryParam.setEuinVal(bseOrderObjectFactory.createSipOrderEntryParamEuinVal(bseSipOrderEntryParam.euinFlag.toString))
    sipOrderEntryParam.setDPC(bseOrderObjectFactory.createSipOrderEntryParamDPC(bseSipOrderEntryParam.dpc.toString))
    sipOrderEntryParam.setRegId(bseOrderObjectFactory.createSipOrderEntryParamRegId(bseSipOrderEntryParam.regId.getOrElse("").toString))
    sipOrderEntryParam.setIPAdd(bseOrderObjectFactory.createSipOrderEntryParamIPAdd(bseSipOrderEntryParam.ipAdd))
    sipOrderEntryParam.setPassword(bseOrderObjectFactory.createSipOrderEntryParamPassword(encryptedPassword))
    sipOrderEntryParam.setPassKey(bseOrderObjectFactory.createSipOrderEntryParamPassKey(bseSipOrderEntryParam.passKey))
    sipOrderEntryParam.setParam1(bseOrderObjectFactory.createSipOrderEntryParamParam1(bseSipOrderEntryParam.param1.getOrElse("")))
    sipOrderEntryParam.setParam2(bseOrderObjectFactory.createSipOrderEntryParamParam2(bseSipOrderEntryParam.param2.getOrElse("")))
    sipOrderEntryParam.setParam3(bseOrderObjectFactory.createSipOrderEntryParamParam3(bseSipOrderEntryParam.param3.getOrElse("")))
    
    sipOrderEntryParam
  }
  
  def populateXsipOrderEntryParam(bseXsipOrderEntryParam:BSEXsipOrderEntryParam,encryptedPassword:String):XsipOrderEntryParam = {
    
    val xSipOrderEntryParam = new XsipOrderEntryParam
    
    xSipOrderEntryParam.setTransactionCode(bseOrderObjectFactory.createXsipOrderEntryParamTransactionCode(bseXsipOrderEntryParam.transCode.toString))
    xSipOrderEntryParam.setUniqueRefNo(bseOrderObjectFactory.createXsipOrderEntryParamUniqueRefNo(bseXsipOrderEntryParam.uniqueRefNo))
    xSipOrderEntryParam.setSchemeCode(bseOrderObjectFactory.createXsipOrderEntryParamSchemeCode(bseXsipOrderEntryParam.schemeCd))
    xSipOrderEntryParam.setMemberCode(bseOrderObjectFactory.createXsipOrderEntryParamMemberCode(bseXsipOrderEntryParam.memberCode))
    xSipOrderEntryParam.setClientCode(bseOrderObjectFactory.createXsipOrderEntryParamClientCode(bseXsipOrderEntryParam.clientCode))
    xSipOrderEntryParam.setUserId(bseOrderObjectFactory.createXsipOrderEntryParamUserId(bseXsipOrderEntryParam.userId.toString))
    xSipOrderEntryParam.setInternalRefNo(bseOrderObjectFactory.createXsipOrderEntryParamInternalRefNo(bseXsipOrderEntryParam.internalRefNo.getOrElse("")))
    xSipOrderEntryParam.setTransMode(bseOrderObjectFactory.createXsipOrderEntryParamTransMode(bseXsipOrderEntryParam.transMode.toString))
    xSipOrderEntryParam.setDpTxnMode(bseOrderObjectFactory.createXsipOrderEntryParamDpTxnMode(bseXsipOrderEntryParam.dpTxnMode.toString))
    xSipOrderEntryParam.setStartDate(bseOrderObjectFactory.createXsipOrderEntryParamStartDate(bseXsipOrderEntryParam.startDate))
    xSipOrderEntryParam.setFrequencyType(bseOrderObjectFactory.createXsipOrderEntryParamFrequencyType(bseXsipOrderEntryParam.frequencyType))
    xSipOrderEntryParam.setFrequencyAllowed(bseOrderObjectFactory.createXsipOrderEntryParamFrequencyAllowed(bseXsipOrderEntryParam.frequencyAllowed.toString))
    xSipOrderEntryParam.setInstallmentAmount(bseOrderObjectFactory.createXsipOrderEntryParamInstallmentAmount(bseXsipOrderEntryParam.installmentAmount.toString))
    xSipOrderEntryParam.setNoOfInstallment(bseOrderObjectFactory.createXsipOrderEntryParamNoOfInstallment(bseXsipOrderEntryParam.noOfInstallments.toString))
    xSipOrderEntryParam.setRemarks(bseOrderObjectFactory.createXsipOrderEntryParamRemarks(bseXsipOrderEntryParam.remarks.getOrElse("")))
    xSipOrderEntryParam.setFolioNo(bseOrderObjectFactory.createXsipOrderEntryParamFolioNo(bseXsipOrderEntryParam.folioNo.getOrElse("")))
    xSipOrderEntryParam.setFirstOrderFlag(bseOrderObjectFactory.createXsipOrderEntryParamFirstOrderFlag(bseXsipOrderEntryParam.firstOrderFlag.toString))
    xSipOrderEntryParam.setBrokerage(bseOrderObjectFactory.createXsipOrderEntryParamBrokerage(bseXsipOrderEntryParam.brokerage.getOrElse("").toString))
    xSipOrderEntryParam.setMandateID(bseOrderObjectFactory.createXsipOrderEntryParamMandateID(bseXsipOrderEntryParam.mandateId.getOrElse("").toString()))
    xSipOrderEntryParam.setSubberCode(bseOrderObjectFactory.createXsipOrderEntryParamSubberCode(bseXsipOrderEntryParam.subBrCode.getOrElse("")))
    xSipOrderEntryParam.setEuin(bseOrderObjectFactory.createXsipOrderEntryParamEuin(bseXsipOrderEntryParam.euin.getOrElse("")))
    xSipOrderEntryParam.setEuinVal(bseOrderObjectFactory.createXsipOrderEntryParamEuinVal(bseXsipOrderEntryParam.euinFlag.toString))
    xSipOrderEntryParam.setDPC(bseOrderObjectFactory.createXsipOrderEntryParamDPC(bseXsipOrderEntryParam.dpc.toString))
    xSipOrderEntryParam.setXsipRegID(bseOrderObjectFactory.createXsipOrderEntryParamXsipRegID(bseXsipOrderEntryParam.xsipRegId.getOrElse("").toString))
    xSipOrderEntryParam.setIPAdd(bseOrderObjectFactory.createXsipOrderEntryParamIPAdd(bseXsipOrderEntryParam.ipAdd))
    xSipOrderEntryParam.setPassword(bseOrderObjectFactory.createXsipOrderEntryParamPassword(encryptedPassword))
    xSipOrderEntryParam.setPassKey(bseOrderObjectFactory.createXsipOrderEntryParamPassKey(bseXsipOrderEntryParam.passKey))
    xSipOrderEntryParam.setParam1(bseOrderObjectFactory.createXsipOrderEntryParamParam1(bseXsipOrderEntryParam.param1.getOrElse("")))
    xSipOrderEntryParam.setParam2(bseOrderObjectFactory.createXsipOrderEntryParamParam2(bseXsipOrderEntryParam.param2.getOrElse("")))
    xSipOrderEntryParam.setParam3(bseOrderObjectFactory.createXsipOrderEntryParamParam3(bseXsipOrderEntryParam.param3.getOrElse("")))
    
    xSipOrderEntryParam
  }
  
  def populateSpreadOrderEntryParam(bseSpreadOrderEntryParam:BSESpreadOrderEntryParam,encryptedPassword:String):SpreadOrderEntryParam = {
    val spreadOrderEntryParam = new SpreadOrderEntryParam
    
    spreadOrderEntryParam.setTransactionCode(bseOrderObjectFactory.createSpreadOrderEntryParamTransactionCode(bseSpreadOrderEntryParam.transCode.toString))
    spreadOrderEntryParam.setUniqueRefNo(bseOrderObjectFactory.createSpreadOrderEntryParamUniqueRefNo(bseSpreadOrderEntryParam.transNo))
    spreadOrderEntryParam.setOrderID(bseOrderObjectFactory.createSpreadOrderEntryParamOrderID(bseSpreadOrderEntryParam.orderId.getOrElse("").toString))
    spreadOrderEntryParam.setUserID(bseOrderObjectFactory.createSpreadOrderEntryParamUserID(bseSpreadOrderEntryParam.userId.toString))
    spreadOrderEntryParam.setMemberId(bseOrderObjectFactory.createSpreadOrderEntryParamMemberId(bseSpreadOrderEntryParam.memberId))
    spreadOrderEntryParam.setClientCode(bseOrderObjectFactory.createSpreadOrderEntryParamClientCode(bseSpreadOrderEntryParam.clientCode))
    spreadOrderEntryParam.setSchemeCode(bseOrderObjectFactory.createSpreadOrderEntryParamSchemeCode(bseSpreadOrderEntryParam.schemeCd))
    spreadOrderEntryParam.setBuySell(bseOrderObjectFactory.createSpreadOrderEntryParamBuySell(bseSpreadOrderEntryParam.buySell.toString))
    spreadOrderEntryParam.setBuySellType(bseOrderObjectFactory.createSpreadOrderEntryParamBuySellType(bseSpreadOrderEntryParam.buySellType.toString))
    spreadOrderEntryParam.setDPTxn(bseOrderObjectFactory.createSpreadOrderEntryParamDPTxn(bseSpreadOrderEntryParam.dpTxn.toString))
    spreadOrderEntryParam.setOrderValue(bseOrderObjectFactory.createSpreadOrderEntryParamOrderValue(bseSpreadOrderEntryParam.orderVal.toString))
    spreadOrderEntryParam.setRedemptionAmt(bseOrderObjectFactory.createSpreadOrderEntryParamRedemptionAmt(bseSpreadOrderEntryParam.redemAmt.toString))
    spreadOrderEntryParam.setAllUnitFlag(bseOrderObjectFactory.createSpreadOrderEntryParamAllUnitFlag(bseSpreadOrderEntryParam.allUnitFlag.toString))
    spreadOrderEntryParam.setRedeemDate(bseOrderObjectFactory.createSpreadOrderEntryParamRedeemDate(bseSpreadOrderEntryParam.redeemDate))
    spreadOrderEntryParam.setFolioNo(bseOrderObjectFactory.createSpreadOrderEntryParamFolioNo(bseSpreadOrderEntryParam.folioNo.getOrElse("")))
    spreadOrderEntryParam.setRemarks(bseOrderObjectFactory.createSpreadOrderEntryParamRemarks(bseSpreadOrderEntryParam.remarks.getOrElse("")))
    spreadOrderEntryParam.setKYCStatus(bseOrderObjectFactory.createSpreadOrderEntryParamKYCStatus(bseSpreadOrderEntryParam.kycStatus.toString))
    spreadOrderEntryParam.setRefNo(bseOrderObjectFactory.createSpreadOrderEntryParamRefNo(bseSpreadOrderEntryParam.refNo.getOrElse("")))
    spreadOrderEntryParam.setSubBroCode(bseOrderObjectFactory.createSpreadOrderEntryParamSubBroCode(bseSpreadOrderEntryParam.subBrCode.getOrElse("")))
    spreadOrderEntryParam.setEUIN(bseOrderObjectFactory.createSpreadOrderEntryParamEUIN(bseSpreadOrderEntryParam.euin.getOrElse("")))
    spreadOrderEntryParam.setEUINVal(bseOrderObjectFactory.createSpreadOrderEntryParamEUINVal(bseSpreadOrderEntryParam.euinVal.toString))
    spreadOrderEntryParam.setMinRedeem(bseOrderObjectFactory.createSpreadOrderEntryParamMinRedeem(bseSpreadOrderEntryParam.minRedeem.toString))
    spreadOrderEntryParam.setDPC(bseOrderObjectFactory.createSpreadOrderEntryParamDPC(bseSpreadOrderEntryParam.dpc.toString))
    spreadOrderEntryParam.setIPAddress(bseOrderObjectFactory.createSpreadOrderEntryParamIPAddress(bseSpreadOrderEntryParam.ipAdd))
    spreadOrderEntryParam.setPassword(bseOrderObjectFactory.createSpreadOrderEntryParamPassword(encryptedPassword))
    spreadOrderEntryParam.setPassKey(bseOrderObjectFactory.createSpreadOrderEntryParamPassKey(bseSpreadOrderEntryParam.passKey))
    spreadOrderEntryParam.setParam1(bseOrderObjectFactory.createSpreadOrderEntryParamParam1(bseSpreadOrderEntryParam.param1.getOrElse("")))
    spreadOrderEntryParam.setParam2(bseOrderObjectFactory.createSpreadOrderEntryParamParam2(bseSpreadOrderEntryParam.param2.getOrElse("")))
    spreadOrderEntryParam.setParam3(bseOrderObjectFactory.createSpreadOrderEntryParamParam3(bseSpreadOrderEntryParam.param3.getOrElse("")))
    
    spreadOrderEntryParam
  }
  
  def populateSwitchOrderEntryParam(bseSwitchOrderEntryParam:BSESwitchOrderEntryParam,encryptedPassword:String):SwitchOrderEntryParam = {
    val switchOrderEntryParam = new SwitchOrderEntryParam
    
    switchOrderEntryParam.setTransCode(bseOrderObjectFactory.createSwitchOrderEntryParamTransCode(bseSwitchOrderEntryParam.transCode.toString))
    switchOrderEntryParam.setTransNo(bseOrderObjectFactory.createSwitchOrderEntryParamTransNo(bseSwitchOrderEntryParam.transNo))
    switchOrderEntryParam.setOrderId(bseOrderObjectFactory.createSwitchOrderEntryParamOrderId(bseSwitchOrderEntryParam.orderId.getOrElse("").toString))
    switchOrderEntryParam.setUserId(bseOrderObjectFactory.createSwitchOrderEntryParamUserId(bseSwitchOrderEntryParam.userId.toString))
    switchOrderEntryParam.setMemberId(bseOrderObjectFactory.createSwitchOrderEntryParamMemberId(bseSwitchOrderEntryParam.memberId))
    switchOrderEntryParam.setClientCode(bseOrderObjectFactory.createSwitchOrderEntryParamClientCode(bseSwitchOrderEntryParam.clientCode))
    switchOrderEntryParam.setFromSchemeCd(bseOrderObjectFactory.createSwitchOrderEntryParamFromSchemeCd(bseSwitchOrderEntryParam.fromSchemeCd))
    switchOrderEntryParam.setToSchemeCd(bseOrderObjectFactory.createSwitchOrderEntryParamToSchemeCd(bseSwitchOrderEntryParam.toSchemeCd))
    switchOrderEntryParam.setBuySell(bseOrderObjectFactory.createSwitchOrderEntryParamBuySell(bseSwitchOrderEntryParam.buySell.toString))
    switchOrderEntryParam.setBuySellType(bseOrderObjectFactory.createSwitchOrderEntryParamBuySellType(bseSwitchOrderEntryParam.buySellType.toString))
    switchOrderEntryParam.setDPTxn(bseOrderObjectFactory.createSwitchOrderEntryParamDPTxn(bseSwitchOrderEntryParam.dpTxn.toString))
    switchOrderEntryParam.setOrderVal(bseOrderObjectFactory.createSwitchOrderEntryParamOrderVal(bseSwitchOrderEntryParam.switchAmt.getOrElse("").toString))
    switchOrderEntryParam.setSwitchUnits(bseOrderObjectFactory.createSwitchOrderEntryParamSwitchUnits(bseSwitchOrderEntryParam.switchUnits.getOrElse("").toString))
    switchOrderEntryParam.setAllUnitsFlag(bseOrderObjectFactory.createSwitchOrderEntryParamAllUnitsFlag(bseSwitchOrderEntryParam.allUnitFlag.toString))
    switchOrderEntryParam.setFolioNo(bseOrderObjectFactory.createSwitchOrderEntryParamFolioNo(bseSwitchOrderEntryParam.folioNo.getOrElse("")))
    switchOrderEntryParam.setRemarks(bseOrderObjectFactory.createSwitchOrderEntryParamRemarks(bseSwitchOrderEntryParam.remarks.getOrElse("")))
    switchOrderEntryParam.setKYCStatus(bseOrderObjectFactory.createSwitchOrderEntryParamKYCStatus(bseSwitchOrderEntryParam.kycStatus.toString))
    switchOrderEntryParam.setSubBrCode(bseOrderObjectFactory.createSwitchOrderEntryParamSubBrCode(bseSwitchOrderEntryParam.subBrCode.getOrElse("")))
    switchOrderEntryParam.setEUIN(bseOrderObjectFactory.createSwitchOrderEntryParamEUIN(bseSwitchOrderEntryParam.euin.getOrElse("")))
    switchOrderEntryParam.setEUINVal(bseOrderObjectFactory.createSwitchOrderEntryParamEUINVal(bseSwitchOrderEntryParam.euinVal.toString))
    switchOrderEntryParam.setMinRedeem(bseOrderObjectFactory.createSwitchOrderEntryParamMinRedeem(bseSwitchOrderEntryParam.minRedeem.toString))
    switchOrderEntryParam.setIPAdd(bseOrderObjectFactory.createSwitchOrderEntryParamIPAdd(bseSwitchOrderEntryParam.ipAdd))
    switchOrderEntryParam.setPassword(bseOrderObjectFactory.createSwitchOrderEntryParamPassword(encryptedPassword))
    switchOrderEntryParam.setPassKey(bseOrderObjectFactory.createSwitchOrderEntryParamPassKey(bseSwitchOrderEntryParam.passKey))
    switchOrderEntryParam.setParma1(bseOrderObjectFactory.createSwitchOrderEntryParamParma1(bseSwitchOrderEntryParam.param1.getOrElse("")))
    switchOrderEntryParam.setParam2(bseOrderObjectFactory.createSwitchOrderEntryParamParam2(bseSwitchOrderEntryParam.param2.getOrElse("")))
    switchOrderEntryParam.setParam3(bseOrderObjectFactory.createSwitchOrderEntryParamParam3(bseSwitchOrderEntryParam.param3.getOrElse("")))
    
    switchOrderEntryParam
  }
}

