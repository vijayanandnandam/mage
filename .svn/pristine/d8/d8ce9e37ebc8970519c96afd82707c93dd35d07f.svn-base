package helpers

import constants.{CapitalGainConstants, CategoryConstants, FundConstants}

import scala.collection.mutable.HashMap

object CapitalGainHelper extends CapitalGainConstants with CategoryConstants{
  
  val fundCategoryGainCriteria = HashMap.empty[Long,Int]
  
  fundCategoryGainCriteria += (EQUITY_ID -> EQUITY_CAPITAL_GAIN_CRITERIA)
  fundCategoryGainCriteria += (DEBT_ID -> DEBT_CAPITAL_GAIN_CRITERIA)
  fundCategoryGainCriteria += (EQUITY_HYBRID -> EQUITY_CAPITAL_GAIN_CRITERIA)
  fundCategoryGainCriteria += (DEBT_HYBRID -> DEBT_CAPITAL_GAIN_CRITERIA)
  //fundCategoryGainCriteria += (GOLD_FUND -> DEBT_CAPITAL_GAIN_CRITERIA)
  //fundCategoryGainCriteria += (LIQUID_ID -> DEBT_CAPITAL_GAIN_CRITERIA)
  
  def getFundCapitalGainCriteria(fundCategoryId:Long):Int = {

    fundCategoryGainCriteria.getOrElse(fundCategoryId, 0)
  }
}