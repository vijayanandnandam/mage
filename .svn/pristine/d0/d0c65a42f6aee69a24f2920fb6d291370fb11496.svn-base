package service

import com.google.inject.Inject
import constants.SolrConstants
import helpers.SolrSearchHelper
import models._
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrDocument, SolrDocumentList}
import play.api.libs.json.Json
import repository.module.SchemeRepository

import scala.collection.mutable.ListBuffer

class SolrFundSearchService @Inject()(solrSearchHelper: SolrSearchHelper, schemeRepository: SchemeRepository) extends SolrConstants {

  /**
    * This method is used to get the list of AMC available in the index
    *
    * @return
    */
  def getAmcList(searchQuery: SearchQuery): ListBuffer[FundFilter] = {
    // val solrQuery: SolrQuery = filterBySearchType(searchQuery);
    val solrQuery = new SolrQuery();
    solrQuery.setQuery(searchQuery.q.get);
    val facetList = solrSearchHelper.getFacetData(solrQuery, "*", FUND_D_AMC, FUND_CORE);
    val amcFilterData = for (facet <- facetList) yield {
      new FundFilter(facet.name, facet.name, facet.count, false);
    }
    return amcFilterData
  }

  /**
    * Category List as per the latest fund search
    *
    * @param preparedQuery
    * @return
    */
  def getCategoryList(searchQuery: SearchQuery): ListBuffer[FundFilter] = {
    //val solrQuery: SolrQuery = filterBySearchType(searchQuery);
    val solrQuery = new SolrQuery();
    solrQuery.setQuery(searchQuery.q.get);
    val facetList = solrSearchHelper.getFacetData(solrQuery, "*", FUND_D_CATEGORY, FUND_CORE);

    val categoryFilterData = for (facet <- facetList) yield {
      new FundFilter(facet.name, facet.name, facet.count, false);
    }
    return categoryFilterData;
  }

  /**
    * Sub Category List as per the latest fund search
    *
    * @param preparedQuery
    * @return
    */
  def getSubCategoryList(searchQuery: SearchQuery): ListBuffer[FundFilter] = {
    //val solrQuery: SolrQuery = filterBySearchType(searchQuery);
    val solrQuery = new SolrQuery();
    solrQuery.setQuery(searchQuery.q.get);
    val facetList = solrSearchHelper.getFacetData(solrQuery, "*", FUND_D_SUB_CATEGORY, FUND_CORE);

    val subCategoryFilterData = for (facet <- facetList) yield {
      new FundFilter(facet.name, facet.name, facet.count, false);
    }
    return subCategoryFilterData;

  }

  /**
    * Category List as per the latest fund search
    *
    * @param preparedQuery
    * @return
    */
  def getAgeList(searchQuery: SearchQuery): ListBuffer[FundFilter] = {
    //val query = filterBySearchType(searchQuery);
    val query = new SolrQuery();
    query.setQuery(searchQuery.q.get);
    val intervals = Array("[1,*]", "[3,*]", "[5,*]", "[7,*]", "[10,*]");
    val facetList = solrSearchHelper.getFacetDataForIntervals(query, "*", FUND_D_AGE, intervals, FUND_CORE);
    val ageFilterData = ListBuffer.empty[FundFilter];
    facetList.foreach { (facet: Facet) =>
      val facetNames = facet.name.split(',');
      ageFilterData += new FundFilter(facetNames(0).slice(1, facetNames.length + 1), facetNames(0).slice(1, facetNames.length + 1), facet.count, false);
    }
    return ageFilterData;
  }

  /**
    * Category List as per the latest fund search
    *
    * @param preparedQuery
    * @return
    */
  def getRatingList(searchQuery: SearchQuery): ListBuffer[FundFilter] = {
    //val query = filterBySearchType(searchQuery);
    val query = new SolrQuery();
    query.setQuery(searchQuery.q.get);
    val intervals = Array("[1,2)", "[2,3)", "[3,4)", "[4,5)", "[5,6)");
    val facetList = solrSearchHelper.getFacetDataForIntervals(query, "*", FUND_D_RATING, intervals, FUND_CORE);
    val ratingFilterData = ListBuffer.empty[FundFilter];
    facetList.foreach { (facet: Facet) =>
      val abc = facet.name.split(',');
      ratingFilterData += new FundFilter(abc(0).slice(1, abc.length + 1), abc(0).slice(1, abc.length + 1), facet.count, false);
    }
    return ratingFilterData;
  }

