package utils

import constants.{DBConstants, InvestmentConstants}

import scala.collection.mutable

object DBConstantsUtil extends DBConstants with InvestmentConstants {

  val MANDATE_TYPE_MAP = mutable.HashMap[String,String]()
  MANDATE_TYPE_MAP.+=(ISIP_MANDATE -> ISIP_MANDATE_VALUE)
  MANDATE_TYPE_MAP.+=(PHYSICAL_MANDATE -> PHYSICAL_MANDATE_VALUE)


  def getInvestmentModeFullForm(value: String): String = {
    (value) match {
      case "S" => return SIP;
      case "L" => return LUMPSUM;
    }
  }

  def getExitLoadUnit(value: String): String = {
    value match {
      case "P" => "%"
      case "M" => "INR"
      case default => ""
    }
  }

  def getExitLoadBreakPointUnit(value: String): String = {
    value match {
      case "D" => "Days"
      case "W" => "Weeks"
      case "M" => "Months"
      case "Y" => "Years"
      case default => ""
    }
  }
}