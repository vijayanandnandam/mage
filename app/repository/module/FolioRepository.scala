package repository.module


import java.sql.Date
import java.util
import java.util.{Calendar, Locale}
import javax.inject.Singleton

import com.google.inject.Inject
import constants._
import data.model.Tables.{Fcdsa, Fcdsd, FcfhsRow, Fcfht, FcfhtRow, Fcfomt, FcfomtRow, Fcfpt, FcfptRow, Fcsopt, Fcuft}
import helpers.SchemeHelper
import models._
import models.enumerations.AssetClassEnum
import models.integration.enumerations.BuySellEnum
import models.integration.enumerations.BuySellEnum.BuySellEnum
import org.slf4j.LoggerFactory
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables.{FcdsdRepo, FcfhsRepo, FcomtRepo, FcsotRepo}
import slick.jdbc._
import utils.{Calculation, DateTimeUtils}

import scala.collection.mutable
import scala.collection.mutable.{HashMap, ListBuffer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fincash on 25-01-2017.
  */

@Singleton
class FolioRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, fcdsdRepo: FcdsdRepo,
                                schemeHelper: SchemeHelper, fcomtRepo: FcomtRepo, fcsotRepo: FcsotRepo, fcfhsRepo: FcfhsRepo)
  extends HasDatabaseConfigProvider[JdbcProfile] with CategoryConstants with MapperConstants with FolioConstants with DateConstants {

  val logger, log = LoggerFactory.getLogger(classOf[FolioRepository])

  import SlickKit._
  import profile.api._

  //Gets Folio no based on the maximum count of schemes in that folio
  def getAMCFolioNo(userPk: Long, amctrfnum: Long): Future[Option[String]] = {

    val query = sql"""SELECT
                      folioDetail.FOMTFOLIONO, COUNT(*) AS SCHEMECOUNT
                  FROM
                      FCFHT,
                      (SELECT
                          FOMTFOLIONO, FOMTRFNUM
                      FROM
                          FCFOMT
                      WHERE
                          FOMTAMCTRFNUM = ${amctrfnum}
                              AND FOMTRFNUM IN (SELECT
                                  UFTFOMTRFNUM
                              FROM
                                  FCUFT
                              WHERE
                                  UFTUBDRFNUM = ${userPk})
                              AND FOMTMERGEDRFNUM IS NULL) folioDetail
                  WHERE
                      folioDetail.FOMTRFNUM = FHTFOMTRFNUM
                  GROUP BY FHTFOMTRFNUM""".as[(String, Long)]

    db.run(query).map(values => {
      if (values.isEmpty) {
        None
      } else {
        Some(values.head._1)
      }
    })
  }

  def getSchemeFolioNo(userPk: Long, soptrfnum: Long): Future[Seq[FcfomtRow]] = {

    val unitsLimit: Option[Double] = Some(0.0)
    val query = for {
      smtList <- Fcsopt.filter(_.id === soptrfnum).map(_.soptsmtrfnum).result
      soptList <- Fcsopt.filter(_.soptsmtrfnum inSet(smtList)).map(_.id).result
      fomtObj <- Fcfomt.filter(_.id in Fcfht.filter(x => ((x.fhtfomtrfnum in (Fcuft.filter(_.uftubdrfnum === userPk).map(_.uftfomtrfnum))) && ((x.fhtsoptrfnum inSet(soptList)) && x.fhtholdingunits > unitsLimit))).map(_.fhtfomtrfnum)).result
    } yield (fomtObj)

    db.run(query)
  }

  def getMaxHoldingFolioId(fomtrfnumList: List[Long]): Future[Option[Long]] = {

    val folioList = fomtrfnumList.mkString(",")
    val query = sql"""SELECT
          SUM(FHTHOLDINGUNITS), FHTFOMTRFNUM
      FROM
          FCFHT
      WHERE
          FHTFOMTRFNUM IN (#$folioList)
      GROUP BY FHTFOMTRFNUM
      ORDER BY 1 DESC
      LIMIT 1;""".as[(Long, Long)]
    db.run(query).map(maxFolioIdList => {
      if (maxFolioIdList.nonEmpty) {
        Some(maxFolioIdList.head._2)
      } else {
        None
      }
    })

  }

  def getFundDetails(userPk: Long,holdingFilter:HoldingFilter): Future[ListBuffer[FundDetails]] = {

    val liquidId = LIQUID_ID
    val elssId = ELSS_ID
    var query =
      sql"""SELECT
    smtFolioDetails.FHSSOPTRFNUM,
    smtFolioDetails.FOMTFOLIONO,
    smtFolioDetails.SOPTLEGALNAME,
    smtFolioDetails.SOPTSCHEMEPLAN,
    smtFolioDetails.AMCTDISPLAYNAME,
    ctmtPr.catName,
    ctmtPr.parentId,
    smtFolioDetails.FHSDESCRIPTION,
    smtFolioDetails.FHSTRADEDATE,
    smtFolioDetails.FHSTYPE,
    smtFolioDetails.FHSUNITS,
    smtFolioDetails.FHSPRICE,
    smtFolioDetails.FHSAMOUNT,
    smtFolioDetails.FOMTRFNUM,
    smtFolioDetails.FOMTMODEOFHOLDING,
    smtFolioDetails.SMTDISPLAYNAME,
    smtFolioDetails.SOPTDIVIDENDFRQN,
    smtFolioDetails.SOPTDIVIOPTIONTYPE,
    smtFolioDetails.FHTRFNUM
FROM
    (SELECT
        soptFolioDetails.FHSSOPTRFNUM,
            smt.SMTCTMTRFNUM,
            smt.SMTDISPLAYNAME,
            soptFolioDetails.FOMTFOLIONO,
            soptFolioDetails.SOPTLEGALNAME,
            soptFolioDetails.SOPTSCHEMEPLAN,
            soptFolioDetails.SOPTDIVIDENDFRQN,
            soptFolioDetails.SOPTDIVIOPTIONTYPE,
            soptFolioDetails.AMCTDISPLAYNAME,
            soptFolioDetails.FHSDESCRIPTION,
            soptFolioDetails.FHSTRADEDATE,
            soptFolioDetails.FHSTYPE,
            soptFolioDetails.FHSUNITS,
            soptFolioDetails.FHSPRICE,
            soptFolioDetails.FHSAMOUNT,
            soptFolioDetails.FOMTRFNUM,
            soptFolioDetails.FOMTMODEOFHOLDING,
            soptFolioDetails.FHTRFNUM
    FROM
        FCSMT smt, (SELECT
        sopt.SOPTSMTRFNUM,
            folioDetails.FHSSOPTRFNUM,
            folioDetails.FOMTFOLIONO,
            sopt.SOPTLEGALNAME,
            sopt.SOPTSCHEMEPLAN,
            sopt.SOPTDIVIDENDFRQN,
            sopt.SOPTDIVIOPTIONTYPE,
            folioDetails.AMCTDISPLAYNAME,
            folioDetails.FHSDESCRIPTION,
            folioDetails.FHSTRADEDATE,
            folioDetails.FHSTYPE,
            folioDetails.FHSUNITS,
            folioDetails.FHSPRICE,
            folioDetails.FHSAMOUNT,
            folioDetails.FOMTRFNUM,
            folioDetails.FOMTMODEOFHOLDING,
            folioDetails.FHTRFNUM
    FROM
        FCSOPT sopt, (SELECT
        fhsData.FHSSOPTRFNUM,
            fhsData.FOMTFOLIONO,
            fhsData.AMCTDISPLAYNAME,
            fhsData.FHSDESCRIPTION,
            fhsData.FHSTRADEDATE,
            fhsData.FHSTYPE,
            fhsData.FHSUNITS,
            fhsData.FHSPRICE,
            fhsData.FHSAMOUNT,
            fhsData.FOMTRFNUM,
            fhsData.FOMTMODEOFHOLDING,
            fhsData.FHTRFNUM
    FROM
        (SELECT
        SUM(FHTHOLDINGUNITS) folioHoldingUnits,
            FHTFOMTRFNUM,
            FHTSOPTRFNUM
    FROM
        FCFHT
    GROUP BY FHTFOMTRFNUM , FHTSOPTRFNUM) folioTotalUnitsResult, (SELECT
        fhs.FHSSOPTRFNUM,
            fomtfhc.FOMTFOLIONO,
            fomtfhc.AMCTDISPLAYNAME,
            fhs.FHSDESCRIPTION,
            fhs.FHSTRADEDATE,
            fhs.FHSTYPE,
            fhs.FHSUNITS,
            fhs.FHSPRICE,
            fhs.FHSAMOUNT,
            fomtfhc.FOMTRFNUM,
            fomtfhc.FOMTMODEOFHOLDING,
            fomtfhc.FHTRFNUM
    FROM
        FCFHS fhs, (SELECT
        fht.FHTRFNUM,
            fomtamc.FOMTFOLIONO,
            fomtamc.FOMTRFNUM,
            fomtamc.AMCTDISPLAYNAME,
            fomtamc.FOMTMODEOFHOLDING
    FROM
        FCFHT fht, (SELECT
        fomtResult.FOMTFOLIONO,
            fomtResult.FOMTRFNUM,
            amct.AMCTDISPLAYNAME,
            fomtResult.FOMTMODEOFHOLDING
    FROM
        FCAMCT amct, (SELECT
        fomt.FOMTFOLIONO,
            fomt.FOMTRFNUM,
            fomt.FOMTAMCTRFNUM,
            fomt.FOMTMODEOFHOLDING
    FROM
        FCFOMT fomt
    WHERE
        fomt.FOMTRFNUM IN (SELECT
                UFTFOMTRFNUM
            FROM
                FCUFT
            WHERE
                UFTUBDRFNUM = ${userPk})) fomtResult
    WHERE
        amct.AMCTRFNUM = fomtResult.FOMTAMCTRFNUM) fomtamc
    WHERE
        fht.FHTFOMTRFNUM = fomtamc.FOMTRFNUM
            AND fht.FHTSTATUS != ${UNITS_REVERSED}) fomtfhc
    WHERE
        fhs.FHSFHTRFNUM = fomtfhc.FHTRFNUM) fhsData
    WHERE
        folioTotalUnitsResult.FHTFOMTRFNUM = fhsData.FOMTRFNUM """

    if(!holdingFilter.emptyHolding){
      query = query concat sql""" AND folioTotalUnitsResult.folioHoldingUnits > 0 """
    }

    query = query concat sql"""AND folioTotalUnitsResult.FHTSOPTRFNUM = fhsData.FHSSOPTRFNUM
    ORDER BY fhsData.FHSSOPTRFNUM , fhsData.FOMTRFNUM , fhsData.FHSTRADEDATE) folioDetails
    WHERE
        sopt.SOPTRFNUM = folioDetails.FHSSOPTRFNUM) soptFolioDetails
    WHERE
        soptFolioDetails.SOPTSMTRFNUM = smt.SMTRFNUM) smtFolioDetails,
    (SELECT
        ch.CTMTRFNUM AS childId,
            pr.CTMTRFNUM AS parentId,
            CASE
                WHEN ch.CTMTRFNUM = ${liquidId}|| ch.CTMTRFNUM = ${elssId} THEN ch.CTMTNAME
                ELSE pr.CTMTNAME
            END AS catName
    FROM
        FCCTMT pr, FCCTMT ch
    WHERE
        pr.CTMTRFNUM = ch.CTMTCTMTRFNUM) ctmtPr
WHERE
    ctmtPr.childId = smtFolioDetails.SMTCTMTRFNUM
    ORDER BY smtFolioDetails.FHSSOPTRFNUM , smtFolioDetails.FOMTRFNUM , smtFolioDetails.FHSTRADEDATE"""

    getFundDetailsResult(query)
  }

  private def getFundDetailsResult(sQLActionBuilder: SQLActionBuilder): Future[ListBuffer[FundDetails]] = {

    implicit val getTransactionSummary = GetResult(r => ReportModel(r.nextLong, r.nextString, r.nextString, r.nextString,
      r.nextString, r.nextString, r.nextLong, r.nextString, r.nextDate, r.nextString, r.nextBigDecimal, r.nextBigDecimal, r.nextBigDecimal,
      r.nextLong, r.nextString, r.nextString, r.nextString, r.nextString, r.nextLong));

    val query = sQLActionBuilder.as[ReportModel]
    val fundMap = HashMap.empty[Long, HashMap[Long, Boolean]]
    val fundDetailList: ListBuffer[FundDetails] = ListBuffer[FundDetails]()

    db.run(query).map { values => {
      values.foreach { reportModel =>
        val fundId = reportModel.fundId
        if (fundMap.contains(fundId) && fundMap.get(fundId).get.contains(reportModel.folioId)) {

          val transactionList = fundDetailList(fundDetailList.size - 1).transactionList
          transactionList.+=(FundTransaction(reportModel.folioHoldingId,reportModel.desc, reportModel.tradeDate, BuySellEnum.withName(reportModel.transType), reportModel.units, reportModel.price, Some(reportModel.amount),folioId = Some(reportModel.folioId)))
          fundDetailList(fundDetailList.size - 1) = fundDetailList(fundDetailList.size - 1).copy(transactionList = transactionList)
        } else {
          val folioMap = mutable.HashMap.empty[Long, Boolean]
          folioMap.+=(reportModel.folioId -> true)
          fundMap.+=(fundId -> folioMap)
          val fundTransactionList: ListBuffer[FundTransaction] = ListBuffer[FundTransaction]()
          fundTransactionList.+=(FundTransaction(reportModel.folioHoldingId,reportModel.desc, reportModel.tradeDate, BuySellEnum.withName(reportModel.transType), reportModel.units, reportModel.price, Some(reportModel.amount),folioId = Some(reportModel.folioId)))
          val schemePlan = schemeHelper.getSchemePlan(reportModel.schemePlan)
          val divFreq = schemeHelper.getDivFrequency(reportModel.divFreq)
          val dividendOption = schemeHelper.getDivOption(reportModel.divOption)
          val fundBasicDetails = FundBasicDetails(reportModel.folioNo, reportModel.schemeDisplayName, ModeOfHoldingConstants.MODE_OF_HOLDING_MAP.getOrElse(reportModel.modeOfHolding, ""), schemePlan, divFreq, dividendOption, SchemePlan.SCHEME_PLAN_MAP.get(reportModel.schemePlan))
          val fundDetail = FundDetails(fundId, fundBasicDetails, reportModel.amcName, AssetClassEnum.withName(reportModel.category), reportModel.categoryId, 0, None, fundTransactionList)
          fundDetailList.+=(fundDetail.copy(folioId = Some(reportModel.folioId)))
        }
      }
      fundDetailList
    }
    }
  }

  def getHoldingFundDetails(userPk: Long): Future[ListBuffer[HoldingFund]] = {
    val query =
      sql"""SELECT fht.FHTRFNUM,
  fht.FHTSOPTRFNUM,
  folioMaster.FOMTFOLIONO,
  folioMaster.FOMTMODEOFHOLDING,
SUM(fht.FHTHOLDINGUNITS) FROM FCFHT fht,

  (SELECT
     fomt.FOMTFOLIONO,
     fomt.FOMTRFNUM,
     fomt.FOMTMODEOFHOLDING
   FROM
     FCFOMT fomt
   WHERE
     fomt.FOMTRFNUM IN (SELECT
                          UFTFOMTRFNUM
                        FROM
                          FCUFT
                        WHERE
                          UFTUBDRFNUM = ${userPk})) folioMaster
WHERE fht.FHTFOMTRFNUM = folioMaster.fomtrfnum AND fht.FHTHOLDINGUNITS != 0.0
GROUP BY fht.FHTFOMTRFNUM,fht.FHTSOPTRFNUM ORDER BY fht.FHTFOMTRFNUM asc"""

    val holdingFundList: ListBuffer[HoldingFund] = ListBuffer[HoldingFund]()
    db.run(query.as[(Long, Long, String, String, BigDecimal)]).map { values => {
      values.foreach { value => {
        val holdingFund = HoldingFund(value._1, value._2, value._3, value._4, Calculation.createRounded(value._5.doubleValue()).doubleValue())
        holdingFundList.+=(holdingFund)
      }
      }
      holdingFundList
    }
    }
  }

  private def getRedemptionFundDetailsResult(sqlActionBuilder: SQLActionBuilder) = {
    val query = sqlActionBuilder.as[(Long, Long, String, String, BigDecimal)]
    val holdingFundList: ListBuffer[HoldingFund] = ListBuffer[HoldingFund]()

    db.run(query).map { values => {
      values.foreach { value => {
        val holdingFund = HoldingFund(value._1, value._2, value._3, value._4, value._5)
        holdingFundList.+=(holdingFund)
      }
      }
    }
    }
  }

  def getThreeYearOlderTransactionsByFhtrfnum(fhtrfnum: Long): Future[Seq[FcfhsRow]] = {
    val currDate = DateTimeUtils.getCurrentDate()
    val cal = Calendar.getInstance(Locale.US)
    cal.setTime(currDate)
    cal.add(Calendar.YEAR, -3)
    cal.add(Calendar.DAY_OF_MONTH, -1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 1)
    val cutoffDate = DateTimeUtils.convertCalendarToSqlDate(cal)
    logger.debug(String.valueOf(cutoffDate))
    fcfhsRepo.filter(x => x.fhsfhtrfnum === fhtrfnum && x.fhstradedate <= cutoffDate).map { fhsrows => {
      fhsrows
    }
    }
  }

  def getPortfolioDetails(userPk: Long): Future[ListBuffer[FundDetails]] = {

    val equityId = EQUITY_ID
    val debtId = DEBT_ID
    val liquidId = LIQUID_ID
    val elssId = ELSS_ID
    val debtHybridId = DEBT_HYBRID
    val equityHybridId = EQUITY_HYBRID
    val query =
      sql"""SELECT
    smtFolioDetails.FHSSOPTRFNUM,
    smtFolioDetails.FOMTFOLIONO,
    smtFolioDetails.SOPTLEGALNAME,
    smtFolioDetails.SOPTSCHEMEPLAN,
    smtFolioDetails.AMCTDISPLAYNAME,
    ctmtPr.catName,
    ctmtPr.parentId,
    smtFolioDetails.FHSDESCRIPTION,
    smtFolioDetails.FHSTRADEDATE,
    smtFolioDetails.FHSTYPE,
    smtFolioDetails.FHSUNITS,
    smtFolioDetails.FHSPRICE,
    smtFolioDetails.FHSAMOUNT,
    smtFolioDetails.FOMTRFNUM,
    smtFolioDetails.FOMTMODEOFHOLDING,
    smtFolioDetails.SMTDISPLAYNAME,
    smtFolioDetails.SOPTDIVIDENDFRQN,
    smtFolioDetails.SOPTDIVIOPTIONTYPE,
    smtFolioDetails.FHTRFNUM
FROM
    (SELECT
        soptFolioDetails.FHSSOPTRFNUM,
            smt.SMTCTMTRFNUM,
            smt.SMTDISPLAYNAME,
            soptFolioDetails.FOMTFOLIONO,
            soptFolioDetails.SOPTLEGALNAME,
            soptFolioDetails.SOPTSCHEMEPLAN,
            soptFolioDetails.SOPTDIVIDENDFRQN,
            soptFolioDetails.SOPTDIVIOPTIONTYPE,
            soptFolioDetails.AMCTDISPLAYNAME,
            soptFolioDetails.FHSDESCRIPTION,
            soptFolioDetails.FHSTRADEDATE,
            soptFolioDetails.FHSTYPE,
            soptFolioDetails.FHSUNITS,
            soptFolioDetails.FHSPRICE,
            soptFolioDetails.FHSAMOUNT,
            soptFolioDetails.FOMTRFNUM,
            soptFolioDetails.FOMTMODEOFHOLDING,
            soptFolioDetails.FHTRFNUM
    FROM
        FCSMT smt, (SELECT
        sopt.SOPTSMTRFNUM,
            folioDetails.FHSSOPTRFNUM,
            folioDetails.FOMTFOLIONO,
            sopt.SOPTLEGALNAME,
            sopt.SOPTSCHEMEPLAN,
            sopt.SOPTDIVIDENDFRQN,
            sopt.SOPTDIVIOPTIONTYPE,
            folioDetails.AMCTDISPLAYNAME,
            folioDetails.FHSDESCRIPTION,
            folioDetails.FHSTRADEDATE,
            folioDetails.FHSTYPE,
            folioDetails.FHSUNITS,
            folioDetails.FHSPRICE,
            folioDetails.FHSAMOUNT,
            folioDetails.FOMTRFNUM,
            folioDetails.FOMTMODEOFHOLDING,
            folioDetails.FHTRFNUM
    FROM
        FCSOPT sopt, (SELECT
        fhs.FHSSOPTRFNUM,
            fomtfhc.FOMTFOLIONO,
            fomtfhc.AMCTDISPLAYNAME,
            fhs.FHSDESCRIPTION,
            fhs.FHSTRADEDATE,
            fhs.FHSTYPE,
            fhs.FHSUNITS,
            fhs.FHSPRICE,
            fhs.FHSAMOUNT,
            fomtfhc.FOMTRFNUM,
            fomtfhc.FOMTMODEOFHOLDING,
            fomtfhc.FHTRFNUM
    FROM
        FCFHS fhs, (SELECT
        fht.FHTRFNUM,
            fomtamc.FOMTFOLIONO,
            fomtamc.FOMTRFNUM,
            fomtamc.AMCTDISPLAYNAME,
            fomtamc.FOMTMODEOFHOLDING
    FROM
        FCFHT fht, (SELECT
        fomtResult.FOMTFOLIONO,
            fomtResult.FOMTRFNUM,
            amct.AMCTDISPLAYNAME,
            fomtResult.FOMTMODEOFHOLDING
    FROM
        FCAMCT amct, (SELECT
        fomt.FOMTFOLIONO,
            fomt.FOMTRFNUM,
            fomt.FOMTAMCTRFNUM,
            fomt.FOMTMODEOFHOLDING
    FROM
        FCFOMT fomt
    WHERE
        fomt.FOMTRFNUM IN (SELECT
                UFTFOMTRFNUM
            FROM
                FCUFT
            WHERE
                UFTUBDRFNUM = ${userPk})) fomtResult
    WHERE
        amct.AMCTRFNUM = fomtResult.FOMTAMCTRFNUM) fomtamc
    WHERE
        fht.FHTFOMTRFNUM = fomtamc.FOMTRFNUM AND fht.FHTSTATUS != ${UNITS_REVERSED}) fomtfhc
    WHERE
        fhs.FHSFHTRFNUM = fomtfhc.FHTRFNUM
    ORDER BY fhs.FHSSOPTRFNUM, fomtfhc.FOMTRFNUM , fhs.FHSTRADEDATE) folioDetails
    WHERE
        sopt.SOPTRFNUM = folioDetails.FHSSOPTRFNUM) soptFolioDetails
    WHERE
        soptFolioDetails.SOPTSMTRFNUM = smt.SMTRFNUM) smtFolioDetails,
    (SELECT
        ch.CTMTRFNUM AS childId,
            CASE
                WHEN ch.CTMTRFNUM = ${debtHybridId} THEN ${debtId}
                WHEN ch.CTMTRFNUM = ${equityHybridId} THEN ${equityId}
                ELSE pr.CTMTRFNUM
            END AS parentId,
            CASE
                WHEN ch.CTMTRFNUM = ${debtHybridId} THEN 'Debt'
                WHEN ch.CTMTRFNUM = ${equityHybridId} THEN 'Equity'
                WHEN ch.CTMTRFNUM = ${liquidId}|| ch.CTMTRFNUM = ${elssId} THEN ch.CTMTNAME
                ELSE pr.CTMTNAME
            END AS catName
    FROM
        FCCTMT pr, FCCTMT ch
    WHERE
        pr.CTMTRFNUM = ch.CTMTCTMTRFNUM) ctmtPr
WHERE
    ctmtPr.childId = smtFolioDetails.SMTCTMTRFNUM
        ORDER BY smtFolioDetails.FHSSOPTRFNUM , smtFolioDetails.FOMTRFNUM , smtFolioDetails.FHSTRADEDATE"""

    getFundDetailsResult(query)
  }

  def getNetUnitsBeforeDate(fundId: Long, userId: Long, date: java.util.Date): Future[BigDecimal] = {

    val transactionList: ListBuffer[(BuySellEnum, BigDecimal)] = ListBuffer[(BuySellEnum, BigDecimal)]()
    val transactionDate = DateTimeUtils.convertDateToYYYYMMDD(date)
    val query = sql"""SELECT
    fhs.FHSTYPE, fhs.FHSUNITS
FROM
    FCFHS fhs
WHERE
    fhs.FHSSOPTRFNUM = ${fundId}
        AND FHSFHTRFNUM IN (SELECT
            FHTRFNUM
        FROM
            FCFHT
        WHERE
            FHTFOMTRFNUM IN (SELECT
                    UFTFOMTRFNUM
                FROM
                    FCUFT
                WHERE
                    UFTUBDRFNUM = ${userId}))
        AND DATE(fhs.FHSTRADEDATE) < DATE(${transactionDate})""".as[(String, BigDecimal)]

    db.run(query).map { values =>
      var netUnits = BigDecimal(0.0)
      values.foreach { value =>
        transactionList.+=((BuySellEnum.withName(value._1), value._2))

        if (BuySellEnum.C == BuySellEnum.withName(value._1)) {
          netUnits += value._2
        } else {
          netUnits -= value._2
        }
      }
      netUnits
    }
  }

  def getCurrNavByFundId(fundId: Long): Future[(Double, util.Date)] = {
    val query = for {
    //dsarow <- Fcdsa.filter(x => x.dsasoptrfnum === fundId && x.dsadamrfnum === MSTAR_DATASOURCE_ID).result
      dsdrow <- Fcdsd.filter(x => x.dsddsarfnum in Fcdsa.filter(x => x.dsasoptrfnum === fundId && x.dsadamrfnum === MSTAR_DATASOURCE_ID).map(_.id)).result
    } yield (dsdrow)
    db.run(query.transactionally).map(value => {
      if (value.nonEmpty) {
        val nav = value.head.dsdnav.toDouble
        val navDateOption = value.head.dsdnavasofdate
        var date = DateTimeUtils.getCurrentDate()
        if (navDateOption.nonEmpty) {
          date = navDateOption.get
        }
        (nav, date)
      }
      else {
        (0.0, DateTimeUtils.getCurrentDate())
      }
    }).recover {
      case ex: Exception =>
        logger.error("{}", ex)
        throw ex
    }
  }

  def getTransactionFundDetails(userPk: Long, transactionFilter: TransactionFilter): Future[ListBuffer[FundDetails]] = {
    val fundId = transactionFilter.fundId
    val folioId = transactionFilter.folioId
    val transactionType = transactionFilter.transType
    val liquidId = LIQUID_ID
    val elssId = ELSS_ID

    var sqlQuery =
      sql"""SELECT
    smtFolioDetails.FHSSOPTRFNUM,
    smtFolioDetails.FOMTFOLIONO,
    smtFolioDetails.SOPTLEGALNAME,
    smtFolioDetails.SOPTSCHEMEPLAN,
    smtFolioDetails.AMCTDISPLAYNAME,
    ctmtPr.catName,
    ctmtPr.parentId,
    smtFolioDetails.FHSDESCRIPTION,
    smtFolioDetails.FHSTRADEDATE,
    smtFolioDetails.FHSTYPE,
    smtFolioDetails.FHSUNITS,
    smtFolioDetails.FHSPRICE,
    smtFolioDetails.FHSAMOUNT,
    smtFolioDetails.FOMTRFNUM,
    smtFolioDetails.FOMTMODEOFHOLDING,
    smtFolioDetails.SMTDISPLAYNAME,
    smtFolioDetails.SOPTDIVIDENDFRQN,
    smtFolioDetails.SOPTDIVIOPTIONTYPE,
    smtFolioDetails.FHTRFNUM
FROM
    (SELECT
        soptFolioDetails.FHSSOPTRFNUM,
            smt.SMTCTMTRFNUM,
            smt.SMTDISPLAYNAME,
            soptFolioDetails.FOMTFOLIONO,
            soptFolioDetails.SOPTLEGALNAME,
            soptFolioDetails.SOPTSCHEMEPLAN,
            soptFolioDetails.SOPTDIVIDENDFRQN,
            soptFolioDetails.SOPTDIVIOPTIONTYPE,
            soptFolioDetails.AMCTDISPLAYNAME,
            soptFolioDetails.FHSDESCRIPTION,
            soptFolioDetails.FHSTRADEDATE,
            soptFolioDetails.FHSTYPE,
            soptFolioDetails.FHSUNITS,
            soptFolioDetails.FHSPRICE,
            soptFolioDetails.FHSAMOUNT,
            soptFolioDetails.FOMTRFNUM,
            soptFolioDetails.FOMTMODEOFHOLDING,
            soptFolioDetails.FHTRFNUM,
            soptFolioDetails.CREATEDATE
    FROM
        FCSMT smt, (SELECT
        sopt.SOPTSMTRFNUM,
            folioDetails.FHSSOPTRFNUM,
            folioDetails.FOMTFOLIONO,
            sopt.SOPTLEGALNAME,
            sopt.SOPTSCHEMEPLAN,
            sopt.SOPTDIVIDENDFRQN,
            sopt.SOPTDIVIOPTIONTYPE,
            folioDetails.AMCTDISPLAYNAME,
            folioDetails.FHSDESCRIPTION,
            folioDetails.FHSTRADEDATE,
            folioDetails.FHSTYPE,
            folioDetails.FHSUNITS,
            folioDetails.FHSPRICE,
            folioDetails.FHSAMOUNT,
            folioDetails.FOMTRFNUM,
            folioDetails.FOMTMODEOFHOLDING,
            folioDetails.FHTRFNUM,
            folioDetails.CREATEDATE
    FROM
        FCSOPT sopt, (SELECT
        fhs.FHSSOPTRFNUM,
            fomtfhc.FOMTFOLIONO,
            fomtfhc.AMCTDISPLAYNAME,
            fhs.FHSDESCRIPTION,
            fhs.FHSTRADEDATE,
            fhs.FHSTYPE,
            fhs.FHSUNITS,
            fhs.FHSPRICE,
            fhs.FHSAMOUNT,
            fhs.CREATEDATE,
            fomtfhc.FOMTRFNUM,
            fomtfhc.FOMTMODEOFHOLDING,
            fomtfhc.FHTRFNUM
    FROM
        FCFHS fhs, (SELECT
        fht.FHTRFNUM,
            fomtamc.FOMTFOLIONO,
            fomtamc.FOMTRFNUM,
            fomtamc.AMCTDISPLAYNAME,
            fomtamc.FOMTMODEOFHOLDING
    FROM
        FCFHT fht, (SELECT
        fomtResult.FOMTFOLIONO,
            fomtResult.FOMTRFNUM,
            amct.AMCTDISPLAYNAME,
            fomtResult.FOMTMODEOFHOLDING
    FROM
        FCAMCT amct, (SELECT
        fomt.FOMTFOLIONO,
            fomt.FOMTRFNUM,
            fomt.FOMTAMCTRFNUM,
            fomt.FOMTMODEOFHOLDING
    FROM
        FCFOMT fomt
    WHERE
        fomt.FOMTRFNUM IN (SELECT
                UFTFOMTRFNUM
            FROM
                FCUFT
            WHERE
                UFTUBDRFNUM = ${userPk}) """

    if(!folioId.isEmpty && folioId.get > 0L){

      val folioFilter = sql""" AND fomt.FOMTRFNUM = ${folioId} """
      sqlQuery = sqlQuery concat(folioFilter)
    }
    sqlQuery = sqlQuery concat sql""") fomtResult
    WHERE
        amct.AMCTRFNUM = fomtResult.FOMTAMCTRFNUM) fomtamc
    WHERE
        fht.FHTFOMTRFNUM = fomtamc.FOMTRFNUM) fomtfhc
    WHERE
        fhs.FHSFHTRFNUM = fomtfhc.FHTRFNUM """

    if (!transactionFilter.startDate.isEmpty && !transactionFilter.endDate.isEmpty) {
      val startDate = DateTimeUtils.convertStringDateWithFormats(transactionFilter.startDate.get, ORDER_CUT_OFF_DATE_FORMAT, "yyyy-MM-dd").get
      val endDate = DateTimeUtils.convertStringDateWithFormats(transactionFilter.endDate.get, ORDER_CUT_OFF_DATE_FORMAT, "yyyy-MM-dd").get

      val dateFilter = sql"""AND fhs.FHSTRADEDATE >= ${startDate} AND fhs.FHSTRADEDATE <= ${endDate} """
      sqlQuery = sqlQuery concat dateFilter
    }

    if (!fundId.isEmpty && fundId.get > 0L) {
      val id = fundId.get
      val filterQuery = sql""" AND fhs.FHSSOPTRFNUM = ${id} """
      sqlQuery = sqlQuery concat filterQuery
    }
    if (!transactionType.isEmpty && transactionType.get != "B") {
      val transType = transactionType.get
      val filterQuery = sql""" AND fhs.FHSTYPE = ${transType} """
      sqlQuery = sqlQuery concat filterQuery
    }
    val query1 =
      sql"""ORDER BY fhs.FHSSOPTRFNUM , fomtfhc.FOMTRFNUM, fhs.FHSTRADEDATE, fhs.FHSRFNUM, fhs.CREATEDATE) folioDetails
    WHERE
        sopt.SOPTRFNUM = folioDetails.FHSSOPTRFNUM) soptFolioDetails
    WHERE
        soptFolioDetails.SOPTSMTRFNUM = smt.SMTRFNUM) smtFolioDetails,
    (SELECT
        ch.CTMTRFNUM AS childId,
            pr.CTMTRFNUM AS parentId,
            CASE
                WHEN ch.CTMTRFNUM = ${liquidId}|| ch.CTMTRFNUM = ${elssId} THEN ch.CTMTNAME
                ELSE pr.CTMTNAME
            END AS catName
    FROM
        FCCTMT pr, FCCTMT ch
    WHERE
        pr.CTMTRFNUM = ch.CTMTCTMTRFNUM) ctmtPr
WHERE
    ctmtPr.childId = smtFolioDetails.SMTCTMTRFNUM
        ORDER BY smtFolioDetails.FHSSOPTRFNUM , smtFolioDetails.FOMTRFNUM , smtFolioDetails.FHSTRADEDATE, smtFolioDetails.CREATEDATE"""
    sqlQuery = sqlQuery concat query1

   // println(sqlQuery);

    getFundDetailsResult(sqlQuery)

  }

  def getCapitalGainFundDetails(userPk: Long, transType:String, fhtList:List[Long],startDate: Option[util.Date], endDate: Option[util.Date]): Future[ListBuffer[FundDetails]] = {


    val fhtFormattedId = fhtList.mkString(",")
    val debtHybridId = DEBT_HYBRID
    val equityHybridId = EQUITY_HYBRID
    var query =
      sql"""SELECT
    smtFolioDetails.FHSSOPTRFNUM,
    smtFolioDetails.FOMTFOLIONO,
    smtFolioDetails.SOPTLEGALNAME,
    smtFolioDetails.SOPTSCHEMEPLAN,
    smtFolioDetails.AMCTDISPLAYNAME,
    ctmtPr.catName,
    ctmtPr.parentId,
    smtFolioDetails.FHSDESCRIPTION,
    smtFolioDetails.FHSTRADEDATE,
    smtFolioDetails.FHSTYPE,
    smtFolioDetails.FHSUNITS,
    smtFolioDetails.FHSPRICE,
    smtFolioDetails.FHSAMOUNT,
    smtFolioDetails.FOMTRFNUM,
    smtFolioDetails.FOMTMODEOFHOLDING,
    smtFolioDetails.SMTDISPLAYNAME,
    smtFolioDetails.SOPTDIVIDENDFRQN,
    smtFolioDetails.SOPTDIVIOPTIONTYPE,
    smtFolioDetails.FHTRFNUM
FROM
    (SELECT
        soptFolioDetails.FHSSOPTRFNUM,
            smt.SMTCTMTRFNUM,
            smt.SMTDISPLAYNAME,
            soptFolioDetails.FOMTFOLIONO,
            soptFolioDetails.SOPTLEGALNAME,
            soptFolioDetails.SOPTSCHEMEPLAN,
            soptFolioDetails.SOPTDIVIDENDFRQN,
            soptFolioDetails.SOPTDIVIOPTIONTYPE,
            soptFolioDetails.AMCTDISPLAYNAME,
            soptFolioDetails.FHSDESCRIPTION,
            soptFolioDetails.FHSTRADEDATE,
            soptFolioDetails.FHSTYPE,
            soptFolioDetails.FHSUNITS,
            soptFolioDetails.FHSPRICE,
            soptFolioDetails.FHSAMOUNT,
            soptFolioDetails.FOMTRFNUM,
            soptFolioDetails.FOMTMODEOFHOLDING,
            soptFolioDetails.FHTRFNUM
    FROM
        FCSMT smt, (SELECT
        sopt.SOPTSMTRFNUM,
            folioDetails.FHSSOPTRFNUM,
            folioDetails.FOMTFOLIONO,
            sopt.SOPTLEGALNAME,
            sopt.SOPTSCHEMEPLAN,
            sopt.SOPTDIVIDENDFRQN,
            sopt.SOPTDIVIOPTIONTYPE,
            folioDetails.AMCTDISPLAYNAME,
            folioDetails.FHSDESCRIPTION,
            folioDetails.FHSTRADEDATE,
            folioDetails.FHSTYPE,
            folioDetails.FHSUNITS,
            folioDetails.FHSPRICE,
            folioDetails.FHSAMOUNT,
            folioDetails.FOMTRFNUM,
            folioDetails.FOMTMODEOFHOLDING,
            folioDetails.FHTRFNUM
    FROM
        FCSOPT sopt, (SELECT
        fhs.FHSSOPTRFNUM,
            fomtfhc.FOMTFOLIONO,
            fomtfhc.AMCTDISPLAYNAME,
            fhs.FHSDESCRIPTION,
            fhs.FHSTRADEDATE,
            fhs.FHSTYPE,
            fhs.FHSUNITS,
            fhs.FHSPRICE,
            fhs.FHSAMOUNT,
            fomtfhc.FOMTRFNUM,
            fomtfhc.FOMTMODEOFHOLDING,
            fomtfhc.FHTRFNUM
    FROM
        FCFHS fhs, (SELECT
        fht.FHTRFNUM,
            fomtamc.FOMTFOLIONO,
            fomtamc.FOMTRFNUM,
            fomtamc.AMCTDISPLAYNAME,
            fomtamc.FOMTMODEOFHOLDING
    FROM
        FCFHT fht, (SELECT
        fomtResult.FOMTFOLIONO,
            fomtResult.FOMTRFNUM,
            amct.AMCTDISPLAYNAME,
            fomtResult.FOMTMODEOFHOLDING
    FROM
        FCAMCT amct, (SELECT
        fomt.FOMTFOLIONO,
            fomt.FOMTRFNUM,
            fomt.FOMTAMCTRFNUM,
            fomt.FOMTMODEOFHOLDING
    FROM
        FCFOMT fomt
    WHERE
        fomt.FOMTRFNUM IN (SELECT
                UFTFOMTRFNUM
            FROM
                FCUFT
            WHERE
                UFTUBDRFNUM = ${userPk})) fomtResult
    WHERE
        amct.AMCTRFNUM = fomtResult.FOMTAMCTRFNUM) fomtamc
    WHERE
        fht.FHTFOMTRFNUM = fomtamc.FOMTRFNUM"""

    if(fhtList.nonEmpty){
      val fhtIdQueryFilter = sql""" AND fht.FHTRFNUM IN (#$fhtFormattedId) """
      query = query concat(fhtIdQueryFilter)
    }

    val queryPart3 = sql""") fomtfhc
    WHERE
        fhs.FHSFHTRFNUM = fomtfhc.FHTRFNUM
            AND fhs.FHSTRANSACTIONMODE != ${REVERSAL} AND fhs.FHSTYPE = ${transType}"""
    query = query concat(queryPart3)

    if(transType == DEBIT && startDate.nonEmpty && endDate.nonEmpty){
      val startDateString = DateTimeUtils.convertDateToYYYYMMDD(startDate.get)
      val endDateString = DateTimeUtils.convertDateToYYYYMMDD(endDate.get)
      val queryFilter = sql""" AND date(fhs.FHSTRADEDATE) >= ${startDateString} AND date(fhs.FHSTRADEDATE) <= ${endDateString} """
      query = query concat(queryFilter)
    }
            val queryPart2 = sql""" ORDER BY fhs.FHSSOPTRFNUM , fomtfhc.FOMTRFNUM , fomtfhc.FHTRFNUM , fhs.FHSTRADEDATE) folioDetails
    WHERE
        sopt.SOPTRFNUM = folioDetails.FHSSOPTRFNUM) soptFolioDetails
    WHERE
        soptFolioDetails.SOPTSMTRFNUM = smt.SMTRFNUM) smtFolioDetails,
    (SELECT
        ch.CTMTRFNUM AS childId,
            pr.CTMTRFNUM AS parentId,
            CASE
                WHEN ch.CTMTRFNUM = ${debtHybridId} || ch.CTMTRFNUM = ${equityHybridId} THEN ch.CTMTNAME
                ELSE pr.CTMTNAME
            END AS catName
    FROM
        FCCTMT pr, FCCTMT ch
    WHERE
        pr.CTMTRFNUM = ch.CTMTCTMTRFNUM) ctmtPr
WHERE
    ctmtPr.childId = smtFolioDetails.SMTCTMTRFNUM
              ORDER BY smtFolioDetails.FHSSOPTRFNUM , smtFolioDetails.FOMTRFNUM , smtFolioDetails.FHSTRADEDATE"""

    query = query concat(queryPart2)


    getFundDetailsResult(query)

  }

  def getAssetAllocationFundDetails(userPk: Long): Future[ListBuffer[FundDetails]] = {
    val debtHybridId = DEBT_HYBRID
    val equityHybridId = EQUITY_HYBRID
    val equityId = EQUITY_ID
    val hybridId = HYBRID_ID
    val query =
      sql"""SELECT
    smtFolioDetails.FHSSOPTRFNUM,
    smtFolioDetails.FOMTFOLIONO,
    smtFolioDetails.SOPTLEGALNAME,
    smtFolioDetails.SOPTSCHEMEPLAN,
    smtFolioDetails.AMCTDISPLAYNAME,
    ctmtPr.catName,
    ctmtPr.parentId,
    smtFolioDetails.FHSDESCRIPTION,
    smtFolioDetails.FHSTRADEDATE,
    smtFolioDetails.FHSTYPE,
    smtFolioDetails.FHSUNITS,
    smtFolioDetails.FHSPRICE,
    smtFolioDetails.FHSAMOUNT,
    smtFolioDetails.FOMTRFNUM,
    smtFolioDetails.FOMTMODEOFHOLDING,
    smtFolioDetails.SMTDISPLAYNAME,
    smtFolioDetails.SOPTDIVIDENDFRQN,
    smtFolioDetails.SOPTDIVIOPTIONTYPE,
    smtFolioDetails.FHTRFNUM
FROM
    (SELECT
        soptFolioDetails.FHSSOPTRFNUM,
            smt.SMTCTMTRFNUM,
            smt.SMTDISPLAYNAME,
            soptFolioDetails.FOMTFOLIONO,
            soptFolioDetails.SOPTLEGALNAME,
            soptFolioDetails.SOPTSCHEMEPLAN,
            soptFolioDetails.SOPTDIVIDENDFRQN,
            soptFolioDetails.SOPTDIVIOPTIONTYPE,
            soptFolioDetails.AMCTDISPLAYNAME,
            soptFolioDetails.FHSDESCRIPTION,
            soptFolioDetails.FHSTRADEDATE,
            soptFolioDetails.FHSTYPE,
            soptFolioDetails.FHSUNITS,
            soptFolioDetails.FHSPRICE,
            soptFolioDetails.FHSAMOUNT,
            soptFolioDetails.FOMTRFNUM,
            soptFolioDetails.FOMTMODEOFHOLDING,
            soptFolioDetails.FHTRFNUM
    FROM
        FCSMT smt, (SELECT
        sopt.SOPTSMTRFNUM,
            folioDetails.FHSSOPTRFNUM,
            folioDetails.FOMTFOLIONO,
            sopt.SOPTLEGALNAME,
            sopt.SOPTSCHEMEPLAN,
            sopt.SOPTDIVIDENDFRQN,
            sopt.SOPTDIVIOPTIONTYPE,
            folioDetails.AMCTDISPLAYNAME,
            folioDetails.FHSDESCRIPTION,
            folioDetails.FHSTRADEDATE,
            folioDetails.FHSTYPE,
            folioDetails.FHSUNITS,
            folioDetails.FHSPRICE,
            folioDetails.FHSAMOUNT,
            folioDetails.FOMTRFNUM,
            folioDetails.FOMTMODEOFHOLDING,
            folioDetails.FHTRFNUM
    FROM
        FCSOPT sopt, (SELECT
        fhs.FHSSOPTRFNUM,
            fomtfhc.FOMTFOLIONO,
            fomtfhc.AMCTDISPLAYNAME,
            fhs.FHSDESCRIPTION,
            fhs.FHSTRADEDATE,
            fhs.FHSTYPE,
            fhs.FHSUNITS,
            fhs.FHSPRICE,
            fhs.FHSAMOUNT,
            fomtfhc.FOMTRFNUM,
            fomtfhc.FOMTMODEOFHOLDING,
            fomtfhc.FHTRFNUM
    FROM
        FCFHS fhs, (SELECT
        fht.FHTRFNUM,
            fomtamc.FOMTFOLIONO,
            fomtamc.FOMTRFNUM,
            fomtamc.AMCTDISPLAYNAME,
            fomtamc.FOMTMODEOFHOLDING
    FROM
        FCFHT fht, (SELECT
        fomtResult.FOMTFOLIONO,
            fomtResult.FOMTRFNUM,
            amct.AMCTDISPLAYNAME,
            fomtResult.FOMTMODEOFHOLDING
    FROM
        FCAMCT amct, (SELECT
        fomt.FOMTFOLIONO,
            fomt.FOMTRFNUM,
            fomt.FOMTAMCTRFNUM,
            fomt.FOMTMODEOFHOLDING
    FROM
        FCFOMT fomt
    WHERE
        fomt.FOMTRFNUM IN (SELECT
                UFTFOMTRFNUM
            FROM
                FCUFT
            WHERE
                UFTUBDRFNUM = ${userPk})) fomtResult
    WHERE
        amct.AMCTRFNUM = fomtResult.FOMTAMCTRFNUM) fomtamc
    WHERE
        fht.FHTFOMTRFNUM = fomtamc.FOMTRFNUM
         AND fht.FHTHOLDINGUNITS > 0) fomtfhc
    WHERE
        fhs.FHSFHTRFNUM = fomtfhc.FHTRFNUM
    ORDER BY fhs.FHSSOPTRFNUM , fomtfhc.FOMTRFNUM, fhs.FHSTRADEDATE) folioDetails
    WHERE
        sopt.SOPTRFNUM = folioDetails.FHSSOPTRFNUM) soptFolioDetails
    WHERE
        soptFolioDetails.SOPTSMTRFNUM = smt.SMTRFNUM) smtFolioDetails,
    (SELECT
        ch.CTMTRFNUM AS childId,
            CASE
                WHEN ch.CTMTRFNUM = ${debtHybridId} THEN ${hybridId}
                WHEN ch.CTMTRFNUM = ${equityHybridId} THEN ${equityId}
                ELSE pr.CTMTRFNUM
            END AS parentId,
            CASE
                WHEN ch.CTMTRFNUM = ${debtHybridId} THEN 'Debt'
                WHEN ch.CTMTRFNUM = ${equityHybridId} THEN 'Equity'
                ELSE pr.CTMTNAME
            END AS catName
    FROM
        FCCTMT pr, FCCTMT ch
    WHERE
        pr.CTMTRFNUM = ch.CTMTCTMTRFNUM) ctmtPr
WHERE
    ctmtPr.childId = smtFolioDetails.SMTCTMTRFNUM
        ORDER BY smtFolioDetails.FHSSOPTRFNUM , smtFolioDetails.FOMTRFNUM , smtFolioDetails.FHSTRADEDATE"""


    getFundDetailsResult(query)
  }

  def getIRRFundDetails(userPk: Long): Future[ListBuffer[Transaction]] = {

    val fundMap = HashMap.empty[Long, HashMap[Long, Boolean]]
    val transactionsList: ListBuffer[Transaction] = ListBuffer[Transaction]()

    val query = sql"""SELECT
    folioSopt.FHSSOPTRFNUM,
    folioSopt.FOMTFOLIONO,
    folioSopt.SOPTLEGALNAME,
    folioSopt.FHSDESCRIPTION,
    folioSopt.FHSTRADEDATE,
    folioSopt.FHSTYPE,
    folioSopt.FHSUNITS,
    folioSopt.FHSPRICE,
    folioSopt.FHSAMOUNT,
    folioSopt.FOMTRFNUM,
    smt.SMTDISPLAYNAME,
    folioSopt.SOPTSCHEMEPLAN,
    folioSopt.SOPTDIVIDENDFRQN,
    folioSopt.SOPTDIVIOPTIONTYPE
FROM
    FCSMT smt,
    (SELECT
        folioDetails.FHSSOPTRFNUM,
            folioDetails.FOMTFOLIONO,
            sopt.SOPTLEGALNAME,
            sopt.SOPTSCHEMEPLAN,
            sopt.SOPTDIVIDENDFRQN,
            sopt.SOPTDIVIOPTIONTYPE,
            sopt.SOPTSMTRFNUM,
            folioDetails.FHSDESCRIPTION,
            folioDetails.FHSTRADEDATE,
            folioDetails.FHSTYPE,
            folioDetails.FHSUNITS,
            folioDetails.FHSPRICE,
            folioDetails.FHSAMOUNT,
            folioDetails.FOMTRFNUM
    FROM
        FCSOPT sopt, (SELECT
        fhsData.FHSSOPTRFNUM,
            fhsData.FOMTFOLIONO,
            fhsData.AMCTDISPLAYNAME,
            fhsData.FHSDESCRIPTION,
            fhsData.FHSTRADEDATE,
            fhsData.FHSTYPE,
            fhsData.FHSUNITS,
            fhsData.FHSPRICE,
            fhsData.FHSAMOUNT,
            fhsData.FOMTRFNUM,
            fhsData.FOMTMODEOFHOLDING
    FROM
        (SELECT
        SUM(FHTHOLDINGUNITS) folioHoldingUnits,
            FHTFOMTRFNUM,
            FHTSOPTRFNUM
    FROM
        FCFHT
    GROUP BY FHTFOMTRFNUM , FHTSOPTRFNUM) folioTotalUnitsResult, (SELECT
        fhs.FHSSOPTRFNUM,
            fomtfhc.FOMTFOLIONO,
            fomtfhc.AMCTDISPLAYNAME,
            fhs.FHSDESCRIPTION,
            fhs.FHSTRADEDATE,
            fhs.FHSTYPE,
            fhs.FHSUNITS,
            fhs.FHSPRICE,
            fhs.FHSAMOUNT,
            fomtfhc.FOMTRFNUM,
            fomtfhc.FOMTMODEOFHOLDING
    FROM
        FCFHS fhs, (SELECT
        fht.FHTRFNUM,
            fomtamc.FOMTFOLIONO,
            fomtamc.FOMTRFNUM,
            fomtamc.AMCTDISPLAYNAME,
            fomtamc.FOMTMODEOFHOLDING
    FROM
        FCFHT fht, (SELECT
        fomtResult.FOMTFOLIONO,
            fomtResult.FOMTRFNUM,
            amct.AMCTDISPLAYNAME,
            fomtResult.FOMTMODEOFHOLDING
    FROM
        FCAMCT amct, (SELECT
        fomt.FOMTFOLIONO,
            fomt.FOMTRFNUM,
            fomt.FOMTAMCTRFNUM,
            fomt.FOMTMODEOFHOLDING
    FROM
        FCFOMT fomt
    WHERE
        fomt.FOMTRFNUM IN (SELECT
                UFTFOMTRFNUM
            FROM
                FCUFT
            WHERE
                UFTUBDRFNUM = ${userPk})) fomtResult
    WHERE
        amct.AMCTRFNUM = fomtResult.FOMTAMCTRFNUM) fomtamc
    WHERE
        fht.FHTFOMTRFNUM = fomtamc.FOMTRFNUM
            AND fht.FHTSTATUS != ${UNITS_REVERSED}) fomtfhc
    WHERE
        fhs.FHSFHTRFNUM = fomtfhc.FHTRFNUM) fhsData
    WHERE
        folioTotalUnitsResult.FHTFOMTRFNUM = fhsData.FOMTRFNUM
            AND folioTotalUnitsResult.folioHoldingUnits > 0
            AND folioTotalUnitsResult.FHTSOPTRFNUM = fhsData.FHSSOPTRFNUM
    ORDER BY fhsData.FHSSOPTRFNUM , fhsData.FOMTRFNUM , fhsData.FHSTRADEDATE) folioDetails
    WHERE
        sopt.SOPTRFNUM = folioDetails.FHSSOPTRFNUM) folioSopt
WHERE
    folioSopt.SOPTSMTRFNUM = smt.SMTRFNUM
ORDER BY folioSopt.FHSSOPTRFNUM , folioSopt.FOMTRFNUM , folioSopt.FHSTRADEDATE""".as[(Long, String, String, String, Date, String, BigDecimal, BigDecimal, BigDecimal, Long, String, String, String, String)]

    db.run(query).map(values => {
      values.foreach(value => {
        val fundId = value._1
        if (fundMap.contains(fundId) && fundMap.get(fundId).get.contains(value._10)) {
          val transactionDetailsList = transactionsList(transactionsList.size - 1).transactionList
          val transDate = new util.Date(value._5.getTime)
          transactionDetailsList.+=(TransactionDetails(transDate, value._4, value._9.toDouble, value._8.toDouble, value._7.toDouble, BuySellEnum.withName(value._6)))
          transactionsList(transactionsList.size - 1) = transactionsList(transactionsList.size - 1).copy(transactionList = transactionDetailsList)
        } else {
          val folioMap = mutable.HashMap.empty[Long, Boolean]
          folioMap.+=(value._10 -> true)
          fundMap.+=(fundId -> folioMap)
          val transactionDetailsList: ListBuffer[TransactionDetails] = ListBuffer[TransactionDetails]()
          val transDate = new util.Date(value._5.getTime)
          val schemePlan = schemeHelper.getSchemePlan(value._12)
          val divFreq = schemeHelper.getDivFrequency(value._13)
          val dividendOption = schemeHelper.getDivOption(value._14)
          transactionDetailsList.+=(TransactionDetails(transDate, value._4, value._9.toDouble, value._8.toDouble, value._7.toDouble, BuySellEnum.withName(value._6)))
          val transaction = Transaction(value._2, fundId, value._11, schemePlan, divFreq, dividendOption, transactionDetailsList)
          transactionsList.+=(transaction)
        }
      })
      transactionsList
    })
  }

  def getFolioSchemePayoutTxns(folioId: Long, soptrfnum: Long): Future[List[FcfptRow]] = {
    val query = Fcfpt.filter(x => x.fptfomtrfnum === folioId && x.fptsoptrfnum === soptrfnum).sortBy(_.fpttype desc).result

    db.run(query).map(payoutSeq =>{
      payoutSeq.toList
    })
  }

  def getUserTransactions(userPk: Long): Future[List[TransactionSummary]] = {

    implicit val getTransactionSummary = GetResult(r => TransactionSummary(r.nextLong, r.nextLong, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextLong, r.nextDouble, r.nextString, false, false, r.nextString, r.nextDoubleOption));

    val query = sql"""SELECT
          od.OMTRFNUM AS orderId,
          od.SOTRFNUM AS subOrderId,
          fnd.SMTDISPLAYNAME AS fundName,
          fnd.SOPTSCHEMEPLAN AS schemePlan,
          fnd.SOPTDIVIDENDFRQN AS dividendFreq,
          fnd.SOPTDIVIOPTIONTYPE AS dividendOption,
          od.CREATEDATE AS subOrderDate,
          ostm.OSTMDISPLAYNAME AS subOrderStatus,
          ostm.OSTMRFNUM AS subOrderState,
          od.SOTORDERAMOUNT AS subOrderAmount,
          CASE
              WHEN od.SOTINVESTMENTMODE = 'S' THEN 'SIP'
              ELSE 'LUMPSUM'
          END AS subOrderInvestmentMode,
          CASE
              WHEN od.OMTBUYSELL = 'P' THEN 'Purchase'
              ELSE 'Redemption'
          END AS orderType,
          od.SOTORDERQUANTITY AS subOrderQuantity
      FROM
          FCOSTM ostm,
          (SELECT
              omt.OMTRFNUM,
                  omt.OMTBUYSELL,
                  sot.SOTRFNUM,
                  sot.SOTSOPTRFNUM,
                  sot.SOTOSTMSTATERFNUM,
                  sot.CREATEDATE,
                  sot.SOTORDERAMOUNT,
                  sot.SOTORDERQUANTITY,
                  sot.SOTINVESTMENTMODE,
                  sot.SOTSIPINSTALLMENTS
          FROM
              FCOMT omt, FCSOT sot
          WHERE
              omt.OMTUBDRFNUM = ${userPk}
                  AND sot.SOTOMTRFNUM = omt.OMTRFNUM) AS od,
          (SELECT
              sopt.SOPTRFNUM,
                  smt.SMTDISPLAYNAME,
                  sopt.SOPTSCHEMEPLAN,
                  sopt.SOPTDIVIOPTIONTYPE,
                  sopt.SOPTDIVIDENDFRQN
          FROM
              FCSOPT sopt, FCSMT smt
          WHERE
              smt.SMTRFNUM = sopt.SOPTSMTRFNUM) AS fnd
      WHERE
          od.SOTSOPTRFNUM = fnd.SOPTRFNUM
              AND od.SOTOSTMSTATERFNUM = ostm.OSTMRFNUM
      ORDER BY od.CREATEDATE DESC , od.SOTRFNUM DESC""".as[TransactionSummary];
    db.run(query).map(data => data.toList);
  }

  def getUserSchemeTransactions(userPk: Long, sotrfnumList: List[Long]): Future[List[TransactionSummary]] = {

    implicit val getTransactionSummary = GetResult(r => TransactionSummary(r.nextLong, r.nextLong, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextLong, r.nextDouble, r.nextString, false, false, r.nextString, r.nextDoubleOption));

    val sotIdQueryList = sotrfnumList.mkString(",")

    val query = sql"""SELECT
    od.OMTRFNUM AS orderId,
    od.SOTRFNUM AS subOrderId,
    fnd.SMTDISPLAYNAME AS fundName,
    fnd.SOPTSCHEMEPLAN AS schemePlan,
    fnd.SOPTDIVIDENDFRQN AS dividendFreq,
    fnd.SOPTDIVIOPTIONTYPE AS dividendOption,
    od.CREATEDATE AS subOrderDate,
    ostm.OSTMDISPLAYNAME AS subOrderStatus,
    ostm.OSTMRFNUM AS subOrderState,
    od.SOTORDERAMOUNT AS subOrderAmount,
    CASE
        WHEN od.SOTINVESTMENTMODE = 'S' THEN 'SIP'
        ELSE 'LUMPSUM'
    END AS subOrderInvestmentMode,
    CASE
        WHEN od.OMTBUYSELL = 'P' THEN 'Purchase'
        ELSE 'Redemption'
    END AS orderType,
    od.SOTORDERQUANTITY AS subOrderQuantity
FROM
    FCOSTM ostm,
    (SELECT
        omt.OMTRFNUM,
            omt.OMTBUYSELL,
            sot.SOTRFNUM,
            sot.SOTSOPTRFNUM,
            sot.SOTOSTMSTATERFNUM,
            sot.CREATEDATE,
            sot.SOTORDERAMOUNT,
            sot.SOTORDERQUANTITY,
            sot.SOTINVESTMENTMODE,
            sot.SOTSIPINSTALLMENTS
    FROM
        FCOMT omt, FCSOT sot
    WHERE
        omt.OMTUBDRFNUM = ${userPk}
            AND sot.SOTOMTRFNUM = omt.OMTRFNUM
            AND sot.SOTRFNUM IN (#$sotIdQueryList)) AS od,
    (SELECT
        sopt.SOPTRFNUM,
            smt.SMTDISPLAYNAME,
            sopt.SOPTSCHEMEPLAN,
            sopt.SOPTDIVIOPTIONTYPE,
            sopt.SOPTDIVIDENDFRQN
    FROM
        FCSOPT sopt, FCSMT smt
    WHERE
        smt.SMTRFNUM = sopt.SOPTSMTRFNUM) AS fnd
WHERE
    od.SOTSOPTRFNUM = fnd.SOPTRFNUM
        AND od.SOTOSTMSTATERFNUM = ostm.OSTMRFNUM
ORDER BY od.CREATEDATE DESC , od.SOTRFNUM DESC""".as[TransactionSummary];

    db.run(query).map(data => {
      data.toList
    });

  }

  def getFhtRowByFolioNoAndSoptrfnum(folioNo: String, soptrfnum: Long): Future[Option[FcfhtRow]] = {
    val query = for {
      fomtrow <- Fcfomt.filter(x => x.fomtfoliono === folioNo)
      fhtrow <- Fcfht.filter(x => x.fhtsoptrfnum === soptrfnum)
    } yield (fhtrow)

    db.run(query.result).map(value => {
      value.headOption
    })
  }

}


object SlickKit {

  implicit class SQLActionBuilderConcat(a: SQLActionBuilder) {
    def concat(b: SQLActionBuilder): SQLActionBuilder = {
      SQLActionBuilder(a.queryParts ++ b.queryParts, new SetParameter[Unit] {
        def apply(p: Unit, pp: PositionedParameters): Unit = {
          a.unitPConv.apply(p, pp)
          b.unitPConv.apply(p, pp)
        }
      })
    }
  }

}