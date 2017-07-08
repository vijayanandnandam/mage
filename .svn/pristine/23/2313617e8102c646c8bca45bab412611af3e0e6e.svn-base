package helpers

import java.util.Calendar

import constants.SchemePlan

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 06-02-2017.
  */
class SchemeHelper {

  def getAge(date: java.sql.Date): Double = {
    val cal: Calendar = Calendar.getInstance();
    cal.setTime(date);
    val fundInceptionYear = cal.get(Calendar.YEAR);
    val currentYear = Calendar.getInstance().get(Calendar.YEAR);
    currentYear - fundInceptionYear;
  }

  def getDivOption(divOption: String): String = {
    SchemePlan.DIVIDEND_OPTION_MAP.getOrElse(divOption, "")
  }

  def getDivFrequency(divFrequency: String): String = {
    SchemePlan.DIVIDEND_FREQ_MAP.getOrElse(divFrequency, "")
  }

  def getSipFrequencyFullForm(sipFreqType: String): String = {
    SchemePlan.SIP_FREQUENCY_FULLFORM_MAP.getOrElse(sipFreqType, "")
  }

  def getSipFrequencyShortForm(sipFreqType: String): String = {
    SchemePlan.SIP_FREQUENCY_SHORTFORM_MAP.getOrElse(sipFreqType, "")
  }



  def getSchemePlan(schemePlan: String): String = {
    SchemePlan.SCHEME_PLAN_MAP.getOrElse(schemePlan, "")
  }

  def getSchemeOption(schemePlan: String, divFreq: String): String = {

    val planName = SchemePlan.SCHEME_PLAN_MAP.getOrElse(schemePlan, "")
    val freqName = SchemePlan.DIVIDEND_FREQ_MAP.getOrElse(divFreq, "")
    var schemeOption = ""

    schemeOption = freqName
    if (planName.length != 0) {
      schemeOption = schemeOption + " "
    }
    schemeOption += planName

    schemeOption
  }

  def sortPlanOptions(planOptions: ListBuffer[(String, String, String)]): ListBuffer[(String, String, String)] = {

    val sortedPlanList = ListBuffer[(String, String, String)]()
    val planOptionMap: mutable.LinkedHashMap[(String, String, String), Int] = SchemePlan.getPlanOptionFreqMap()

    for (option <- planOptions) {
      planOptionMap.+=(option -> 1)
    }

    for (key <- planOptionMap) {
      if (key._2 > 0) {
        sortedPlanList.+=(key._1)
      }
    }
    sortedPlanList
  }

}
