package service

import scala.collection.mutable.ListBuffer
import models.Holding
import java.text.SimpleDateFormat
import models.FundDetails
import helpers.DecimalFormat
import utils.NumberUtils

class HoldingsReportService {

  def getHoldings(capitalGainsFunds: ListBuffer[FundDetails], totalCurrentValue: BigDecimal,
                  totalCostValue: BigDecimal): ListBuffer[Holding] = {

    val holdingsList = ListBuffer[Holding]()
    var totalUnitsLeft = BigDecimal(0.0)
    var totalRealizedGain = BigDecimal(0.0)
    for (fund <- capitalGainsFunds) {
      var unrealizedGain: BigDecimal = fund.currentValue.getOrElse(0.0)
      unrealizedGain -= fund.costValue.getOrElse(0.0)

      var realizedGain: BigDecimal = fund.totalLongTermGain.getOrElse(0.0)
      realizedGain += fund.totalShortTermGain.getOrElse(0.0)
      totalRealizedGain += realizedGain

      val absoluteReturn = Some(((realizedGain + unrealizedGain) * 100) / fund.costValue.getOrElse(1.0))
      totalUnitsLeft += fund.totalUnitsLeft.getOrElse(0.0)
      holdingsList.+=(Holding(fund.fundId, fund.fundBasicDetails.fundName, Some(fund.fundBasicDetails.folioNo), fund.costValue, fund.totalUnitsLeft, Some(fund.currNav), fund.navDate, fund.currentValue, Some(realizedGain), Some(unrealizedGain), absoluteReturn))
    }

    totalRealizedGain = DecimalFormat.formatDecimalPlace(totalRealizedGain)

    holdingsList.+=(Holding(0, "Grand Total", None, Some(totalCostValue), Some(totalUnitsLeft), None, None, Some(totalCurrentValue), Some(totalRealizedGain), Some(totalCurrentValue - totalCostValue), None))

    holdingsList
  }
}