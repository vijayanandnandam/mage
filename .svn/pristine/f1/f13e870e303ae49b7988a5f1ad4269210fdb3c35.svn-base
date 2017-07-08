package models.enumerations

import play.api.libs.json.Writes
import utils.EnumerationUtils
import play.api.libs.json.Reads

object AssetClassEnum extends Enumeration{
  
  type AssetClassEnum = Value
  
  val EQUITY_FUNDS = Value(1,"Equity")
  
  val DEBT_FUNDS = Value(2,"Debt")
  
  val LIQUID_FUNDS = Value(3,"Liquid")
  
  val HYBRID_FUNDS = Value(4,"Hybrid")
  
  val GOLD_FUND = Value(5,"Gold")
  
  val ELSS = Value(6,"ELSS")
  
  val TOTAL = Value(7,"Total")

  val HYBRID_EQUITY = Value(8,"Balanced-Equity")

  val HYBRID_DEBT = Value(9,"Balanced-Debt")
  
  implicit def enumReads: Reads[AssetClassEnum] = EnumerationUtils.enumReads(AssetClassEnum)

  implicit def enumWrites: Writes[AssetClassEnum] = EnumerationUtils.enumWrites
}