  def getAUMRange() = {
    //Json.obj("min" -> 0, "max" -> 100000)
  }

  /**
    * Prepares and executes Query for fund search on fund search core
    *
    * @param searchQuery
    * @return
    */
  def fundSearch(searchQuery: SearchQuery): FundSearchResult = {
    val query = getFundSearchQuery(searchQuery);

    var inputQuery = query.getQuery;
    var shortQuery = "";

    if (inputQuery.length > 4) {
      shortQuery = inputQuery.substring(0, inputQuery.length - 2);
    } else {
      shortQuery = inputQuery;
    }


    query.setRows(searchQuery.r.get.toInt)
    query.setStart(searchQuery.f.get.toInt)

    var qr: QueryResponse = new QueryResponse();
    var totalResults: Long = 0;

    val splits: Array[String] = solrSearchHelper.splitQuery(shortQuery);
    if (splits != null && splits.length > 0 && splits(0) != "*") {
      splits(0) = splits(0) + "*";
      val keyword = solrSearchHelper.makeQueryFromArray(splits);
      // try adding star at the end to check misspelling etc.
      query.setQuery(keyword);
      qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
      totalResults = qr.getResults.getNumFound;
    }


    if (totalResults == 0) {
      query.setQuery(inputQuery)
      qr = solrSearchHelper.execute(query, FUND_CORE);
      totalResults = qr.getResults().getNumFound();

    }

    if (totalResults == 0) {
      query.setQuery(inputQuery)
      qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
      totalResults = qr.getResults.getNumFound;
    }


    if (totalResults == 0) {
      val splits: Array[String] = solrSearchHelper.splitQuery(shortQuery);
      if (splits != null && splits.length > 0 && splits(0) != "*") {
        splits(0) = "*" + splits(0) + "*";
        // try adding star at the end to check misspelling etc.
        query.setQuery(solrSearchHelper.makeQueryFromArray(splits));
        qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
        totalResults = qr.getResults.getNumFound();
      }

    }

    if (totalResults > 0) {
      return new FundSearchResult(totalResults, getResultAsFundList(qr.getResults));
    } else {
      return new FundSearchResult(0, ListBuffer.empty[FundDoc]);
    }
  }


  def autoComplete(queryTerm: String): ListBuffer[FundDoc] = {

    val inputQuery = queryTerm.trim;

    val query = new SolrQuery();
    query.set("defType", "edismax");
    query.add("mm", "2<-75%");
    query.add("mm.autoRelax", "true");
    query.add("pf", "textShingle");
    query.add("pf", "text");
    query.add("ps", "10");
    query.add("qf", "text");
    query.add("qf", "textShingle");
    query.add("qf", "name^4");
    query.add("qf", "amc^3");
    query.add("qf", "cat^2");
    query.add("qf", "scat^1");
    query.add("tie", "0.1");
    query.setRows(15);
    query.setQuery(inputQuery);

    var qr: QueryResponse = new QueryResponse();
    var totalResults: Long = 0;

    val splits: Array[String] = solrSearchHelper.splitQuery(inputQuery);
    if (splits != null && splits.length > 0 && splits(0) != "*") {
      splits(0) = splits(0) + "*";
      val keyword = solrSearchHelper.makeQueryFromArray(splits);
      // try adding star at the end to check misspelling etc.
      query.setQuery(keyword);
      qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
      totalResults = qr.getResults.getNumFound;
    }


    if (totalResults == 0) {
      query.setQuery(inputQuery)
      qr = solrSearchHelper.execute(query, FUND_CORE);
      totalResults = qr.getResults().getNumFound();

    }

    if (totalResults == 0) {
      query.setQuery(inputQuery)
      qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
      totalResults = qr.getResults.getNumFound;
    }


    if (totalResults == 0) {
      val splits: Array[String] = solrSearchHelper.splitQuery(inputQuery);
      if (splits != null && splits.length > 0 && splits(0) != "*") {
        splits(0) = "*" + splits(0) + "*";
        // try adding star at the end to check misspelling etc.
        query.setQuery(solrSearchHelper.makeQueryFromArray(splits));
        qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
        totalResults = qr.getResults.getNumFound();
      }

    }

    if (totalResults > 0) {
      getResultAsFundList(qr.getResults);
    } else {
      return ListBuffer.empty[FundDoc];
    }

  }

