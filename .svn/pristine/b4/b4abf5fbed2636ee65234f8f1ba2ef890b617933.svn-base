package service

import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

import constants.OrderConstants
import helpers.{OrderHelper, SchemeHelper}
import models.{FundDetails, Transaction, TransactionFilter, TransactionSummary}
import org.slf4j.LoggerFactory
import repository.module.FolioRepository
import utils.DateTimeUtils

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 25-01-2017.
  */
class ReportService @Inject()(implicit ec: ExecutionContext, reportRepository: FolioRepository,
                              schemeHelper: SchemeHelper, orderCancelService: OrderCancelService) extends OrderConstants{

  val logger, log = LoggerFactory.getLogger(classOf[ReportService])

  def getPortfolioAssetAllocationDetails(userPk: Long): Future[ListBuffer[FundDetails]] = {

    reportRepository.getPortfolioDetails(userPk).flatMap { values =>

      reportRepository.getFundCurrNAV(values).map { fundDetailsList =>

        fundDetailsList
      }
    }
  }

  def getFundDetails(userPk: Long): Future[ListBuffer[FundDetails]] = {

    reportRepository.getFundDetails(userPk).flatMap { values =>

      reportRepository.getFundCurrNAV(values).map { fundDetailsList =>

        fundDetailsList
      }
    }
  }

  def getTransactionFundDetails(userPk: Long, transactionFilter: TransactionFilter): Future[ListBuffer[FundDetails]] = {

    reportRepository.getTransactionFundDetails(userPk, transactionFilter).flatMap { values =>

      reportRepository.getFundCurrNAV(values).map { fundDetailsList =>

        fundDetailsList
      }
    }
  }

  def getCapitalGainFundDetails(userPk: Long, financialYear: Int): Future[ListBuffer[FundDetails]] = {

    val finYearDateRange = DateTimeUtils.getFinancialYear(financialYear)
    val startDate: Date = finYearDateRange._1
    val endDate: Date = finYearDateRange._2
    reportRepository.getCapitalGainFundDetails(userPk, startDate, endDate).flatMap { values =>
      reportRepository.getFundCurrNAV(values).map { fundDetailsList =>
        fundDetailsList
      }
    }
  }

  def getAssetAllocationFundDetails(userPk: Long): Future[ListBuffer[FundDetails]] = {

    reportRepository.getAssetAllocationFundDetails(userPk).flatMap { values =>

      reportRepository.getFundCurrNAV(values).map { fundDetailsList =>

        fundDetailsList
      }
    }
  }

  def getIRRFundDetails(userPk: Long): Future[ListBuffer[Transaction]] = {
    reportRepository.getIRRFundDetails(userPk).map { transactions =>
      transactions
    }
  }

  def getUserOrders(userPk: Long): Future[List[TransactionSummary]] = {
    reportRepository.getUserTransactions(userPk).flatMap(data => {
      val subOrderIdList = ListBuffer[Long]()
      val transSummaryList = ListBuffer[TransactionSummary]()
      data.foreach(trnSummary =>{
        if(trnSummary.subOrderState != ORDER_CANCELLED){

          subOrderIdList.+=(trnSummary.subOrderId)
        }
      })
      orderCancelService.isCancellationAllowed(subOrderIdList,userPk).map(sotCancelMap =>{
        data.foreach(trnSummary =>{
          transSummaryList.+=(trnSummary.copy(schemePlan = schemeHelper.getSchemePlan(trnSummary.schemePlan),
            dividendFreq = schemeHelper.getDivFrequency(trnSummary.dividendFreq),
            dividendOption = schemeHelper.getDivOption(trnSummary.dividendOption),
            cancelAllowed = sotCancelMap.getOrElse(trnSummary.subOrderId,false)))
        })
        transSummaryList.toList
      })
    });
  }
}
