package models.integration

import scala.collection.mutable.ListBuffer

import models.ErrorModel
import models.integration.enumerations.AllRedeemEnum.AllRedeemEnum
import models.integration.enumerations.AllUnitEnum.AllUnitEnum
import models.integration.enumerations.BuySellEnum.BuySellEnum
import models.integration.enumerations.BuySellTypeEnum.BuySellTypeEnum
import models.integration.enumerations.DPCEnum.DPCEnum
import models.integration.enumerations.DPTxnEnum.DPTxnEnum
import models.integration.enumerations.EUINDeclarationEnum.EUINDeclarationEnum
import models.integration.enumerations.FirstOrderEnum.FirstOrderEnum
import models.integration.enumerations.FrequencyTypeEnum.FrequencyTypeEnum
import models.integration.enumerations.KYCStatusEnum.KYCStatusEnum
import models.integration.enumerations.MinRedeemEnum.MinRedeemEnum
import models.integration.enumerations.TransactionCodeEnum.TransactionCodeEnum
import models.integration.enumerations.TransactionModeEnum.TransactionModeEnum
import play.api.libs.json.Json

case class BSEGetPassword(userId: String, password: String, passKey: String)

case class BSEGetPasswordResponse(responseCode: String, encryptedPassword: String)

case class BSEPasswordValidateWrapper(getBSEPasswordResponse: BSEGetPasswordResponse,
                                      errorList: Option[ListBuffer[ErrorModel]])

case class BSEOrderEntryParam(transCode: TransactionCodeEnum, transNo: String, orderId: Option[Long], userId: Long, memberId: String,
                              clientCode: String, schemeCd: String, buySell: String, buySellType: String, dpTxn: DPTxnEnum,
                              orderVal: Option[BigDecimal], qty: Option[BigDecimal], allRedeem: AllRedeemEnum, folioNo: Option[String],
                              remarks: Option[String], kycStatus: String, refNo: Option[String], subBrCode: Option[String],
                              euin: Option[String], euinVal: EUINDeclarationEnum, minRedeem: MinRedeemEnum, dpc: DPCEnum, ipAdd: String,
                              password: String, passKey: String, param1: Option[String], param2: Option[String],
                              param3: Option[String])

case class BSEOrderEntryParamResponse(transCode: String, transNo: String, orderNumber: Option[Long],
                                      userId: String, memberId: String, clientCode: String, bseRemarks: Option[String],
                                      successFlag: String)

case class BSEOrderValidateWrapper(bseOrderEntryParamResponse: BSEOrderEntryParamResponse, errorList: Option[ListBuffer[ErrorModel]])

case class BSESipOrderEntryParam(transCode: TransactionCodeEnum, uniqueRefNo: String, schemeCd: String, memberCode: String,
                                 clientCode: String, userId: Long, internalRefNo: Option[String], transMode: TransactionModeEnum,
                                 dpTxnMode: DPTxnEnum, startDate: String, frequencyType: FrequencyTypeEnum, frequencyAllowed: Int,
                                 installmentAmount: Int, noOfInstallments: Int, remarks: Option[String],
                                 folioNo: Option[String], firstOrderFlag: FirstOrderEnum, subBrCode: Option[String], euin: Option[String],
                                 euinFlag: EUINDeclarationEnum, dpc: DPCEnum, regId: Option[Long], ipAdd: String, password: String,
                                 passKey: String, param1: Option[String], param2: Option[String], param3: Option[String])

case class BSESipOrderEntryParamResponse(transCode: String, transactionNo: String, memberId: String, clientCode: String,
                                         userId: String, sipRegId: String, bseRemarks: Option[String], successFlag: String)

case class BSESipOrderValidateWrapper(bseSipOrderEntryParamResponse: BSESipOrderEntryParamResponse, errorList: Option[ListBuffer[ErrorModel]])