  /**
    * This api is used to prepare the search query for the solr core of funds
    *
    * @param searchQuery
    */
  def getFundSearchQuery(searchQuery: SearchQuery): SolrQuery = {


    //filter by search type i.e. ELSS, SIP, ALL funds on frontend
    //val query = filterBySearchType(searchQuery);
    val query = new SolrQuery();

    //keyword search
    if (!searchQuery.q.get.isEmpty() && searchQuery.q.get != "*:*") {
      query.setQuery(searchQuery.q.get.trim);
      query.set("defType", "edismax");
      query.add("mm", "2<-75%");
      query.add("mm.autoRelax", "true");
      query.add("pf", "textShingle");
      query.add("pf", "text");
      query.add("ps", "10");
      query.add("qf", "text");
      query.add("qf", "textShingle");
      query.add("qf", "name^4");
      query.add("qf", "amc^3");
      query.add("qf", "cat^2");
      query.add("qf", "scat^1");
      query.add("tie", "0.1");
    } else {
      query.setQuery("*");
    }

    //sorting always break tie on dname
    if (searchQuery.q.isEmpty && searchQuery.se.isEmpty) {
      solrSearchHelper.getDefaultSort().foreach(data => {
        query.addSort(data._1, data._2);
        //query.addSort(FUND_D_NAME, ORDER.asc) //if no sorting found then default sort will be on name
      })
    } else if (searchQuery.se.isDefined && searchQuery.se.get.length > 0) {
      if (searchQuery.so.get.equals(ORDER.desc.name()))
        query.addSort(searchQuery.se.get, ORDER.desc)
      else
        query.addSort(searchQuery.se.get, ORDER.asc)
      query.addSort(FUND_D_NAME, ORDER.asc) //if no sorting found then default sort will be on name
    }

    //rating
    if (searchQuery.ra.isDefined && searchQuery.ra.get.length > 0) {
      val criteria = new StringBuilder("(");
      searchQuery.ra.get.zipWithIndex.foreach {
        case (rating, i) =>
          criteria.append(FUND_D_RATING + ":" + rating.toString());
          if (!(searchQuery.ra.get.length == i + 1)) {
            criteria.append(OR_OPERATOR);
          }
      }
      query.add("fq", criteria.append(")").toString());
    }

    //age
    if (searchQuery.ag.isDefined && searchQuery.ag.get > 0) {
      query.add("fq", FUND_D_AGE + ":" + "[" + searchQuery.ag.get + TO_OPERATOR + "*]");
    }

    //aum
    if (searchQuery.ar.isDefined && searchQuery.ar.get.length() > 0) {
      query.add("fq", FUND_D_AUM + ":" + "[" + searchQuery.ar.get + "]");
    }

    //category
    if (searchQuery.c.isDefined && searchQuery.c.get.length > 0) {
      val categorySearchQuery = new StringBuilder("(");
      searchQuery.c.get.zipWithIndex.foreach {
        case (category, i) =>
          categorySearchQuery.append(FUND_D_CATEGORY + ":" + "\"" + category + "\"");
          if (!(searchQuery.c.get.length == i + 1)) {
            categorySearchQuery.append(OR_OPERATOR);
          }
      }
      query.add("fq", categorySearchQuery.append(")").toString());
    }

    //sub category
    if (searchQuery.sc.isDefined && searchQuery.sc.get.length > 0) {
      val subCategorySearchQuery = new StringBuilder("(");
      searchQuery.sc.get.zipWithIndex.foreach {
        case (subCategory, i) =>
          subCategorySearchQuery.append(FUND_D_SUB_CATEGORY + ":" + "\"" + subCategory + "\"");
          if (!(searchQuery.sc.get.length == i + 1)) {
            subCategorySearchQuery.append(OR_OPERATOR);
          }
      }
      query.add("fq", subCategorySearchQuery.append(")").toString());
    }

    //amc
    if (searchQuery.am.isDefined && searchQuery.am.get.length > 0) {
      val amcSearchQuery = new StringBuilder("(");
      searchQuery.am.get.zipWithIndex.foreach {
        case (amc, i) =>
          amcSearchQuery.append(FUND_D_AMC + ":" + "\"" + amc + "\"");
          if (!(searchQuery.am.get.length == i + 1)) {
            amcSearchQuery.append(OR_OPERATOR);
          }
      }
      query.add("fq", amcSearchQuery.append(")").toString());
    }

    //others
    if (searchQuery.o.isDefined && searchQuery.o.get.length > 0) {
      if (searchQuery.o.get.split(':')(0) == "sip") {
        query.add("fq", FUND_D_SIP + ":" + Y_FLAG);
      }
    }


    return query;
  }

