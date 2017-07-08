package service

import java.sql.Date
import javax.inject.Inject

import constants.{DBConstants, InvestmentConstants, ProductConstants}
import data.model.Tables.{FcamctRow, FcctmtRow, FcdrdRow, FcdsdRow, FcramtRow, FcsmtRow, FcsoelRow, FcsoptRow}
import helpers.SchemeHelper
import models.{FundDoc, Product, ProductOption}
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsNull, Json}
import repository.module.{ProductRepository, SchemeRepository}
import utils.DBConstantsUtil

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fincash on 02-02-2017.
  */
class ProductService @Inject()(productRepository: ProductRepository, schemeRepository: SchemeRepository,
                               solrFundSearchService: SolrFundSearchService, schemeHelper: SchemeHelper)
  extends ProductConstants with DBConstants with InvestmentConstants {

  val logger, log = LoggerFactory.getLogger(classOf[ProductService])


  def getQuickSipFunds(): Future[Option[List[FundDoc]]] = {
    getProductFunds(SMART_SIP)
  }

  def getTaxSaverFunds(): Future[Option[List[FundDoc]]] = {
    getProductFunds(TAX_SAVER)
  }

  def getSavingsPlusFunds(): Future[Option[List[FundDoc]]] = {
    getProductFunds(SAVINGS_PLUS)
  }

  def getProductFunds(productName: String): Future[Option[List[FundDoc]]] = {
    val product = productRepository.getProductByName(productName)

    product.flatMap(product => {
      productRepository.getProductStateObjectList(product.id).flatMap(productStateObjectList => {
        val futures = for (productStateObject <- productStateObjectList) yield {
          val soptRfnumArray = productStateObject.soptRfnumArray.split(',')
          val soptIsDefaultArray = productStateObject.soptIsDefaultArray.split(',')
          var soptWeightageArray = Array[String]()
          var soptInvestmentModeArray = Array[String]()

          if (productStateObject.soptIsWeightage.isDefined) {
            soptWeightageArray = productStateObject.soptIsWeightage.get.split(',')
          }

          if (productStateObject.soptInvestmentModeArray.isDefined) {
            soptInvestmentModeArray = productStateObject.soptInvestmentModeArray.get.split(',')
          }

          val productOptions = ListBuffer[ProductOption]()
          soptWeightageArray.foreach(data => logger.debug(data))
          soptInvestmentModeArray.foreach(data => logger.debug(data))

          soptRfnumArray.zipWithIndex.foreach((data) => {
            val index = data._2
            val soptRfnum = data._1.toLong
            val isDefault = soptIsDefaultArray(index)
            val weightage = if (soptWeightageArray.length > 0) Some(soptWeightageArray(index).toDouble) else None
            val investmentMode = if (soptInvestmentModeArray.length > 0) Some(DBConstantsUtil.getInvestmentModeFullForm(soptInvestmentModeArray(index))) else None;
            productOptions += new ProductOption(soptRfnum = soptRfnum, isDefault = isDefault, weightage = weightage, investmentMode = investmentMode);
          })

          getFundObjBySchemeId(new Product(productStateObject.productRfnum.get, productStateObject.productName.get, productStateObject.smtRfnum.get, productOptions.toList))
        }
        Future.sequence(futures).map(values =>{
          val fundsList = ListBuffer[FundDoc]()
          values.foreach(fund =>{
            if(fund.nonEmpty){
              fundsList.+=(fund.get)
            }
          })
          if(fundsList.size == 0){
            None
          } else{
            Some(fundsList.toList)
          }
        })
      })
    })

  }

  def getFundObjBySchemeId(product: Product): Future[Option[FundDoc]] = {
    val queries = for {
      smtObj <- schemeRepository.getSchemeById(product.smtRfnum)
      soptObj <- schemeRepository.getDefaultSchemeById(product.smtRfnum)
      scatObj <- schemeRepository.getSchemeCategoryById(smtObj.smtctmtrfnum)
      catObj <- schemeRepository.getSchemeCategoryById(scatObj.ctmtctmtrfnum.get)
      amcObj <- schemeRepository.getSchemeAMCById(smtObj.smtamctrfnum)
      dsdObj <- schemeRepository.getDailySchemeData(soptObj.id)
      drdObjList <- schemeRepository.getDailyReturnData(soptObj.id)
      ratioObj <- schemeRepository.getLatestRatioData(soptObj.id)
      sipAmount <- schemeRepository.getMinSIPAmount(soptObj.id)
      exitLoadObj <- schemeRepository.getExitLoadData(soptObj.id)
    } yield (smtObj, soptObj, scatObj, catObj, amcObj, dsdObj, drdObjList, ratioObj, sipAmount, exitLoadObj)

    queries.map(values => {
        Some(getFundObj(values._1, values._2, values._3, values._4, values._5, values._6, values._7, values._8, values._9, values._10, product))
    })
  }

  def getFundObj(smtObj: FcsmtRow, soptObj: FcsoptRow, scatObj: FcctmtRow, catObj: FcctmtRow, amcObj: FcamctRow, dsdObj: Option[FcdsdRow],
                 drdObjList: Seq[FcdrdRow], ratioObj: Option[FcramtRow], sipAmount: Option[Int], soelObj: Option[FcsoelRow], productObj: Product): FundDoc = {

    var sipAllowed = Some(false)
    if (sipAmount.isDefined) {
      sipAllowed = Some(true)
    }

    var nav:Option[Float] = None
    var navChange:Option[Double] = None
    var navPercentChange:Option[Double] = None
    var navAsOfDate:Option[String] = None
    var aumincr:Option[Double] = None
    var asOfDate:Option[String] = None
    var expense:Option[Float] = None
    var alpha:Option[Float] = None
    var sharpe:Option[Float] = None
    var information:Option[Float] = None
    var yieldToMaturity:Option[Double] = None
    var maturity:Option[Float] = None
    var modifiedDuration:Option[Double] = None
    var exitLoad: Option[String] = Some("NIL")
    if(dsdObj.nonEmpty){
      nav = Some(dsdObj.get.dsdnav)
      navChange = dsdObj.get.dsdnavchange
      navPercentChange = dsdObj.get.dsdnavpercentchange
      navAsOfDate = Some(dsdObj.get.dsdnavasofdate.getOrElse("").toString)
    }
    if(ratioObj.nonEmpty){
      aumincr = Some(ratioObj.get.ramtaumincr)
      asOfDate = Some(ratioObj.get.ramtasofdate.getOrElse("").toString)
      expense = ratioObj.get.ramtexpense
      alpha = ratioObj.get.ramtalpha
      sharpe = ratioObj.get.ramtsharpe
      information = ratioObj.get.ramtinformation
      yieldToMaturity = ratioObj.get.ramtyieldtomaturity
      maturity = ratioObj.get.ramtmaturity
      modifiedDuration = ratioObj.get.ramtmodifiedduration
    }
    if(soelObj.nonEmpty){
      if (soelObj.get.soelvalue > 0 && soelObj.get.soelhighbreakpoint.nonEmpty){
        var exitValue = soelObj.get.soelvalue
        if (soelObj.get.soelvalue - soelObj.get.soelvalue.toInt == 0.0){
          exitValue = soelObj.get.soelvalue.toInt
        }
        if (soelObj.get.soelbreakpointunit == "D"){
          exitLoad = Some(soelObj.get.soellowbreakpoint + "-" + soelObj.get.soelhighbreakpoint.get + " Days" + " (" + exitValue + "%), " + soelObj.get.soelhighbreakpoint.get + " Days and above (NIL)")
        } else if (soelObj.get.soelbreakpointunit == "M"){
          exitLoad = Some(soelObj.get.soellowbreakpoint + "-" + soelObj.get.soelhighbreakpoint.get + " Month" + " (" + exitValue + "%), " + soelObj.get.soelhighbreakpoint.get + " Month and above (NIL)")
        } else if (soelObj.get.soelbreakpointunit == "Y") {
          exitLoad = Some(soelObj.get.soellowbreakpoint + "-" + soelObj.get.soelhighbreakpoint.get + " Year" + " (" + exitValue + "%), " + soelObj.get.soelhighbreakpoint.get + " Year and above (NIL)")
        }
      }
      else {
        exitLoad = Some("NIL")
      }
    }
    var age = 0.0
    if(!soptObj.soptinceptiondate.isEmpty){
      age = schemeHelper.getAge(soptObj.soptinceptiondate.get)
    }
    var fundDoc = new FundDoc(smtObj.id, smtObj.smtdisplayname,age , 5, aumincr.getOrElse(0), amcObj.id, amcObj.amctbrandname.get,
      catObj.id, catObj.ctmtname, scatObj.id, scatObj.ctmtname, smtObj.smtsebirisk.get, selected = Some(true), productObj = Some(productObj), sipAllowed = sipAllowed);

    var fundBasicFactSheet = Json.obj(
      "id" -> smtObj.id,
      "name" -> smtObj.smtdisplayname,
      "isin" -> smtObj.smtisin,
      "description" -> smtObj.smtfunddescription,
      "nav" -> nav,
      "navValueChange" -> navChange,
      "navPercentChange" -> navPercentChange,
      "navAsOfDate" -> navAsOfDate,
      "inceptionDate" -> soptObj.soptinceptiondate,
      "category" -> catObj.ctmtname,
      "subCategory" -> scatObj.ctmtname,
      "fundType" -> soptObj.soptstructure,
      "aum" -> aumincr,
      "aumAsOfDate" -> asOfDate,
      "expenseRatio" -> expense,
      "fincashRating" -> "5",
      "exitLoad" -> exitLoad,
      "benchmarkName" -> "TBD",
      "minLumpsum" -> smtObj.smtminlumpsum,
      "minSIPAmt" -> sipAmount.getOrElse("0").toString,
      "alphaRatio" -> alpha,
      "sharpeRatio" -> sharpe,
      "infoRatio" -> information,
      "ratioAsOfDate" -> asOfDate,
      "riskometer" -> smtObj.smtsebirisk,
      "debtYield" -> yieldToMaturity,
      "maturity" -> maturity,
      "duration" -> modifiedDuration,
      "amc" -> amcObj.amctbrandname,
      "amcId" -> amcObj.id
    )

    drdObjList.foreach(returnData => {
      if (returnData.drdreturnperiod == 1 && returnData.drdreturnunit == "M") {
        fundBasicFactSheet = fundBasicFactSheet.+("ret1mn", Json.parse(returnData.drdreturnvalue.toString))
      } else if (returnData.drdreturnperiod == 3 && returnData.drdreturnunit == "M") {
        fundBasicFactSheet = fundBasicFactSheet.+("ret3mn", Json.parse(returnData.drdreturnvalue.toString))
      } else if (returnData.drdreturnperiod == 6 && returnData.drdreturnunit == "M") {
        fundBasicFactSheet = fundBasicFactSheet.+("ret6mn", Json.parse(returnData.drdreturnvalue.toString))
      } else if (returnData.drdreturnperiod == 1 && returnData.drdreturnunit == "Y") {
        fundBasicFactSheet = fundBasicFactSheet.+("ret1yr", Json.parse(returnData.drdreturnvalue.toString))
      } else if (returnData.drdreturnperiod == 3 && returnData.drdreturnunit == "Y") {
        fundBasicFactSheet = fundBasicFactSheet.+("ret3yr", Json.parse(returnData.drdreturnvalue.toString))
      } else if (returnData.drdreturnperiod == 5 && returnData.drdreturnunit == "Y") {
        fundBasicFactSheet = fundBasicFactSheet.+("ret5yr", Json.parse(returnData.drdreturnvalue.toString))
      } else if (returnData.drdreturnperiod == 0 && returnData.drdreturnunit == "I") {
        fundBasicFactSheet = fundBasicFactSheet.+("retSinceInception", Json.parse(returnData.drdreturnvalue.toString))
      }

      fundBasicFactSheet = fundBasicFactSheet.+("retAsOfDate", Json.toJson(returnData.drdreturnasofdate.get.toString))
    })


    fundDoc = fundDoc.copy(basicFactsheet = Some(Json.toJson(fundBasicFactSheet)))
    fundDoc
  }


}