case class BSEXsipOrderEntryParam(transCode: TransactionCodeEnum, uniqueRefNo: String, schemeCd: String, memberCode: String,
                                  clientCode: String, userId: Long, internalRefNo: Option[String], transMode: TransactionModeEnum,
                                  dpTxnMode: DPTxnEnum, startDate: String, frequencyType: String, frequencyAllowed: Int,
                                  installmentAmount: Double, noOfInstallments: Int, remarks: Option[String],
                                  folioNo: Option[String], firstOrderFlag: FirstOrderEnum, brokerage: Option[BigDecimal],
                                  mandateId: Option[Long], subBrCode: Option[String], euin: Option[String], euinFlag: EUINDeclarationEnum, dpc: DPCEnum,
                                  xsipRegId: Option[Long], ipAdd: String, password: String,
                                  passKey: String, param1: Option[String], param2: Option[String], param3: Option[String])

case class BSEXsipOrderEntryParamResponse(transCode: String, transactionNo: String, memberId: String, clientCode: String,
                                          userId: String, xsipRegId: String, bseRemarks: Option[String],
                                          successFlag: String)

case class BSEXsipOrderValidateWrapper(bseXsipOrderEntryParamResponse: BSEXsipOrderEntryParamResponse, errorList: Option[ListBuffer[ErrorModel]])

case class BSESpreadOrderEntryParam(transCode: TransactionCodeEnum, transNo: String, orderId: Option[Long], userId: Long, memberId: String,
                                    clientCode: String, schemeCd: String, buySell: BuySellEnum, buySellType: BuySellTypeEnum, dpTxn: DPTxnEnum,
                                    orderVal: BigDecimal, redemAmt: BigDecimal, allUnitFlag: AllUnitEnum, redeemDate: String,
                                    folioNo: Option[String], remarks: Option[String], kycStatus: String, refNo: Option[String],
                                    subBrCode: Option[String], euin: Option[String], euinVal: EUINDeclarationEnum, minRedeem: MinRedeemEnum, dpc: DPCEnum,
                                    ipAdd: String, password: String, passKey: String, param1: Option[String], param2: Option[String],
                                    param3: Option[String])

case class BSESpreadOrderEntryParamResponse(transCode: String, transNo: String, orderId: String, userId: String,
                                            memberId: String, clientCode: String, bseRemarks: Option[String], successFlag: String)

case class BSESpreadOrderValidateWrapper(bseSpreadOrderEntryParamResponse: BSESpreadOrderEntryParamResponse, errorList: Option[ListBuffer[ErrorModel]])

case class BSESwitchOrderEntryParam(transCode: TransactionCodeEnum, transNo: String, orderId: Option[Long], userId: Long, memberId: String,
                                    clientCode: String, fromSchemeCd: String, toSchemeCd: String, buySell: BuySellEnum,
                                    buySellType: BuySellTypeEnum, dpTxn: DPTxnEnum, switchAmt: Option[BigDecimal], switchUnits: Option[BigDecimal],
                                    allUnitFlag: AllUnitEnum, folioNo: Option[String], remarks: Option[String], kycStatus: String,
                                    subBrCode: Option[String], euin: Option[String], euinVal: EUINDeclarationEnum, minRedeem: MinRedeemEnum,
                                    ipAdd: String, password: String, passKey: String, param1: Option[String], param2: Option[String],
                                    param3: Option[String])

case class BSESwitchOrderEntryParamResponse(transCode: String, transNo: String, orderId: String, userId: String, memberId: String,
                                            clientCode: String, bseRemarks: Option[String], successFlag: String)

case class BSESwitchOrderValidateWrapper(bseSwitchOrderEntryParamResponse: BSESwitchOrderEntryParamResponse, errorList: Option[ListBuffer[ErrorModel]])

case class BSEUploadGetPassword(userId: Long, memberId: String, password: String, passKey: String)

case class BSEUploadMfApi(flag: String, userId: Long, password: String, param: String)

case class BSEUploadMfApiResponse(status: String, response: String, referenceNumber: Option[String])

case class BSEUploadMfApiResponseValidateWrapper(bseUploadMfApiResponse: BSEUploadMfApiResponse, errorList: Option[ListBuffer[ErrorModel]])

