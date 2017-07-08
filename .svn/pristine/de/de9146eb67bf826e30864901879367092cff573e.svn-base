package models

import java.util.Date

case class HoldingFilter(emptyHolding: Boolean)
case class Holding(id: Long, name: String, modeOfHolding: Option[String], plan: Option[String], divFreq: Option[String], divOption: Option[String],
                   folioNo: Option[String], costValue: Option[BigDecimal], units: Option[BigDecimal], currNav: Option[BigDecimal],
                   navDate: Option[Date], currValue: Option[BigDecimal],
                   realizedGain: Option[BigDecimal], unrealizedGain: Option[BigDecimal], absoluteReturn: Option[BigDecimal])

case class RedeemSuggestion(fundName: String, fundPlan: String, fundOption: String, folioNo: String, holdingMode: String, redeemableUnits: BigDecimal,
                            totalUnits: BigDecimal, currValue: BigDecimal, navDate: Date, currNav: BigDecimal, exitLoad: String)

case class HoldingFund(fhtrfnum: Long, soptrfnum: Long, folioNo: String, holdingMode: String, holdingUnits: BigDecimal)


object HoldingJsonFormats {

  import play.api.libs.json.Json
  implicit val holdingFilterFormat = Json.format[HoldingFilter]
  implicit val holdingFormat = Json.format[Holding]
  implicit val redeemSuggestionFormat = Json.format[RedeemSuggestion]
  implicit val holdingFunFormat = Json.format[HoldingFund]
}