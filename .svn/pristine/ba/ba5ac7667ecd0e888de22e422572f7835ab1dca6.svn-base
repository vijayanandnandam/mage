package models

import java.util.Date

import scala.collection.mutable.ListBuffer
import models.enumerations.AssetClassEnum
import models.enumerations.AssetClassEnum.AssetClassEnum
import models.integration.enumerations.BuySellEnum.BuySellEnum
import play.api.libs.json.Json

import scala.collection.mutable

case class FundTransaction(transDesc:String, transDate:Date, transType:BuySellEnum, units:BigDecimal, price:BigDecimal, amount:Option[BigDecimal]=None, gain:Option[BigDecimal]=None)
case class DashboardTopFund(fundId:Long,fundName:String,fundReturn:BigDecimal)
case class FundBasicDetails(folioNo:String, fundName:String,modeOfHolding:String, fundType:Option[String]=None)
case class FundDetails(fundId:Long, fundBasicDetails:FundBasicDetails, amc:String,assetClass:AssetClassEnum, assetClassId:Long, currNav:BigDecimal,navDate:Option[Date]=None, transactionList:ListBuffer[FundTransaction],
                       costValue:Option[BigDecimal]=None, currentValue:Option[BigDecimal]=None, realizedGain:Option[BigDecimal]=None,
                       unrealizedGain:Option[BigDecimal]=None,
                       redemptionDetailsList:Option[ListBuffer[RedemptionDetails]]=None, totalRedeemedUnits:Option[BigDecimal]=None,
                       totalUnitsLeft:Option[BigDecimal]=None,
                       totalAmount:Option[BigDecimal]=None, totalShortTermGain:Option[BigDecimal]=None, totalLongTermGain:Option[BigDecimal]=None,pan:Option[String]=None,folioId:Option[Long]=None)
case class DashboardFundDetail(fundId:Long,fundName:String,currentValue:Option[BigDecimal])
case class AssetClassFundDetails(assetClass:AssetClassEnum,fundDetailsList:ListBuffer[DashboardFundDetail])
case class RedemptionDetails(desc:String,redeemDate:Option[Date],units:Option[BigDecimal], price:Option[BigDecimal], amount:Option[BigDecimal],
                             purchaseDate:Date,purchaseUnits:BigDecimal, purchasePrice:BigDecimal,shortTermGain:Option[BigDecimal]=None,
                             longTermGain:Option[BigDecimal]=None)
case class TransactionFilter(startDate:Option[Date],endDate:Option[Date],fundId:Option[Long],transType:Option[String])
case class FundTransactionReport(folioNo:String, fundId:Long, fundName:String, fundType:String, modeOfHolding:String, openingUnits:BigDecimal, openingValue:BigDecimal,
                                 closingUnits:BigDecimal, closingValue:BigDecimal, transactionList:ListBuffer[FundTransaction])
case class TransactionType(typeKey:String,typeValue:String)
case class TransactionReport(mobileNo:String, emailId:String,transactionTypeList:ListBuffer[TransactionType],transactionReportList: List[FundTransactionReport])
case class CapitalGainFundModel(status:String,pan:Option[String],fundDetailsList:ListBuffer[FundDetails],
                                cumulativeShortTermGain:BigDecimal,cumulativeLongTermGain:BigDecimal)
object FundDetailJsonFormats {

  import play.api.libs.json.JsPath
  import play.api.libs.json.Reads
  import play.api.libs.functional.syntax._
  implicit val transactionFilterReads: Reads[TransactionFilter] = (
      (JsPath \ "startDate").readNullable[Date] and
      (JsPath \ "endDate").readNullable[Date] and
      (JsPath \ "fundId").readNullable[Long] and
      (JsPath \ "transType").readNullable[String]
  )(TransactionFilter.apply _)
  implicit val transFormat = Json.format[FundTransaction]
  implicit val dashboardTopFundFormat = Json.format[DashboardTopFund]
  implicit val redemptionDetailsFormat = Json.format[RedemptionDetails]
  implicit val fundBasicDetailsFormat = Json.format[FundBasicDetails]
  implicit val capitalGainFundsFormat = Json.format[FundDetails]
  implicit val fundTransactionReportFormat = Json.format[FundTransactionReport]
  implicit val capitalGainFundModelFormat = Json.format[CapitalGainFundModel]
  implicit val transactionTypeFormat = Json.format[TransactionType]
  implicit val transactionReportFormat = Json.format[TransactionReport]
  implicit val dashboardFundDetailsFormat = Json.format[DashboardFundDetail]
  implicit val assetClassFundDetailsFormat = Json.format[AssetClassFundDetails]
}