package controllers

import com.google.inject.Inject
import helpers.AuthenticatedAction
import models.FundsJsonFormats._
import models.{FundSearchFacets, SearchQuery}
import org.slf4j.LoggerFactory
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.SolrFundSearchService


class FundsController @Inject()(solrFundSearchService: SolrFundSearchService, auth: AuthenticatedAction) extends Controller {

  val logger , log = LoggerFactory.getLogger(classOf[FundsController])

  /**
    * This method is used to search funds on selection of amc name or some keyword
    *
    * @return
    */
  def searchFunds() = auth.Action(parse.json) { request =>
    val searchQuery = request.body.as[SearchQuery];
    //val ageFilterData = solrFundSearchService.getAgeList(getFundSearchQuery(searchQuery.copy(age = Some(0), keyword = Some(query.getQuery()))));
    //val ratingFilterData = solrFundSearchService.getRatingList(getFundSearchQuery(searchQuery.copy(rating = Some(Seq.empty), keyword = Some(query.getQuery()))));
    //aum


    val fundSearchResults = solrFundSearchService.fundSearch(searchQuery);
    /*Ok(Json.toJson(fundSearchResults.copy(
      ratingFilterData = Some(solrFundSearchService.getRatingList(searchQuery)),
      ageFilterData = Some(solrFundSearchService.getAgeList(searchQuery)),
        //aumFilterData = Some(solrFundSearchService.get),
        amcFilterData = Some(solrFundSearchService.getAmcList(searchQuery)),
      categoryFilterData = Some(solrFundSearchService.getCategoryList(searchQuery)),
      subCategoryFilterData = Some(solrFundSearchService.getSubCategoryList(searchQuery))
    )));*/

    Ok(Json.toJson(solrFundSearchService.fundSearch(searchQuery)));

  }

  /**
    * This method is used to search funds on selection of amc name or some keyword
    *
    * @return
    */
  def getFacets = auth.Action(parse.json) { request =>
    var searchQuery = request.body.as[SearchQuery];

    val searchKeyword = solrFundSearchService.fundSearchQueryKeyword(searchQuery);
    searchQuery = searchQuery.copy(q = Some(searchKeyword));

    //val fundSearchResults = solrFundSearchService.fundSearch(searchQuery);
    Ok(Json.toJson(new FundSearchFacets(
      ratingFilterData = Some(solrFundSearchService.getRatingList(searchQuery)),
      ageFilterData = Some(solrFundSearchService.getAgeList(searchQuery)),
      aumFilterData = None,
      amcFilterData = Some(solrFundSearchService.getAmcList(searchQuery)),
      categoryFilterData = Some(solrFundSearchService.getCategoryList(searchQuery)),
      subCategoryFilterData = Some(solrFundSearchService.getSubCategoryList(searchQuery))
    )));

    //Ok(Json.toJson(solrFundSearchService.fundSearch(searchQuery)));

  }


  /**
    * This method is used to get autoComplete data
    *
    * @return
    */
  def getAutoCompleteData = auth.Action(parse.json) { request =>
    val queryTerm = request.body.\("term").as[String];
    val funds = solrFundSearchService.autoComplete(queryTerm);
    Ok(Json.toJson(funds));
  }

  /**
    * This method is used to search funds on selection of amc name or some keyword
    *
    * @return
    */
  def getBasicFactSheet = auth.Action(parse.json) { request =>
    val fundId = request.body.\("id").as[Int]

    /* val basicFactsheet = new FundBasicFactsheet(id = fundId,
       name = "Axis Equity Fund",
       description = Some("To achieve long term capital appreciation by investing in a diversified portfolio predominantly consisting of equity and equity related securities including derivatives. However, there can be no assurance that the investment objective of the Scheme will be achieved."),
       nav = Some("20.75"),
       inceptionDate = Some("5th January 2010"),
       category = Some("Equity"),
       fundType = Some("Open-Ended"),
       aum = Some("2166.16"),
       expenseRatio = Some("0.97"),
       fincashRating = Some("4"),
       exitLoad = Some("200"),
       benchmarkName = Some("Nifty 500"),
       minInvAmt = Some("5000"),
       minSIPAmt = Some("500"),
       alphaRatio = Some("0.97"),
       sharpeRatio = Some("0.97"),
       infoRatio = Some("0.97"),
       retSinceInception = Some("12"),
       ret1yr = Some("10"),
       ret3yr = Some("12"),
       ret5yr = Some("15"),
       riskometer = Some("High"));

     Ok(Json.toJson(basicFactsheet));*/

    Ok

  }

