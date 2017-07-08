package models

import models.integration.enumerations.TransactionCodeEnum.TransactionCodeEnum
import models.integration.enumerations.FrequencyTypeEnum.FrequencyTypeEnum
import models.integration.enumerations.AllUnitEnum.AllUnitEnum
import models.integration.enumerations.MandateTypeEnum.MandateTypeEnum
import models.integration.enumerations.BuySellTypeEnum.BuySellTypeEnum
import models.integration.enumerations.BuySellEnum.BuySellEnum
import models.integration.enumerations.DPTxnEnum.DPTxnEnum
import models.integration.enumerations.FirstOrderEnum.FirstOrderEnum
import models.integration.enumerations.MandateTypeEnum
import models.integration.enumerations.TransactionModeEnum.TransactionModeEnum


case class FCOrderEntryModel(uniqueRefNo: String, transCode: TransactionCodeEnum, orderId: Option[Long], schemeCode: String, clientCode: String,
                             buySell: String, buySellType: String, dpTransaction: DPTxnEnum, internalRefNo: Option[String],
                             amount: Option[BigDecimal], qty: Option[BigDecimal], ipAdd: Option[String], folioNo:Option[String])

case class FCSipOrderEntryModel(uniqueRefNo: String, transCode: TransactionCodeEnum, schemeCode: String, clientCode: String, startDate: String,
                                frequencyType: FrequencyTypeEnum, transactionMode: TransactionModeEnum, dpTxnMode: DPTxnEnum,
                                internalRefNo: Option[String],frequencyAllowed: Int, installmentAmount: Int, noOfInstallments: Int,
                                firstOrderFlag: FirstOrderEnum, sipRegId: Option[Long], ipAdd: Option[String])

case class FCXsipOrderEntryModel(uniqueRefNo: String, transCode: TransactionCodeEnum, schemeCode: String, clientCode: String, startDate: String,
                                 frequencyType: String, transactionMode: TransactionModeEnum, dpTxnMode: DPTxnEnum, internalRefNo: Option[String],
                                 frequencyAllowed: Int, installmentAmount: Double, noOfInstallments: Int, mandateId: Option[Long],
                                 isipMandateId: Option[String], firstOrderFlag: FirstOrderEnum, xsipRegId: Option[Long],
                                 ipAdd: Option[String], folioNo:Option[String])

case class FCSpreadOrderEntryModel(uniqueRefNo: String, transCode: TransactionCodeEnum, orderId: Option[Long], schemeCode: String,
                                   clientCode: String, buySell: BuySellEnum, buySellType: BuySellTypeEnum, dpTxnMode: DPTxnEnum,
                                   internalRefNo: Option[String],purchaseAmt: BigDecimal, redeemAmt: BigDecimal, allUnitFlag: AllUnitEnum,
                                   redeemDate: String, ipAdd: Option[String])

case class FCSwitchOrderEntryModel(uniqueRefNo: String, transCode: TransactionCodeEnum, orderId: Option[Long], clientCode: String,
                                   fromSchemeCode: String, toSchemeCode: String, buySell: BuySellEnum, dpTxnMode: DPTxnEnum,
                                   buySellType: BuySellTypeEnum, switchAmt: Option[BigDecimal],
                                   switchUnits: Option[BigDecimal], allUnits: AllUnitEnum, ipAdd: Option[String])

case class XsipMandateRegisterModel(clientCode: String, amount: Double, ifscCode: String, accNo: String,
                                    mandateType: MandateTypeEnum)

case class FCSTPEntryModel(clientCode: String, fromSchemeCode: String, toSchemeCode: String, buySellType: BuySellTypeEnum,
                           startDate: String, frequencyType: FrequencyTypeEnum, numberOfTransfers: Long, installmentAmt: Double,
                           transactionMode: TransactionModeEnum, internalRefNo: Option[String],firstOrderFlag: FirstOrderEnum)

case class FCSWPEntryModel(clientCode: String, schemeCode: String, startDate: String, frequencyType: FrequencyTypeEnum,
                           numberOfWithdrawl: Long, installmentAmt: Double, installmentUnits: Option[Double],
                           transactionMode: TransactionModeEnum, internalRefNo: Option[String],firstOrderFlag: FirstOrderEnum)

case class ClientOrderPaymentStatus(clientCode: String, orderId: String, segment: String)




