package controllers

import javax.inject.Inject

import constants.CNDConstants
import helpers.{AuthenticatedAction, DecimalFormat}
import models.FundDetailJsonFormats._
import models.HoldingJsonFormats._
import models.ReportsJsonFormats._
import models._
import models.enumerations.AssetClassEnum
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.Controller
import service._

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class ReportsController @Inject()(implicit ec: ExecutionContext, auth: AuthenticatedAction, portfolioSummaryService: PortfolioSummaryService, capitalGainsService: CapitalGainsReportService,
                                  holdingReportService: HoldingsReportService, transactionReportService: TransactionReportService, reportService: ReportService,
                                  userService: UserService, cNDService: CNDService) extends Controller with CNDConstants {

  val logger, log = LoggerFactory.getLogger(classOf[ReportsController])

  def getPortfolioAssetAllocation() = auth.Action.async { request =>

    val assetClassDetailsList: ListBuffer[AssetClassDetails] = ListBuffer[AssetClassDetails]()

    userService.getUserObjectFromReq(request).flatMap { userObject =>
      reportService.getPortfolioAssetAllocationDetails(userObject.get.userid.get).flatMap { fundDetailsList =>
        reportService.getCapitalGainPurchaseTxns(userObject.get.userid.get,fundDetailsList).flatMap(purchaseFunds =>{

          val assetClassCurrentValueModel = portfolioSummaryService.calculateCurrentValue(fundDetailsList)
          val currentValue = assetClassCurrentValueModel.totalCurrentValue
          val asOfDate = assetClassCurrentValueModel.asOfDate
          val assetClassCurrentValueMap = assetClassCurrentValueModel.assetClassCurrentValueMap
          val assetClassCostValueModel = portfolioSummaryService.calculateCostValue(fundDetailsList)
          val assetClassCostValueMap = assetClassCostValueModel.assetClassCostValueMap
          val costValue = assetClassCostValueModel.totalCostValue
          val unrealizedGain = currentValue - costValue
          val capitalGainsFunds: ListBuffer[FundDetails] = capitalGainsService.calculateCapitalGains(fundDetailsList,purchaseFunds)
          var realizedGain: BigDecimal = BigDecimal(0.0)
          for (fund <- capitalGainsFunds) {
            realizedGain += fund.totalLongTermGain.getOrElse(0.0)
            realizedGain += fund.totalShortTermGain.getOrElse(0.0)
          }
          reportService.getPayoutGains(fundDetailsList).map(totalPayoutGain =>{
            realizedGain += totalPayoutGain
            for (assetClass <- AssetClassEnum.values) {
              if (assetClass != AssetClassEnum.HYBRID_EQUITY && assetClass != AssetClassEnum.HYBRID_DEBT && assetClass != AssetClassEnum.HYBRID_FUNDS) {
                val assetClassCostValue = assetClassCostValueMap.getOrElse(assetClass, BigDecimal(0.0))
                var investedClassShare = BigDecimal(0.0)
                if (costValue != 0) {
                  investedClassShare = (assetClassCostValue * 100) / costValue
                }
                val assetClassCurrentValue = assetClassCurrentValueMap.getOrElse(assetClass, BigDecimal(0.0))
                var currentClassShare = BigDecimal(0.0)
                if (currentValue != 0) {
                  currentClassShare = (assetClassCurrentValue * 100) / currentValue
                }
                var currentCostRatio = BigDecimal(0.0)
                if (assetClassCostValue != 0) {
                  currentCostRatio = (assetClassCurrentValue - assetClassCostValue) * 100 / assetClassCostValue
                }

                assetClassDetailsList.+=(AssetClassDetails(assetClass, assetClassCostValue, investedClassShare, assetClassCurrentValue, currentClassShare, currentCostRatio))
              }

            }
            val portfolioAssetAllocation: PortfolioAssetAllocation = PortfolioAssetAllocation(currentValue, asOfDate, costValue, unrealizedGain, realizedGain,
              assetClassDetailsList)

            Ok(Json.toJson(portfolioAssetAllocation))
          })
        })
      }

    }

  }

  def getCapitalGainReport(financialYear: String) = auth.Action.async { request =>

    userService.getUsernameFromRequest(request).flatMap { userName =>
      userService.getUserByUserName(userName.get).flatMap { _user =>
        if (_user.nonEmpty){
          val user = _user.get
          reportService.getCapitalGainFundDetails(user.id, financialYear.toInt).flatMap { fundDetailsList =>
            reportService.getCapitalGainPurchaseTxns(user.id,fundDetailsList).flatMap(purchaseFunds =>{

              val capitalGainsFunds: ListBuffer[FundDetails] = capitalGainsService.calculateCapitalGains(fundDetailsList,purchaseFunds)
              val cumulativeGains = capitalGainsService.calculateCumulativeGain(capitalGainsFunds)
              cNDService.getCNDByPk(user.ubdcndtaxstatusrfnum.getOrElse(TAX_STATUS_INDIVIDUAL)).map { cndRow =>
                val capitalGainFundModel: CapitalGainFundModel = CapitalGainFundModel(cndRow.get.cndname, user.ubdpan, capitalGainsFunds, cumulativeGains._1, cumulativeGains._2)
                Ok(Json.toJson(capitalGainFundModel))
              }
            })
          }
        }
        else {
          Future.apply(Ok(Json.obj("success" -> false, "error" -> "user not found", "message" -> "user not found")))
        }
      }
    }
  }

  def getHoldingReport = auth.Action.async(parse.json) { request =>

    userService.getUserObjectFromReq(request).flatMap { userObject =>
      val holdingFilter = request.body.as[HoldingFilter]
      reportService.getFundDetails(userObject.get.userid.get,holdingFilter).flatMap { fundDetailsList =>

        //logger.debug(fundDetailsList+"")
        reportService.getCapitalGainPurchaseTxns(userObject.get.userid.get,fundDetailsList).flatMap(purchaseFunds =>{

          val assetClassCurrentValue = portfolioSummaryService.calculateCurrentValue(fundDetailsList)
          val currentValue = assetClassCurrentValue.totalCurrentValue
          val assetClassCurrentValueMap = assetClassCurrentValue.assetClassCurrentValueMap
          val assetClassCostValue = portfolioSummaryService.calculateCostValue(fundDetailsList)
          val assetClassCostValueMap = assetClassCostValue.assetClassCostValueMap
          val costValue = assetClassCostValue.totalCostValue
          val unrealizedGain = currentValue - costValue
          val capitalGainsFunds: ListBuffer[FundDetails] = capitalGainsService.calculateCapitalGains(fundDetailsList,purchaseFunds)

          holdingReportService.getHoldings(capitalGainsFunds, currentValue, costValue).map(holdingsList =>{
            Ok(Json.toJson(holdingsList))
          })
        })
      }
    }
  }

  def getTransactionFilterValues() = auth.Action.async { request =>

    userService.getUsernameFromRequest(request).flatMap { userName =>
      userService.getUserByUserName(userName.get).flatMap { _user =>
        if (_user.nonEmpty){
          val user = _user.get
          reportService.getTransactionFundDetails(user.id, TransactionFilter(None, None, None, None,None)).map { fundDetailsList =>
            var fundTransactionReportList: List[FundTransactionReport] = reportService.getFundTxnReport(fundDetailsList)
            val fundFilter:List[FundTransactionReport] = reportService.getFundFilter(fundTransactionReportList)
            val folioFilter:List[FolioFilter] = reportService.getFolioFilter(fundTransactionReportList)
            val transactionTypesList = transactionReportService.getTransactionTypes()
            val transactionReport: TransactionReport = TransactionReport(user.ubdmobileno.getOrElse(""), user.ubdemailid, transactionTypesList, fundTransactionReportList,
              fundFilter = Some(fundFilter), folioFilter = Some(folioFilter))
            Ok(Json.toJson(transactionReport))
          }
        }
        else {
          Future.apply(Ok(Json.obj("success" -> false, "error" -> "user not found", "message" -> "user not found")))
        }
      }
    }
  }

  def getFolioTxnFilterByFund() = auth.Action.async(parse.json) { request =>
    userService.getUsernameFromRequest(request).flatMap { userName =>
      userService.getUserByUserName(userName.get).flatMap { _user =>
        if (_user.nonEmpty) {
          val user = _user.get
          val transactionFilter = request.body.as[TransactionFilter]
          reportService.getTransactionFundDetails(user.id, transactionFilter).map { fundDetailsList =>
            var fundTransactionReportList: List[FundTransactionReport] = reportService.getFundTxnReport(fundDetailsList)
            val folioFilter:List[FolioFilter] = reportService.getFolioFilter(fundTransactionReportList)
            val transactionTypesList = transactionReportService.getTransactionTypes()
            val transactionReport: TransactionReport = TransactionReport(user.ubdmobileno.getOrElse(""), user.ubdemailid, transactionTypesList, fundTransactionReportList,
              folioFilter = Some(folioFilter))
            Ok(Json.toJson(transactionReport))
          }

        } else {
          Future.apply(Ok(Json.obj("success" -> false, "error" -> "user not found", "message" -> "user not found")))
        }
      }
    }
  }

  def getTransactionReport() = auth.Action.async(parse.json) { request =>

    val transactionFilter = request.body.as[TransactionFilter]

    userService.getUsernameFromRequest(request).flatMap { userName =>
      userService.getUserByUserName(userName.get).flatMap { _user =>
        if (_user.nonEmpty){
          val user = _user.get
          reportService.getTransactionFundDetails(user.id, transactionFilter).flatMap { fundDetailsList =>
            reportService.addSchemePayouts(fundDetailsList).flatMap(payoutTxns =>{
              transactionReportService.getTransactionReport(fundDetailsList, transactionFilter, user.id).map { fundTransactionsList =>
                val transactionTypesList = transactionReportService.getTransactionTypes()
                val transactionReport: TransactionReport = TransactionReport(user.ubdmobileno.getOrElse(""), user.ubdemailid, transactionTypesList, fundTransactionsList)
                Ok(Json.toJson(transactionReport))
              }
            })
          }
        }
        else {
          Future.apply(Ok(Json.obj("success" -> false, "error" -> "user not found", "message" -> "user not found")))
        }
      }
    }
  }

  def getAssetAllocationReport() = auth.Action.async { request =>

    val assetAllocationList: ListBuffer[AssetAllocationModel] = ListBuffer[AssetAllocationModel]()
    userService.getUserObjectFromReq(request).flatMap { userObject =>
      reportService.getAssetAllocationFundDetails(userObject.get.userid.get).map { fundDetailsList =>

        val assetClassCurrentValueModel = portfolioSummaryService.calculateCurrentValue(fundDetailsList)
        val currentValue = assetClassCurrentValueModel.totalCurrentValue
        val assetClassCurrentValueMap = assetClassCurrentValueModel.assetClassCurrentValueMap

        for (assetClass <- AssetClassEnum.values) {
          if (assetClass == AssetClassEnum.EQUITY_FUNDS || assetClass == AssetClassEnum.DEBT_FUNDS || assetClass == AssetClassEnum.GOLD_FUND) {
            val assetClassCurrentValue = assetClassCurrentValueMap.getOrElse(assetClass, BigDecimal(0.0))

            val formattedAssetClassCurrentValue = DecimalFormat.formatDecimalPlace(assetClassCurrentValue)

            assetAllocationList.+=(AssetAllocationModel(assetClass, formattedAssetClassCurrentValue))
          }
        }

        Ok(Json.toJson(assetAllocationList))
      }
    }
  }

  def getUserTransactions() = auth.Action.async { request =>
    userService.getUserObjectFromReq(request).flatMap { userObject =>
      if (userObject.nonEmpty && userObject.get.userid.nonEmpty){
        reportService.getUserOrders(userObject.get.userid.get).map(data => {
          Ok(Json.toJson(data))
        })
      }
      else {
        Future.apply(Ok(Json.obj("success" -> false, "message" -> "User not logged in")))
      }
    }
  }

}