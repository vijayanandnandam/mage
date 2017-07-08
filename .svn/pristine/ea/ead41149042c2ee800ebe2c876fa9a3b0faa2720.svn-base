package models

import java.util.Date

case class Holding(id:Long, name:String, folioNo:Option[String],costValue:Option[BigDecimal],units:Option[BigDecimal],currNav:Option[BigDecimal],
                    navDate:Option[Date],currValue:Option[BigDecimal],
                    realizedGain:Option[BigDecimal],unrealizedGain:Option[BigDecimal],absoluteReturn:Option[BigDecimal])

case class RedeemSuggestion(fundName: String, fundPlan: String, fundOption: String, folioNo: String, holdingMode: String, redeemableUnits: Double,
                            totalUnits: Double, currValue: Double, navDate: Date, currNav: Double, exitLoad: String)
              
              
object HoldingJsonFormats{
  import play.api.libs.json.Json
  
  implicit val holdingFormat = Json.format[Holding]
  implicit val redeemSuggestionFormat = Json.format[RedeemSuggestion]
}