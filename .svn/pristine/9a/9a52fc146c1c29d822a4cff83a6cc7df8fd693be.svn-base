package constants

import scala.collection.mutable

/**
  * Created by fincash on 10-02-2017.
  */
trait InvestmentConstants {

  //Investment Mode
  final val SIP = "SIP"
  final val LUMPSUM = "LUMPSUM"
}

object InvestmentConstants extends InvestmentConstants with DBConstants {

  val INVESTMENT_MODE_MAP = mutable.HashMap[String, String]()
  INVESTMENT_MODE_MAP.+=(SIP_INVESTMENT_MODE -> SIP)
  INVESTMENT_MODE_MAP.+=(LUMPSUM_INVESTMENT_MODE -> LUMPSUM)

}
