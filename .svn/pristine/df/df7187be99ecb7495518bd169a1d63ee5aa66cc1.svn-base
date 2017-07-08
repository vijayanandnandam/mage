package models.enumerations

import play.api.libs.json.Writes
import utils.EnumerationUtils
import play.api.libs.json.Reads

object InvestmentModeEnum extends Enumeration {

  type InvestmentModeEnum = Value

  val SIP = Value(0, "SIP")
  val LUMPSUM = Value(1, "LUMPSUM")

  implicit def enumReads: Reads[InvestmentModeEnum] = EnumerationUtils.enumReads(InvestmentModeEnum)

  implicit def enumWrites: Writes[InvestmentModeEnum] = EnumerationUtils.enumWrites
}
  