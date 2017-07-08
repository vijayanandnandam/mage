package service

import scala.collection.mutable.ListBuffer
import models.FundTransaction
import models.FundDetails
import models.AssetClassCostValueModel

import scala.collection.mutable.HashMap
import models.enumerations.AssetClassEnum.AssetClassEnum
import models.enumerations.AssetClassEnum
import models.AssetClassCurrValueModel
import models.integration.enumerations.BuySellEnum
import utils.DateTimeUtils

class PortfolioSummaryService {

  def calculateCurrentValue(fundReportList: ListBuffer[FundDetails]): AssetClassCurrValueModel = {

    var currentValue = BigDecimal(0.0)
    val assetClassCurrentValueMap = HashMap[AssetClassEnum, BigDecimal]()
    var fundIndex = 0
    var fundAsOfDate = DateTimeUtils.getCurrentDate
    for (fund <- fundReportList) {

      fundAsOfDate = fund.navDate.get
      var numberOfUnitsLeft = BigDecimal(0.0)
      var fundCurrentValue = BigDecimal(0.0)
      for (transaction <- fund.transactionList) {
        if (transaction.transType == BuySellEnum.R) {
          numberOfUnitsLeft -= transaction.units
        } else {
          numberOfUnitsLeft += transaction.units
        }
      }
      fundCurrentValue = numberOfUnitsLeft * fund.currNav
      currentValue += fundCurrentValue

      var assetClassCurrentValue: BigDecimal = assetClassCurrentValueMap.getOrElseUpdate(fund.assetClass, BigDecimal(0.0))
      assetClassCurrentValue += fundCurrentValue
      assetClassCurrentValueMap.+=(fund.assetClass -> assetClassCurrentValue)
      fundReportList(fundIndex) = fund.copy(currentValue = Some(fundCurrentValue))
      fundIndex += 1
    }

    assetClassCurrentValueMap.+=(AssetClassEnum.TOTAL -> currentValue)
    AssetClassCurrValueModel(assetClassCurrentValueMap, currentValue, fundAsOfDate)
  }

  def calculateCostValue(fundReportList: ListBuffer[FundDetails]): AssetClassCostValueModel = {
    var costValue = BigDecimal(0.0)
    val assetClassCostValueMap = HashMap[AssetClassEnum, BigDecimal]()
    var fundIndex = 0
    for (fund <- fundReportList) {
      var fundCostValue = BigDecimal(0.0)
      val purchaseTransactions = ListBuffer[FundTransaction]()
      val redeemTransactions = ListBuffer[FundTransaction]()
      for (transaction <- fund.transactionList) {
        if (transaction.transType == BuySellEnum.R) {
          redeemTransactions.+=(transaction)
        } else {
          purchaseTransactions.+=(transaction)
        }
      }

      var purchaseIndex = 0
      for (transaction <- redeemTransactions) {

        var redeemUnits = transaction.units
        while (redeemUnits != 0) {
          val purchaseUnits = purchaseTransactions(purchaseIndex).units
          if (redeemUnits <= purchaseUnits) {
            purchaseTransactions(purchaseIndex) = purchaseTransactions(purchaseIndex).copy(units = purchaseUnits - redeemUnits)
            redeemUnits = 0
          } else {
            purchaseTransactions(purchaseIndex) = purchaseTransactions(purchaseIndex).copy(units = 0)
            redeemUnits -= purchaseUnits
            purchaseIndex += 1
          }
        }
      }

      for (transaction <- purchaseTransactions) {
        fundCostValue += (transaction.price * transaction.units)
      }
      costValue += fundCostValue
      var assetClassCost: BigDecimal = assetClassCostValueMap.getOrElseUpdate(fund.assetClass, BigDecimal(0.0))
      assetClassCost += fundCostValue
      assetClassCostValueMap.+=(fund.assetClass -> assetClassCost)
      fundReportList(fundIndex) = fund.copy(costValue = Some(fundCostValue))
      fundIndex += 1
    }
    assetClassCostValueMap.+=(AssetClassEnum.TOTAL -> costValue)
    AssetClassCostValueModel(assetClassCostValueMap, costValue)
  }

}