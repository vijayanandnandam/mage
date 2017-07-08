package models.enumerations

import play.api.libs.json.Writes
import play.api.libs.json.Reads
import utils.EnumerationUtils

object ProductEnum extends Enumeration {

  type ProductEnum = Value

  val SIP,LUMPSUM,SMART_SIP , SAVINGS_PLUS, TAX_SAVER = Value

  implicit val enumReads: Reads[ProductEnum] = EnumerationUtils.enumReads(ProductEnum)

  implicit def enumWrites: Writes[ProductEnum] = EnumerationUtils.enumWrites
}