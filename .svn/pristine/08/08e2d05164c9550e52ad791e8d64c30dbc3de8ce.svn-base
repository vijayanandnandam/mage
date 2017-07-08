package service

import models.FundTransaction

import scala.collection.mutable.ListBuffer
import java.text.SimpleDateFormat

import models.FundDetails
import helpers.DecimalFormat
import models.FundDetails
import models.RedemptionDetails
import utils.DateTimeUtils
import helpers.CapitalGainHelper
import models.integration.enumerations.BuySellEnum

class CapitalGainsReportService {

  def calculateCapitalGains(fundDetailsList: ListBuffer[FundDetails]): ListBuffer[FundDetails] = {

    var fundIndex = 0
    for (currFund <- fundDetailsList) {
      val redemptionDetailsList = ListBuffer[RedemptionDetails]()
      val purchaseTransactions = getPurchaseTransactions(currFund.transactionList)
      val redeemTransactions = getRedeemTransactions(currFund.transactionList)

      var totalShortTermGain: Option[BigDecimal] = None
      var totalLongTermGain: Option[BigDecimal] = None
      val bufferList = ListBuffer[BigDecimal]()
      var totalPurchaseUnits = BigDecimal(0.0)
      for (transaction <- purchaseTransactions) {
        bufferList.+=(transaction.units)
        totalPurchaseUnits += transaction.units
      }

      var lastIndex = 0
      var totalUnitsRedeemed = BigDecimal(0.0)
      var totalAmountRedeemed = BigDecimal(0.0)

      for (transaction <- redeemTransactions) {
        var currRedeemUnits = transaction.units
        totalUnitsRedeemed += transaction.units
        while (currRedeemUnits != 0) {
          val purchaseTrans = purchaseTransactions(lastIndex)
          var individualRedeemAmount = BigDecimal(0.0)
          var redeemUnits = BigDecimal(0.0)
          var individualRedeemGain = BigDecimal(0.0)
          if (currRedeemUnits >= bufferList(lastIndex)) {
            currRedeemUnits -= bufferList(lastIndex)

            individualRedeemAmount = purchaseTrans.units * transaction.price
            individualRedeemGain = individualRedeemAmount - purchaseTrans.price * purchaseTrans.units
            redeemUnits = purchaseTrans.units
            bufferList(lastIndex) = 0
            lastIndex += 1
          } else {
            individualRedeemAmount = currRedeemUnits * transaction.price

            individualRedeemGain = individualRedeemAmount - purchaseTrans.price * currRedeemUnits
            redeemUnits = currRedeemUnits
            bufferList(lastIndex) -= currRedeemUnits
            purchaseTransactions(lastIndex) = purchaseTrans.copy(units = bufferList(lastIndex))
            currRedeemUnits = 0
          }
          totalAmountRedeemed += individualRedeemAmount
          val yearDiff = DateTimeUtils.getDiffYears(purchaseTrans.transDate, transaction.transDate)
          var shortTermGain: Option[BigDecimal] = None
          var longTermGain: Option[BigDecimal] = None

          if (yearDiff <= CapitalGainHelper.getFundCapitalGainCriteria(currFund.assetClassId)) {
            shortTermGain = Some(individualRedeemGain)
            totalShortTermGain = Some((totalShortTermGain.getOrElse(BigDecimal(0.0))))
            totalShortTermGain = Some((individualRedeemGain + totalShortTermGain.getOrElse(BigDecimal(0.0))))
          } else {
            longTermGain = Some(individualRedeemGain)
            totalLongTermGain = Some((totalLongTermGain.getOrElse(BigDecimal(0.0))))
            totalLongTermGain = Some((individualRedeemGain + totalLongTermGain.getOrElse(BigDecimal(0.0))))
          }
          redemptionDetailsList.+=(RedemptionDetails(transaction.transDesc, Some(transaction.transDate), Some(redeemUnits), Some(transaction.price), Some(individualRedeemAmount),
            purchaseTrans.transDate, redeemUnits, purchaseTrans.price, shortTermGain, longTermGain))
        }
      }
      val fund = fundDetailsList(fundIndex)
      val totalUnitsLeft = totalPurchaseUnits - totalUnitsRedeemed
      fundDetailsList(fundIndex) = fund.copy(redemptionDetailsList = Some(redemptionDetailsList), totalRedeemedUnits = Some(totalUnitsRedeemed), totalUnitsLeft = Some(totalUnitsLeft), totalAmount = Some(totalAmountRedeemed), totalShortTermGain = totalShortTermGain, totalLongTermGain = totalLongTermGain)

      fundIndex += 1
    }

    fundDetailsList
  }


  def calculateCumulativeGain(fundsList: ListBuffer[FundDetails]): (BigDecimal, BigDecimal) = {

    var cumulativeShortTermGain = BigDecimal(0.0)
    var cumulativeLongTermGain = BigDecimal(0.0)
    fundsList.foreach(fund => {
      cumulativeShortTermGain += fund.totalShortTermGain.getOrElse(0.0)
      cumulativeLongTermGain += fund.totalLongTermGain.getOrElse(0.0)
    })
    (cumulativeShortTermGain, cumulativeLongTermGain)
  }

  private def getRedeemTransactions(fundTransactionList: ListBuffer[FundTransaction]): ListBuffer[FundTransaction] = {

    val redeemTransactions = ListBuffer[FundTransaction]()

    for (transaction <- fundTransactionList) {

      if (transaction.transType == BuySellEnum.R) {
        redeemTransactions.+=(transaction)
      }
    }

    redeemTransactions
  }

  private def getPurchaseTransactions(fundTransactionList: ListBuffer[FundTransaction]): ListBuffer[FundTransaction] = {

    val purchaseTransactions = ListBuffer[FundTransaction]()

    for (transaction <- fundTransactionList) {

      if (transaction.transType == BuySellEnum.P) {
        purchaseTransactions.+=(transaction)
      }
    }


    purchaseTransactions
  }
}

