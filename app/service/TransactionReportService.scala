package service

import java.util.Date
import javax.inject.{Inject, Singleton}

import constants.{DateConstants, SchemePlan}
import models.integration.enumerations.BuySellEnum
import models.integration.enumerations.BuySellEnum.BuySellEnum
import models.{FolioUnits, _}
import org.slf4j.LoggerFactory
import repository.module.{FolioRepository, SchemeRepository}
import utils.DateTimeUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactionReportService @Inject()(implicit ec: ExecutionContext, reportRepository: FolioRepository, schemeRepository: SchemeRepository, reportService: ReportService) extends DateConstants{


  val logger, log = LoggerFactory.getLogger(classOf[TransactionReportService])


  def getTransactionReport(fundDetailsList: ListBuffer[FundDetails], transactionFilter: TransactionFilter, userPk: Long): Future[List[FundTransactionReport]] = {

    var openingUnits = BigDecimal(0.0)
    var openingValue = BigDecimal(0.0)
    var closingValue = BigDecimal(0.0)

    val mergedFolioFundsList = reportService.mergeFolioTransaction(fundDetailsList)
    val transactionList = for (fund <- mergedFolioFundsList) yield {
      var openingUnits = BigDecimal(0.0)
      var openingValue = BigDecimal(0.0)
      var closingValue = BigDecimal(0.0)
      val folioUnitsList:ListBuffer[FolioUnits] = getFolioUnitsList(fund.transactionList)

      val closingUnits = calculateUnitsFromTransactions(fund.transactionList)
      var endDate = DateTimeUtils.convertDateToFormat(transactionFilter.endDate.get,ORDER_CUT_OFF_DATE_FORMAT).get
      val startDate = DateTimeUtils.convertDateToFormat(transactionFilter.startDate.get,ORDER_CUT_OFF_DATE_FORMAT).get
      if (endDate.compareTo(new Date()) > 0) {
        endDate = DateTimeUtils.setTimeToEOD(new Date())
      }
      val transactionFuture = reportRepository.getNetUnitsBeforeDate(fund.fundId, userPk, startDate)

      transactionFuture.flatMap(netUnits => {
        openingUnits = netUnits
        for {
          startAmount <- schemeRepository.getSchemePrice(fund.fundId, startDate)
          endAmount <- schemeRepository.getSchemePrice(fund.fundId, endDate)

        } yield {
          val fundDetails = fund.fundBasicDetails
          val openingValue = (if (startAmount.isEmpty) BigDecimal(0.0) else startAmount.head) * openingUnits
          FundTransactionReport(fundDetails.folioNo, fund.fundId, fundDetails.fundName, fundDetails.plan, fundDetails.divFreq, fundDetails.divOption, fundDetails.fundType.getOrElse(""), fundDetails.modeOfHolding, openingUnits,
            openingValue, openingUnits + closingUnits, openingValue + (if (endAmount.isEmpty) BigDecimal(0.0) else endAmount.head) * closingUnits, fund.transactionList,
            folioUnits = Some(folioUnitsList))
        }
      })
    }

    Future.sequence(transactionList.toList);
  }

  def calculateUnitsFromTransactions(transactionList: ListBuffer[FundTransaction]): BigDecimal = {

    var totalUnits = BigDecimal(0.0)
    transactionList.foreach {
      value =>
        if (value.transType == BuySellEnum.C) {
          totalUnits += value.units
        } else {
          totalUnits -= value.units
        }
    }
    totalUnits
  }

  def getFolioUnitsList(fundTransactions: ListBuffer[FundTransaction]):ListBuffer[FolioUnits] = {

    val folioVsUnitsMap:mutable.LinkedHashMap[Long,(BigDecimal,BigDecimal)] = mutable.LinkedHashMap[Long,(BigDecimal,BigDecimal)]()

    for(transaction <- fundTransactions){
      if(!folioVsUnitsMap.contains(transaction.folioId.get)){
        folioVsUnitsMap.+=(transaction.folioId.get -> (BigDecimal(0.0),BigDecimal(0.0)))
      }
      val unitsAmtTuple = folioVsUnitsMap.getOrElse(transaction.folioId.get,(BigDecimal(0.0),BigDecimal(0.0)))
      var folioUnitsSum = unitsAmtTuple._1
      var folioAmtSum = unitsAmtTuple._2
      if(transaction.transType == BuySellEnum.C){
        folioUnitsSum += transaction.units
        folioAmtSum += transaction.amount.getOrElse(BigDecimal(0.0))
      } else{
        folioUnitsSum -= transaction.units
        folioAmtSum -= transaction.amount.getOrElse(BigDecimal(0.0))
      }
      folioVsUnitsMap.+=(transaction.folioId.get ->(folioUnitsSum,folioAmtSum))
    }
    (for((key,value) <- folioVsUnitsMap) yield{
      FolioUnits(key,value._1,value._2)
    })(scala.collection.breakOut)
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
        if (value._1 == BuySellEnum.C) {
          totalUnits += value._2
        } else {
          totalUnits -= value._2
        }
    }
    totalUnits
  }
}