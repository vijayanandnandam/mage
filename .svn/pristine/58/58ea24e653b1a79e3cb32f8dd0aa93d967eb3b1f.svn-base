package models.integration.enumerations

import play.api.libs.json.{Reads, Writes}
import utils.EnumerationUtils

object BuySellEnum extends Enumeration{
  
  type BuySellEnum = Value
  
  val P,R,SO,SI = Value

  implicit def enumReads: Reads[BuySellEnum] = EnumerationUtils.enumReads(BuySellEnum)

  implicit def enumWrites: Writes[BuySellEnum] = EnumerationUtils.enumWrites
}