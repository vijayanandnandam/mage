package service

import java.util.Date
import javax.inject.Inject

import constants.SchemePlan

import scala.collection.mutable.ListBuffer
import models._
import helpers.DecimalFormat
import models.integration.enumerations.BuySellEnum
import models.integration.enumerations.BuySellEnum.BuySellEnum
import repository.module.{FolioRepository, SchemeRepository}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class TransactionReportService @Inject()(implicit ec: ExecutionContext, reportRepository: FolioRepository, schemeRepository: SchemeRepository) {

  def getTransactionReport(fundDetailsList: ListBuffer[FundDetails], transactionFilter: TransactionFilter, userPk: Long): Future[List[FundTransactionReport]] = {

    var openingUnits = BigDecimal(0.0)
    var openingValue = BigDecimal(0.0)
    var closingValue = BigDecimal(0.0)


    val transactionList = for (fund <- fundDetailsList) yield {
      var openingUnits = BigDecimal(0.0)
      var openingValue = BigDecimal(0.0)
      var closingValue = BigDecimal(0.0)
      val closingUnits = calculateUnitsFromTransactions(fund.transactionList)
      var endDate = transactionFilter.endDate.get
      if (transactionFilter.endDate.get.compareTo(new Date()) > 0) {
        endDate = new Date()
      }
      val transactionFuture = reportRepository.getNetUnitsBeforeDate(fund.fundId, fund.folioId.get, transactionFilter.startDate.get)

      transactionFuture.flatMap(netUnits => {
        openingUnits = netUnits
        for {
          startAmount <- schemeRepository.getSchemePrice(fund.fundId, transactionFilter.startDate.get)
          endAmount <- schemeRepository.getSchemePrice(fund.fundId, endDate)

        } yield (FundTransactionReport(fund.fundBasicDetails.folioNo, fund.fundId, fund.fundBasicDetails.fundName, fund.fundBasicDetails.fundType.getOrElse(""), fund.fundBasicDetails.modeOfHolding, openingUnits,
          (if (startAmount.isEmpty) BigDecimal(0.0) else startAmount.head) * openingUnits, openingUnits + closingUnits, (if (endAmount.isEmpty) BigDecimal(0.0) else endAmount.head) * closingUnits, fund.transactionList))
      })
    }

    Future.sequence(transactionList.toList);
  }

  def getTransactionTypes(): ListBuffer[TransactionType] = {

    val transactionTypeList: ListBuffer[TransactionType] = ListBuffer[TransactionType]()

    for (value <- SchemePlan.TRANSACTION_TYPE_MAP) {
      transactionTypeList.+=(TransactionType(value._1, value._2))
    }
    transactionTypeList
  }

  def calculateNetUnits(unitsList: ListBuffer[(BuySellEnum, BigDecimal)]): BigDecimal = {

    var totalUnits = BigDecimal(0.0)

    unitsList.foreach {
      value =>
        if (value._1 == BuySellEnum.P) {
          totalUnits += value._2
        } else {
          totalUnits -= value._2
        }
    }
    totalUnits
  }

  def calculateUnitsFromTransactions(transactionList: ListBuffer[FundTransaction]): BigDecimal = {

    var totalUnits = BigDecimal(0.0)
    transactionList.foreach {
      value =>
        if (value.transType == BuySellEnum.P) {
          totalUnits += value.units
        } else {
          totalUnits -= value.units
        }
    }
    totalUnits
  }
}