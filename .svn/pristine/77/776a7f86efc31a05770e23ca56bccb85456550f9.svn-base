package models

import play.api.libs.json.JsValue
import reactivemongo.bson.{BSONDocument, BSONDocumentReader}

import scala.collection.immutable.Map
import scala.collection.mutable.ListBuffer


case class FundBasicFactsheet(id: Long, name: String, isin: Option[String] = None, description: Option[String] = None, nav: Option[String] = None, navValueChange: Option[String] = None,
                              navPercentChange: Option[String] = None, navAsOfDate: Option[String] = None, inceptionDate: Option[String] = None,
                              category: Option[String] = None, subCategory: Option[String] = None, fundType: Option[String] = None, aum: Option[String] = None,
                              aumAsOfDate: Option[String] = None, expenseRatio: Option[String] = None, fincashRating: Option[String] = None,
                              exitLoad: Option[String] = None, benchmarkName: Option[String] = None, minLumpsum: Option[String] = None,
                              minSIPAmt: Option[String] = None, alphaRatio: Option[String] = None, sharpeRatio: Option[String] = None,
                              infoRatio: Option[String] = None, ratioAsOfDate: Option[String] = None, retSinceInception: Option[String] = None,
                              ret1yr: Option[String] = None, ret3yr: Option[String] = None, ret5yr: Option[String] = None,
                              ret1mn: Option[String] = None, ret3mn: Option[String] = None, ret6mn: Option[String] = None, retAsOfDate: Option[String] = None,
                              riskometer: Option[String] = None, debtYield: Option[String] = None, maturity: Option[String] = None, duration: Option[String] = None,
                              amc: Option[String] = None, amcId: Option[String] = None);

case class FundOption(soptRfnum: Long,
                      legalName: String,
                      schemePlan: String,
                      dividendFrequency: String,
                      dividendOption: String,
                      sipAllowed: Boolean,
                      minLumpsum: Double,
                      isDefault: Boolean,
                      aipData: Option[List[AIPData]]
                     )

case class AIPData(soptRfnum: Long,
                   frequency: String,
                   dates: String,
                   minAmount: Long,
                   maxAmount: Long,
                   minNoOfInstallment: Long,
                   maxNoOfInstallment: Long,
                   multiplier: Long
                  )

case class FundVsBenchmark(asOfDate: String, dataPoints: List[String], fundPerformance: List[String], benchmarkPerformance: List[String])

case class StyleCoefficients(assetClass: ListBuffer[String], coefficients: ListBuffer[BigDecimal])

case class MeanReturn(style: BigDecimal, selection: BigDecimal, total: BigDecimal)

case class LiquidityPortfolio(days: ListBuffer[Double], value: ListBuffer[Double])

case class FundDataMap(asOfDate: String, dataMap: Map[String, String])

case class Holdings(name: String, creditRating: String, value: String, share: String)

case class FundHoldings(asOfDate: String, holdings: List[Holdings])

case class EquityFundAdvancedFactsheet(sinceInceptionCmp: FundVsBenchmark, annualReturns: FundVsBenchmark,
                                       quarterlyReturns: FundVsBenchmark, peerComparison: List[FundDoc], topHoldings: FundDataMap,
                                       topSectors: FundDataMap, assetAllocation: FundDataMap, sipPerformance: FundVsBenchmark,
                                       styleCoefficients: StyleCoefficients, meanReturn: MeanReturn, liquidity: LiquidityPortfolio)

case class DebtFundAdvancedFactsheet(sinceInceptionCmp: FundVsBenchmark, annualReturns: FundVsBenchmark,
                                     monthlyReturns: FundVsBenchmark, quarterlyReturns: FundVsBenchmark, peerComparison: List[FundDoc],
                                     topHoldings: FundHoldings, fundHoldings: FundDataMap)

case class FundDoc(id: Long, name: String, age: Double, rating: Double, aum: Double, amcid: Long,
                   amc: String, cid: Long, category: String, scid: Long, subCategory: String,
                   risk: String, sipAllowed: Option[Boolean] = Some(false), basicFactsheet: Option[JsValue] = None,
                   selected: Option[Boolean] = Some(false),
                   investmentMode: Option[String] = None, productObj: Option[Product] = None)

case class FundSearchResult(numFound: Long, funds: ListBuffer[FundDoc])

