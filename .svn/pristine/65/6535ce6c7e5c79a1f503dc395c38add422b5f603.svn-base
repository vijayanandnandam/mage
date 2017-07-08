package service

import javax.inject.Inject

import constants.DBConstants
import data.model.Tables.{FcaiptRow, FcamctRow, FcsoptRow}
import helpers.SchemeHelper
import models._
import models.enumerations.AssetClassEnum
import models.enumerations.AssetClassEnum.AssetClassEnum
import org.slf4j.LoggerFactory
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Producer.nameValue2Producer
import repository.module.SchemeRepository

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class SchemeService @Inject()(schemeRepository: SchemeRepository,
                              schemeHelper: SchemeHelper) extends DBConstants {

  val logger, log = LoggerFactory.getLogger(classOf[SchemeService])


  def getScheme(id: String, schemeMaster: Future[BSONCollection]): Future[FundDoc] = {

    val query = BSONDocument("fid" -> id)

    schemeMaster.flatMap { collection =>
      collection.find(query).one[FundDoc].map { fndDoc => fndDoc.get }
    }

  }

  def getSchemes(idList: List[BSONDocument], schemeMaster: Future[BSONCollection]): Future[ListBuffer[FundDoc]] = {

    val fundList = ListBuffer[FundDoc]();
    schemeMaster.map { collection =>
      idList.foreach { doc =>
        val fundId = doc.getAs[Int]("fundId").get.toString()
        logger.debug(fundId)
        val query = BSONDocument("fid" -> fundId);
        val fundFuture = collection.find(query).one[FundDoc].map { fndDoc =>

          /*val fund = fndDoc.get.copy(investmentMode = Some(InvestmentModeEnum.withName(doc.getAs[String]("investmentMode").get)));
          fundList.+=(fund);*/
        }
        Await.ready(fundFuture, Duration.Inf);
      }
      logger.debug(fundList.toString());
      fundList;
    }
  }

  def mapSchemesToAssetClass(fundDetailsList: ListBuffer[FundDetails]): ListBuffer[AssetClassFundDetails] = {

    val assetClassFundDetailsList = ListBuffer[AssetClassFundDetails]()
    val assetClassFundListMap = mutable.HashMap[AssetClassEnum, ListBuffer[DashboardFundDetail]]()
    assetClassFundListMap.+=(AssetClassEnum.EQUITY_FUNDS -> ListBuffer[DashboardFundDetail]())
    assetClassFundListMap.+=(AssetClassEnum.DEBT_FUNDS -> ListBuffer[DashboardFundDetail]())
    assetClassFundListMap.+=(AssetClassEnum.GOLD_FUND -> ListBuffer[DashboardFundDetail]())

    for (fund <- fundDetailsList) {
      if (!assetClassFundListMap.get(fund.assetClass).isEmpty) {
        val fundDetailList = assetClassFundListMap.get(fund.assetClass).get
        fundDetailList.+=(DashboardFundDetail(fund.fundId, fund.fundBasicDetails.fundName, fund.currentValue))
        assetClassFundListMap.+=(fund.assetClass -> fundDetailList)
      }
    }
    for (key <- assetClassFundListMap) {

      assetClassFundDetailsList.+=(AssetClassFundDetails(key._1, key._2))
    }

    assetClassFundDetailsList
  }

  def getTopThreeSchemes(fundDetails: ListBuffer[FundDetails]): ListBuffer[DashboardTopFund] = {

    val fundsList: ListBuffer[DashboardTopFund] = ListBuffer[DashboardTopFund]()
//    val topFundsList: ListBuffer[DashboardTopFund] = ListBuffer[DashboardTopFund]()
    val fundListFuture = for (fund <- fundDetails) yield {

      schemeRepository.getSchemeLastReturn(MONTH, 3, fund.fundId).map(value => {
        if (value.nonEmpty){
          if(value.get.nonEmpty){
            fundsList.+=(DashboardTopFund(fund.fundId, fund.fundBasicDetails.fundName, BigDecimal(value.get.get.toDouble)))
          }
        }
      })
    }
    val future = Future.sequence(fundListFuture).map(_ => ())
    Await.result(future, Duration.Inf);
    if (fundsList.nonEmpty){
      val sortedFundsList = fundsList.sortWith(_.fundReturn < _.fundReturn).take(3)
      /*topFundsList.+=(sortedFundsList.take(3)
      topFundsList.+=(DashboardTopFund(sortedFundsList(1).fundId,sortedFundsList(1).fundName))
      topFundsList.+=(DashboardTopFund(sortedFundsList(2).fundId,sortedFundsList(2).fundName))*/
      sortedFundsList
    }
    else {
      fundsList
    }
  }

  def getSchemeAIPDataByOptionsList(schemeOptions: List[FcsoptRow]): Future[List[FcaiptRow]] = {
    Future.sequence(for (option <- schemeOptions) yield {
      schemeRepository.getAllAIPDataByOptionId(option.id)
    }).map(data => {
      data.flatten;
    })
  }

  def getSchemeIdByOptionId(soptRfnum: String): Future[Long] = {
    schemeRepository.getSchemeOptionById(soptRfnum.toLong).map(sopt => sopt.soptsmtrfnum);
  }

  def getSchemeOptionAmcDetails(soptrfnum:Long):Future[FcamctRow] = {
    schemeRepository.getSchemeOptionAmcDetails(soptrfnum).map(amctRowList => amctRowList.head)
  }
}