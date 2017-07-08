package models.enumerations

import play.api.libs.json.{Reads, Writes}
import utils.EnumerationUtils

/**
  * Created by Fincash on 27-04-2017.
  */
object RedemptionModeEnum extends Enumeration{
  type RedemptionModeEnum = Value

  val SWP = Value(0, "SWP")
  val LUMPSUM = Value(1, "LUMPSUM")

  implicit def enumReads: Reads[RedemptionModeEnum] = EnumerationUtils.enumReads(RedemptionModeEnum)

  implicit def enumWrites: Writes[RedemptionModeEnum] = EnumerationUtils.enumWrites
}