  def filterBySearchType(searchQuery: SearchQuery): SolrQuery = {
    val query: SolrQuery = new SolrQuery;

    /*//category
    if (searchQuery.c.get.length > 0) {
      val categorySearchQuery = new StringBuilder("(");
      searchQuery.c.get.zipWithIndex.foreach {
        case (category, i) =>
          categorySearchQuery.append(FUND_D_CATEGORY + ":" + "\"" + category + "\"");
          if (!(searchQuery.c.get.length == i + 1)) {
            categorySearchQuery.append(OR_OPERATOR);
          }
      }
      query.add("fq", categorySearchQuery.append(")").toString());
    }


    //filter based on search type
    if (searchQuery.searchType.get == "sip") {
      query.add("fq", FUND_D_SIP + ":" + "Y");
      query.add("fq", FUND_D_CATEGORY + ":" + "\"Equity\"");
    } else if (searchQuery.searchType.get == "elss") {
      query.add("fq", FUND_D_SUB_CATEGORY + ":" + "\"ELSS\"");
    }*/
    query;
  }

  /**
    * Converts SolrDocumentList to Seq[FundDoc]
    *
    * @param searchResults
    * @return
    */
  def getResultAsFundList(searchResults: SolrDocumentList): ListBuffer[FundDoc] = {
    val iterator = searchResults.iterator();
    val fundsList = ListBuffer.empty[FundDoc];

    while (iterator.hasNext()) {
      val solrDoc = iterator.next()
      fundsList += getFundFromFundsSolrDoc(solrDoc);
    }

    return fundsList
  }