  /**
    * This method is used to search funds on selection of amc name or some keyword
    *
    * @return
    */
  def getAdvancedFactSheet = auth.Action(parse.json) { request =>
    val fundId = request.body.\("id").as[Int]
    log.debug(""+fundId)

    if (fundId == 10) {
      /* val basicFactsheet = new FundBasicFactsheet(id = fundId,
         name = "ICICI Pru Dynamic Bond-RP (G)",
         description = Some("To achieve long term capital appreciation by investing in a diversified portfolio predominantly consisting of equity and equity related securities including derivatives. However, there can be no assurance that the investment objective of the Scheme will be achieved."),
         nav = Some("20.75"),
         inceptionDate = Some("5th January 2010"),
         category = Some("Debt"),
         fundType = Some("Open-Ended"),
         aum = Some("2166.16"),
         expenseRatio = Some("0.97"),
         fincashRating = Some("4"),
         exitLoad = Some("200"),
         benchmarkName = Some("Nifty 500"),
         minInvAmt = Some("5000"),
         minSIPAmt = Some("500"),
         alphaRatio = Some("0.97"),
         sharpeRatio = Some("0.97"),
         infoRatio = Some("0.97"),
         retSinceInception = Some("12"),
         ret1yr = Some("10"),
         ret3yr = Some("12"),
         ret5yr = Some("15"),
         riskometer = Some("High"));

       //asOfDate: String, dataPoints: List[String], fund: List[String], benchmark: List[String]

       val sinceInception = new FundVsBenchmark(
         "28/10/2016",
         List.apply("1 year", "3 year", "5 year", "Since Inception"),
         List.apply("29", "24", "19", "14"),
         List.apply("20", "18", "13", "11"));

       val annualReturns = new FundVsBenchmark(
         "28/10/2016",
         List.apply("2016", "2015", "2014", "2013"),
         List.apply("36", "33", "21", "28"),
         List.apply("25", "28", "15", "22"));

       val quarterlyReturns = new FundVsBenchmark(
         "28/10/2016",
         List.apply("2016", "2015", "2014", "2013"),
         List.apply("36", "33", "21", "28", "36", "33", "21", "28", "36", "33", "21", "28", "36", "33", "21", "28"),
         List.apply("38", "32", "15", "40", "31", "28", "23", "27", "34", "33", "19", "27", "37", "36", "17", "32"));

       val monthlyReturns = new FundVsBenchmark(
         "2015",
         List.apply("Jaunary", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"),
         List.apply("36", "33", "21", "28", "36", "33", "21", "28", "36", "33", "21", "28"),
         List.apply("25", "28", "15", "22", "25", "28", "15", "22", "25", "28", "15", "22"));

       val peerComparision = List.apply(
         //FundDoc(19, "HSBC Tax Saver Equity Fund - Dividend", 4, 5, 3000, "HSBC", 1, "Equity", 2, "Large Cap", 10, 12, 13, 15, 24, 18, 45, "Moderately High", true),
         //FundDoc(20, "Birla Sun Life Fixed Term Plan Series Dr", 4, 5, 3000, "Birla", 1, "Equity", 2, "Large Cap", 10, 12, 13, 15, 24, 18, 45, "Moderately High", true),
         //FundDoc(21, "ICICI Prudential FMCG Fund - Dividend Reinvestment", 4, 5, 3000, "ICICI", 1, "Equity", 2, "Large Cap", 10, 12, 13, 15, 24, 18, 45, "Moderately High", true)
       );

       val top5Holdings = new FundHoldings("28/10/2016", List.apply(
         new Holdings("Larsen and Turbo", "Sovereign", "200", "10"),
         new Holdings("Zee Entertainment", "ICRA AAA", "122", "8.3"),
         new Holdings("Housing Development Fin.", "Crisil A+", "96", "5.4"),
         new Holdings("Infosys", "Sovereign", "53", "4.3"),
         new Holdings("Kotak Mahindra", "Crisil AA+", "20", "2.1")));
       val fundHoldings = new FundDataMap("28/10/2016", Map.apply(("Sovereign", "10"), ("ICRA AAA", "20"), ("Crisil A+", "40"), ("Crisil AA+", "18"), ("Sovereign", "12")));

       val debtFundAdvancedFactsheet = new DebtFundAdvancedFactsheet(basicFactsheet, sinceInception, annualReturns,
         monthlyReturns, quarterlyReturns, peerComparision, top5Holdings, fundHoldings);

       Ok(Json.toJson(debtFundAdvancedFactsheet));*/

      Ok

    } else {

      /*val basicFactsheet = new FundBasicFactsheet(
        id = fundId,
        name = "Axis Equity Fund",
        description = Some("To achieve long term capital appreciation by investing in a diversified portfolio predominantly consisting of equity and equity related securities including derivatives. However, there can be no assurance that the investment objective of the Scheme will be achieved."),
        nav = Some("20.75"),
        inceptionDate = Some("5th January 2010"),
        category = Some("Equity"),
        fundType = Some("Open-Ended"),
        aum = Some("2166.16"),
        expenseRatio = Some("0.97"),
        fincashRating = Some("4"),
        exitLoad = Some("200"),
        benchmarkName = Some("Nifty 500"),
        minInvAmt = Some("5000"),
        minSIPAmt = Some("500"),
        alphaRatio = Some("0.97"),
        sharpeRatio = Some("0.97"),
        infoRatio = Some("0.97"),
        retSinceInception = Some("12"),
        ret1yr = Some("10"),
        ret3yr = Some("12"),
        ret5yr = Some("15"),
        riskometer = Some("High"));

      //asOfDate: String, dataPoints: List[String], fund: List[String], benchmark: List[String]

      val sinceInception = new FundVsBenchmark(
        "28/10/2016",
        List.apply("1 year", "3 year", "5 year", "Since Inception"),
        List.apply("29", "24", "19", "14"),
        List.apply("20", "18", "13", "11"));

      val annualReturns = new FundVsBenchmark(
        "28/10/2016",
        List.apply("2016", "2015", "2014", "2013"),
        List.apply("36", "33", "21", "28"),
        List.apply("25", "28", "15", "22"));

      val quarterlyReturns = new FundVsBenchmark(
        "28/10/2016",
        List.apply("2016", "2015", "2014", "2013"),
        List.apply("36", "33", "21", "28", "36", "33", "21", "28", "36", "33", "21", "28", "36", "33", "21", "28"),
        List.apply("38", "32", "15", "40", "31", "28", "23", "27", "34", "33", "19", "27", "37", "36", "17", "32"));

      val peerComparision = List.apply(
        //FundDoc(19, "HSBC Tax Saver Equity Fund - Dividend", 4, 5, 3000, "HSBC", 1, "Equity", 2, "Large Cap", 10, 12, 13, 15, 24, 18, 45, "Moderately High", true),
        //FundDoc(20, "Birla Sun Life Fixed Term Plan Series Dr", 4, 5, 3000, "Birla", 1, "Equity", 2, "Large Cap", 10, 12, 13, 15, 24, 18, 45, "Moderately High", true),
        //FundDoc(21, "ICICI Prudential FMCG Fund - Dividend Reinvestment", 4, 5, 3000, "ICICI", 1, "Equity", 2, "Large Cap", 10, 12, 13, 15, 24, 18, 45, "Moderately High", true)
      );

      val top5Holdings = new FundDataMap("28/10/2016", Map.apply(("Larnsen and Turbo", "10"), ("Zee Entertainment", "8.5"), ("Housing Development Fin.", "7.3"), ("Infosys", "5.4"), ("Kotak Mahindra", "2.8")));
      val top5Sectors = new FundDataMap("28/10/2016", Map.apply(("Pharama", "10"), ("Construction", "8.5"), ("Telecom", "7.3"), ("Industrial", "5.4"), ("Consumer", "2.8")));
      val assetAllocation = new FundDataMap("28/10/2016", Map.apply(("Stocks", "10"), ("Bonds", "20"), ("Cash", "30"), ("Others", "40")));

      val sipPerformance = new FundVsBenchmark(
        "28/10/2016",
        List.apply("1", "3", "5"),
        List.apply("16.57", "12.98", "17.06"),
        List.apply("12.83", "8.53", "11.79"));

      val styleCoefficients = StyleCoefficients(
        ListBuffer("Large Cap", "Mid Cap", "Small Cap", "Cash"),
        ListBuffer(47.9, 32.2, 1.5, 18.4))

      val meanReturn = MeanReturn(17.9, -1.7, 16.2)

      val hashMap: LinkedHashMap[String, Double] = LinkedHashMap[String, Double]()
      hashMap.+=("0.5" -> 84.1)
      hashMap.+=("1" -> 89.8)
      hashMap.+=("3" -> 93)
      val liquidityPortfolio = LiquidityPortfolio(
        ListBuffer(0.5, 1, 3),
        ListBuffer(84.1, 89.8, 93)
      )
      val equityFundAdvancedFactsheet = new EquityFundAdvancedFactsheet(basicFactsheet, sinceInception, annualReturns,
        quarterlyReturns, peerComparision, top5Holdings, top5Sectors, assetAllocation, sipPerformance, styleCoefficients, meanReturn, liquidityPortfolio);

      Ok(Json.toJson(equityFundAdvancedFactsheet));*/

      Ok
    }
  }