case class BSESTPEntryModel(clientCode: String, fromSchemeCode: String, toSchemeCode: String, buySellType: BuySellTypeEnum,
                            transactionMode: TransactionModeEnum, folioNo: Option[String], internalRefNo: Option[String],
                            startDate: String, frequencyType: FrequencyTypeEnum, numberOfTransfers: Long, installmentAmt: Double,
                            firstOrderFlag: FirstOrderEnum, subBrCode: Option[String], euinDeclaration: EUINDeclarationEnum,
                            euin: Option[String], remarks: Option[String], subBrARN: Option[String])

case class BSESWPEntryModel(clientCode: String, schemeCode: String, transactionMode: TransactionModeEnum, folioNo: Option[String],
                            internalRefNo: Option[String], startDate: String, numberOfWithdrawls: Long, frequencyType: FrequencyTypeEnum,
                            installmentAmt: Double, installmentUnits: Option[Double], firstOrderFlag: FirstOrderEnum, subBrCode: Option[String],
                            euinDeclaration: EUINDeclarationEnum, euin: Option[String], remarks: Option[String], subBrARN: Option[String])

case class BSEClientDetailsModel(clientCode: String, clientType: String, taxStatus: String, occupationCode: String, applicantName1: String,
                                 applicantName2: Option[String], applicantName3: Option[String], dateOfBirth: String, gender: String,
                                 physicalDematType: String, accountDetails: ClientAccountDetails, addressDetails: ClientAddressDetails,
                                 guardianFather: Option[String], pan: Option[String], nominee: Option[String] = None,
                                 nomineeRelation: Option[String] = None, guardianPan: Option[String] = None, defaultDp: Option[String] = None, cdslPid: Option[String] = None,
                                 cdslCltId: Option[String] = None, ndslPid: Option[String] = None, ndslCltId: Option[String] = None)

case class ClientAccountDetails(accType1: String, accNo1: String, micrNo1: String, neftOrIfscCode1: String, defaultBankFlag1: String,
                                accType2: Option[String] = None, accNo2: Option[String] = None, micrNo2: Option[String] = None,
                                neftOrIfscCode2: Option[String] = None, defaultBankFlag2: Option[String] = None,
                                accType3: Option[String] = None, accNo3: Option[String] = None, micrNo3: Option[String] = None,
                                neftOrIfscCode3: Option[String] = None, defaultBankFlag3: Option[String] = None,
                                accType4: Option[String] = None, accNo4: Option[String] = None, micrNo4: Option[String] = None,
                                neftOrIfscCode4: Option[String] = None, defaultBankFlag4: Option[String] = None,
                                accType5: Option[String] = None, accNo5: Option[String] = None, micrNo5: Option[String] = None,
                                neftOrIfscCode5: Option[String] = None, defaultBankFlag5: Option[String] = None, chequeName: Option[String] = None)

case class ClientAddressDetails(add1: String, add2: Option[String], add3: Option[String], city: String, state: String, pincode: String, country: String,
                                mobile: String, resiphone: Option[String], resiFax: Option[String], officePhone: Option[String], officeFax: Option[String], email: String,
                                commode: String, divPayMode: String,foreignAddressDetails: ClientForeignAddDetails, pan2: Option[String]=None,
                                pan3: Option[String]=None, mapinNo: Option[String]=None)

case class ClientForeignAddDetails(add1: Option[String] = None, add2: Option[String] = None, add3: Option[String] = None, city: Option[String] = None,
                                   pincode: Option[String] = None, state: Option[String] = None, country: Option[String] = None,
                                   resiphone: Option[String] = None, resiFax: Option[String] = None, officePhone: Option[String] = None, officeFax: Option[String] = None)


case class ClientFatcaUpload(pan: String, name: String, taxStatus: String, dataSrc: String, addressType: String, birthPlace: String, birthCountry: String, residenceCountry1: String,
                             pin1: String, idType1: String, clientIncomeDetails: ClientIncomeDetails, dateOfBirth: Option[String] = None, fatherName: Option[String] = None,
                             spouseName: Option[String] = None, residenceCountry2: Option[String] = None, pin2: Option[String] = None, idType2: Option[String] = None,
                             residenceCountry3: Option[String] = None, pin3: Option[String] = None, idType3: Option[String] = None, residenceCountry4: Option[String] = None,
                             pin4: Option[String] = None, idType4: Option[String] = None, panExemptKYCRefNo: Option[String] = None)