  /**
    *
    * This api maps solr result documents to individual FundDoc model
    *
    * @param doc
    * @return
    */
  def getFundFromFundsSolrDoc(doc: SolrDocument): FundDoc = {
    var fundDoc: FundDoc = new FundDoc(0, "", 0, 0, 0, 0, "", 0, "", 0, "", "");
    //doc.getFieldValue("dfields").asInstanceOf[FundDoc];
    val id = doc.getFieldValue("fid");
    if (id != null) {
      fundDoc = fundDoc.copy(id = id.asInstanceOf[Int])
    }
    val name = doc.getFieldValue("dname");
    if (name != null) {
      fundDoc = fundDoc.copy(name = name.asInstanceOf[String])
    }
    val age = doc.getFieldValue("dage");
    if (age != null) {
      fundDoc = fundDoc.copy(age = age.asInstanceOf[Double])
    }
    val rating = doc.getFieldValue("drat");
    if (rating != null) {
      fundDoc = fundDoc.copy(rating = rating.asInstanceOf[Int])
    }
    val aum = doc.getFieldValue("daum");
    if (aum != null) {
      fundDoc = fundDoc.copy(aum = aum.asInstanceOf[Double])
    }
    val amc = doc.getFieldValue("damc");
    if (amc != null) {
      fundDoc = fundDoc.copy(amc = amc.asInstanceOf[String])
    }
    val categoryId = doc.getFieldValue("cid");
    if (categoryId != null) {
      fundDoc = fundDoc.copy(cid = categoryId.asInstanceOf[Int])
    }
    val category = doc.getFieldValue("dcat");
    if (category != null) {
      fundDoc = fundDoc.copy(category = category.asInstanceOf[String])
    }
    val subCategory = doc.getFieldValue("dscat");
    if (subCategory != null) {
      fundDoc = fundDoc.copy(subCategory = subCategory.asInstanceOf[String])
    }
    val subCategoryId = doc.getFieldValue("scid");
    if (subCategoryId != null) {
      fundDoc = fundDoc.copy(scid = subCategoryId.asInstanceOf[Int])
    }
    val amcId = doc.getFieldValue("amcid");
    if (amcId != null) {
      fundDoc = fundDoc.copy(amcid = amcId.asInstanceOf[Int])
    }
    val risk = doc.getFieldValue(FUND_D_RISK);
    if (risk != null) {
      fundDoc = fundDoc.copy(risk = risk.asInstanceOf[String])
    }
    val sip = doc.getFieldValue(FUND_D_SIP);
    if (sip != null) {
      if (sip == Y_FLAG) {
        fundDoc = fundDoc.copy(sipAllowed = Some(true))
      } else {
        fundDoc = fundDoc.copy(sipAllowed = Some(false))
      }
    }
    val basicFactsheetFields = doc.getFieldValue("dfields");
    val basicFactsheetJson = Json.parse(basicFactsheetFields.asInstanceOf[String]);
    if (basicFactsheetFields != null) {
      fundDoc = fundDoc.copy(basicFactsheet = Some(basicFactsheetJson))
    }
    fundDoc
  }


  /**
    * Prepares and executes Query for fund search on fund search core
    *
    * @param searchQuery
    * @return
    */
  def fundSearchQueryKeyword(searchQuery: SearchQuery): String = {
    val query = getFundSearchQuery(searchQuery);
    val inputQuery = query.getQuery;
    var searchKeyword = inputQuery;
    var shortQuery = "";

    if (inputQuery.length > 4) {
      shortQuery = inputQuery.substring(0, inputQuery.length - 2);
    } else {
      shortQuery = inputQuery;
    }


    query.setRows(searchQuery.r.get.toInt)
    query.setStart(searchQuery.f.get.toInt)

    var qr: QueryResponse = new QueryResponse();
    var totalResults: Long = 0;

    val splits: Array[String] = solrSearchHelper.splitQuery(shortQuery);
    if (splits != null && splits.length > 0 && splits(0) != "*") {
      splits(0) = splits(0) + "*";
      val keyword = solrSearchHelper.makeQueryFromArray(splits);
      searchKeyword = keyword;
      // try adding star at the end to check misspelling etc.
      query.setQuery(keyword);
      qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
      totalResults = qr.getResults.getNumFound;
    }


    if (totalResults == 0) {
      searchKeyword = inputQuery;
      query.setQuery(inputQuery)
      qr = solrSearchHelper.execute(query, FUND_CORE);
      totalResults = qr.getResults().getNumFound();

    }

    if (totalResults == 0) {
      searchKeyword = inputQuery;
      query.setQuery(inputQuery)
      qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
      totalResults = qr.getResults.getNumFound;
    }


    if (totalResults == 0) {
      val splits: Array[String] = solrSearchHelper.splitQuery(shortQuery);
      if (splits != null && splits.length > 0 && splits(0) != "*") {
        splits(0) = "*" + splits(0) + "*";
        // try adding star at the end to check misspelling etc.
        searchKeyword = solrSearchHelper.makeQueryFromArray(splits)
        query.setQuery(searchKeyword);
        qr = solrSearchHelper.spellCheckSearch(query, FUND_CORE);
        totalResults = qr.getResults.getNumFound();
      }

    }

    return searchKeyword;
  }

}