  /**
    * This method is used to search funds on selection of amc name or some keyword
    *
    * @return
    */
  def getComparisonData = auth.Action(parse.json) {
    request =>

      val fundIds = request.body.\("idList")

      val funds = List.apply(
        Map.apply(
          ("id", "10"),
          ("Name", "Axis Debt Fund"),
          ("Current NAV", "112.5"),
          ("Ratings", "4"),
          ("3 Months Return", "1.34%"),
          ("1 Year Return", "2.34%"),
          ("3 Year Return", "3.34%"),
          ("5 Year Return", "4.34%"),
          ("Category", "Equity"),
          ("Benchmark", "CRISIL Short Term Bond Fund"),
          ("AUM (Cr.)", "628"),
          ("Min Investment", "5000"),
          ("Exit Load", "-")),
        Map.apply(
          ("id", "11"),
          ("Name", "ICICI Prudential Savings Fund"),
          ("Current NAV", "236.8"),
          ("Ratings", "5"),
          ("3 Months Return", "2.50%"),
          ("1 Year Return", "3.00%"),
          ("3 Year Return", "3.50%"),
          ("5 Year Return", "4.00%"),
          ("Category", "Equity"),
          ("Benchmark", "CRISIL Liquid Fund"),
          ("AUM (Cr.)", "378"),
          ("Min Investment", "5000"),
          ("Exit Load", "-")),
        Map.apply(
          ("id", "12"),
          ("Name", "SBI Premier Liquid Fund"),
          ("Current NAV", "1100.6"),
          ("Ratings", "4"),
          ("3 Months Return", "2.13%"),
          ("1 Year Return", "7.13%"),
          ("3 Year Return", "12.13%"),
          ("5 Year Return", "17.13%"),
          ("Category", "Equity"),
          ("Benchmark", "Nifty 50"),
          ("AUM (Cr.)", "455"),
          ("Min Investment", "5000"),
          ("Exit Load", "-")));
      Ok(Json.toJson(funds));
  }


}