case class ClientIncomeDetails(wealthSource: String, exchangeName: String, incomeSlab: String, politicallyExposedPerson: String, occupationCode: String, occupationType: String,
                               clientUBODetails: ClientUBODetails, corporateServices: Option[String] = None, netWorth: Option[String] = None,
                               netWorthDate: Option[String] = None, exemptionCode: Option[String] = None, ffiDrnfe: Option[String] = None,
                               giinNumber: Option[String] = None, sponsoringEntity: Option[String] = None, giinNa: Option[String] = None, giinExemptionCode: Option[String] = None,
                               nonFinEntityCategory: Option[String] = None, nonFinEntitySubCategory: Option[String] = None, businessNature: Option[String] = None,
                               relatedToListCompany: Option[String] = None)

case class ClientUBODetails(uboApplicable: String, uboCode: String, uboDeclarationFlag: String, newOrUpdate: String,logName: String, uboCount: Option[String] = None, uboName: Option[String] = None, uboPan: Option[String] = None,
                            uboNation: Option[String] = None, uboAdd1: Option[String] = None, uboAdd2: Option[String] = None, uboAdd3: Option[String] = None, uboCity: Option[String] = None,
                            uboPin: Option[String] = None, uboState: Option[String] = None, uboCountry: Option[String] = None, uboAddressType: Option[String] = None,
                            uboTaxResidenCountry: Option[String] = None, uboTIN: Option[String] = None, uboIdentificationDocType: Option[String] = None, uboBirthCountry: Option[String] = None,
                            uboDateOfBirth: Option[String] = None, uboGender: Option[String] = None, uboFatherName: Option[String] = None,
                            uboOccupation: Option[String] = None, uboOccupationType: Option[String] = None, uboTelNo: Option[String] = None, uboMobileNo: Option[String] = None,
                            uboHolPc: Option[String] = None, selfDeclaration: Option[String] = None, reportingPersonAdhaar: Option[String] = None,
                              filler1: Option[String] = None, filler2: Option[String] = None)

object BSEJsonFormats {

  import models.ErrorModelFormat.errorModelFormat

  implicit val getBSEPasswordFormat = Json.format[BSEGetPassword]
  implicit val getBSEPasswordResponseFormat = Json.format[BSEGetPasswordResponse]
  implicit val bsePasswordValidateWrapperFormat = Json.format[BSEPasswordValidateWrapper]
  implicit val bseOrderEntryParamResponseFormat = Json.format[BSEOrderEntryParamResponse]
  implicit val bseOrderValidateWrapperFormat = Json.format[BSEOrderValidateWrapper]
  implicit val bseSipOrderEntryParamResponseFormat = Json.format[BSESipOrderEntryParamResponse]
  implicit val bseSipOrderValidateWrapperFormat = Json.format[BSESipOrderValidateWrapper]
  implicit val bseXsipOrderEntryParamResponseFormat = Json.format[BSEXsipOrderEntryParamResponse]
  implicit val bseXsipOrderValidateWrapperFormat = Json.format[BSEXsipOrderValidateWrapper]
  implicit val bseSpreadOrderEntryParamResponseFormat = Json.format[BSESpreadOrderEntryParamResponse]
  implicit val bseSpreadOrderValidateWrapperFormat = Json.format[BSESpreadOrderValidateWrapper]
  implicit val bseSwitchOrderEntryParamResponseFormat = Json.format[BSESwitchOrderEntryParamResponse]
  implicit val bseSwitchOrderValidateWrapperFormat = Json.format[BSESwitchOrderValidateWrapper]
  implicit val bseMfApiResponseFormat = Json.format[BSEUploadMfApiResponse]
  implicit val bseMfApiResponseValidatorFormat = Json.format[BSEUploadMfApiResponseValidateWrapper]
}