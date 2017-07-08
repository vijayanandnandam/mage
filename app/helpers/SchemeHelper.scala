package helpers

import java.util.Calendar
import javax.inject.Singleton

import constants.{ModeOfHoldingConstants, SchemePlan}
import data.model.Tables.{FcbsatRow, FcbseRow, FcrpaRow}
import utils.DateTimeUtils
import utils.DateTimeUtils.BSE_SIP_DATE_FORMAT

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by fincash on 06-02-2017.
  */

@Singleton
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

  def getHoldingMode(mode: String): String = {
    ModeOfHoldingConstants.MODE_OF_HOLDING_MAP.getOrElse(mode, "")
  }

  def getMinLumpSumAmount(rpaObj: FcrpaRow, bseObj: FcbseRow):Option[Double] = {

    if(bseObj.bseminpurchaseamt.isEmpty){
      rpaObj.rpaminpuramount
    } else if(rpaObj.rpaminpuramount.isEmpty){
      bseObj.bseminpurchaseamt
    } else{
      val bseMinLumpSumAmt = bseObj.bseminpurchaseamt.getOrElse(0D)
      val rpaMinLumpSumAmt = rpaObj.rpaminpuramount.getOrElse(0D)
      Some(Math.max(bseMinLumpSumAmt,rpaMinLumpSumAmt))
    }
  }

  def getMinAddLumpSumAmount(rpaObj: FcrpaRow, bseObj: FcbseRow):Option[Double] = {

    if(bseObj.bseaddpuramtmultiple.isEmpty){
      rpaObj.rpaminaddinvestamt
    } else if(rpaObj.rpaminaddinvestamt.isEmpty){
      bseObj.bseaddpuramtmultiple
    } else{
      val bseMinAddLumpSumAmt = bseObj.bseaddpuramtmultiple.getOrElse(0D)
      val rpaMinAddLumpSumAmt = rpaObj.rpaminaddinvestamt.getOrElse(0D)
      Some(Math.max(bseMinAddLumpSumAmt,rpaMinAddLumpSumAmt))
    }
  }

  def getMinLumpSumMultiplier(rpaObj: FcrpaRow, bseObj: FcbseRow):Option[Double] = {

    if(bseObj.bsepuramtmultiplier.isEmpty){
      rpaObj.rpapuramtmultiplier
    } else if(rpaObj.rpapuramtmultiplier.isEmpty){
      bseObj.bsepuramtmultiplier
    } else{
      val bseMinLumpSumMultiplier = bseObj.bsepuramtmultiplier.getOrElse(0D)
      val rpaMinLumpSumMultiplier = rpaObj.rpapuramtmultiplier.getOrElse(0D)
      Some(Math.max(bseMinLumpSumMultiplier,rpaMinLumpSumMultiplier))
    }
  }

  def getAllowedDates(bsatRow:FcbsatRow):String = {

    val daysList:Array[String] = bsatRow.bsataipday.split('|')
    val maximumGap = bsatRow.bsatmaximumgap
    var validDaysString = ""
    for(day <- daysList) {
      val estimatedSipDate = DateTimeUtils.convertDateToFormat(DateTimeUtils.getEstimatedSIPDate(day.toInt),BSE_SIP_DATE_FORMAT).get
      val maxGapDate = DateTimeUtils.getNthDayDate(maximumGap.toInt)

      if(!estimatedSipDate.after(maxGapDate)){

        if(validDaysString.length > 0){
          validDaysString += "|"
        }

        validDaysString += day
      }
    }
    validDaysString
  }
}