case class FundSearchFacets(ratingFilterData: Option[ListBuffer[FundFilter]],
                            aumFilterData: Option[ListBuffer[FundFilter]], amcFilterData: Option[ListBuffer[FundFilter]],
                            ageFilterData: Option[ListBuffer[FundFilter]], categoryFilterData: Option[ListBuffer[FundFilter]],
                            subCategoryFilterData: Option[ListBuffer[FundFilter]])

case class FundFilter(id: String, name: String, count: Long, selected: Boolean)

case class SearchQuery(am: Option[Seq[String]], q: Option[String], f: Option[Long], r: Option[Long], ra: Option[Seq[Int]],
                       ag: Option[Int], c: Option[Seq[String]], sc: Option[Seq[String]], se: Option[String], so: Option[String],
                       ar: Option[String], o: Option[String])

case class Facet(name: String, count: Long)


object FundsJsonFormats {

  import models.ProductsJsonFormats._
  import play.api.libs.json.Json

  implicit val aIPDataFormat = Json.format[AIPData]
  //implicit val fundBasicFactsheet = Json.format[FundBasicFactsheet]
  implicit val fundOptionFormat = Json.format[FundOption]
  implicit val fundDocFormat = Json.format[FundDoc]
  implicit val fundVsBenchmarkFormat = Json.format[FundVsBenchmark]
  implicit val fundDataMapFormat = Json.format[FundDataMap]
  implicit val holdingsFormat = Json.format[Holdings]
  implicit val fundHoldingsFormat = Json.format[FundHoldings]
  implicit val styleCoefficientsFormat = Json.format[StyleCoefficients]
  implicit val meanReturnFormat = Json.format[MeanReturn]
  implicit val liquidityPortfolioFormat = Json.format[LiquidityPortfolio]
  implicit val equityFundAdvancedFactsheetFormat = Json.format[EquityFundAdvancedFactsheet]
  implicit val debtFundAdvancedFactsheetFormat = Json.format[DebtFundAdvancedFactsheet]
  implicit val fundFilterFormat = Json.format[FundFilter]
  implicit val fundSearchResultFormat = Json.format[FundSearchResult]
  implicit val searchQueryFormat = Json.format[SearchQuery]
  implicit val facetsFormat = Json.format[Facet]
  implicit val fundSearchFacets = Json.format[FundSearchFacets]

}

object FundDoc {

  /*implicit val reads: Reads[FundDoc] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "age").read[Double] and
      (JsPath \ "rat").read[Double] and
      (JsPath \ "aum").read[String] and
      (JsPath \ "amcid").read[Long] and
      (JsPath \ "amc").read[Double] and
      (JsPath \ "cid").read[Long] and
      (JsPath \ "category").read[String] and
      (JsPath \ "scid").read[Long] and
      (JsPath \ "subcategory").read[String] and
      (JsPath \ "riks").read[String]

    )(FundDoc.apply _)*/

  implicit object FundDocReader extends BSONDocumentReader[FundDoc] {
    def read(doc: BSONDocument): FundDoc = {

      val fid = doc.getAs[String]("fid").get.toInt
      val name = doc.getAs[String]("dname").get
      val age = doc.getAs[String]("dage").get.toDouble
      val rating = doc.getAs[String]("drat").get.toInt
      val aum = 0
      val amc = doc.getAs[String]("damc").get
      val cid = doc.getAs[String]("cid").get.toInt
      val category = doc.getAs[String]("dcat").get
      val scid = doc.getAs[String]("scid").get.toInt
      val subCategory = doc.getAs[String]("dscat").get
      val risk = doc.getAs[String]("drisk").get
      val rt1m = doc.getAs[String]("drt1m").get.toDouble
      val rt3m = doc.getAs[String]("drt3m").get.toDouble
      val rt6m = doc.getAs[String]("drt6m").get.toDouble
      val rt1y = doc.getAs[String]("drt1y").get.toDouble
      val rt3y = doc.getAs[String]("drt3y").get.toDouble
      val rt5y = doc.getAs[String]("drt5y").get.toDouble
      val rtsi = doc.getAs[String]("drtsi").get.toDouble

      FundDoc(fid, name, age, rating, aum, 0, amc, cid, category, scid, subCategory, risk);
    }
  }

}
