package service

import javax.inject.Inject

import constants.SolrConstants
import helpers.SolrSearchHelper
import models.{BankSuggestion, FundDoc}
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrDocument, SolrDocumentList}
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer

/**
  * Created by Fincash on 01-03-2017.
  */
class SolrBankSearchService @Inject() (solrSearchHelper: SolrSearchHelper) extends SolrConstants{

  def autoComplete(queryTerm: String): ListBuffer[BankSuggestion] = {

    val inputQuery = queryTerm.trim;

    val query = new SolrQuery();
    query.set("defType", "edismax");
    query.add("pf", "textShingle");
    query.add("pf", "text");
    query.add("ps", "10");
    query.add("qf", "text");
    query.add("qf", "textShingle");
    query.add("qf", "ifsc^0.5");
    query.add("qf", "bbname^0.5");
    query.add("qf", "bmname^1");
    query.setRows(15);
    query.setQuery(inputQuery);

    var qr: QueryResponse = new QueryResponse();
    var totalResults: Long = 0;

    val splits: Array[String] = solrSearchHelper.splitQuery(inputQuery);

    if (totalResults == 0) {
      query.setQuery(inputQuery)
      qr = solrSearchHelper.execute(query, BANK_CORE);
      totalResults = qr.getResults().getNumFound();

    }

    if (totalResults == 0) {
      query.setQuery(inputQuery)
      qr = solrSearchHelper.spellCheckSearch(query, BANK_CORE);
      totalResults = qr.getResults.getNumFound;
    }

    if (totalResults ==0 && splits != null && splits.length > 0 && splits(0) != "*") {
      if (splits(0).length > 3){
        splits(0) = splits(0).substring(0,splits(0).length-1) + "*";
      }
      val keyword = solrSearchHelper.makeQueryFromArray(splits);
      // try adding star at the end to check misspelling etc.
      query.setQuery(keyword);
      qr = solrSearchHelper.spellCheckSearch(query, BANK_CORE);
      totalResults = qr.getResults.getNumFound;
    }

    if (totalResults > 0) {
      getResultAsBanksuggestionList(qr.getResults);
    } else {
      return ListBuffer.empty[BankSuggestion];
    }

  }

  def getResultAsBanksuggestionList(searchResults: SolrDocumentList): ListBuffer[BankSuggestion] = {
    val iterator = searchResults.iterator();
    val banksList = ListBuffer.empty[BankSuggestion];

    while (iterator.hasNext()) {
      val solrDoc = iterator.next()
      banksList += getBanksuggestionsFromBanksSolrDoc(solrDoc);
    }

    return banksList
  }

  def getBanksuggestionsFromBanksSolrDoc(doc: SolrDocument): BankSuggestion = {
    var bankDoc: BankSuggestion = new BankSuggestion("", "", "", Option(""), "")
    //doc.getFieldValue("dfields").asInstanceOf[FundDoc];
    val id = doc.getFieldValue(BANK_BMTID);
    if (id != null) {
      bankDoc = bankDoc.copy(bmtrfnum = id.asInstanceOf[Integer].toString)
    }
    val name = doc.getFieldValue(BANK_D_BANKNAME);
    if (name != null) {
      bankDoc = bankDoc.copy(bmtbankname = name.asInstanceOf[String])
    }
    val bbid = doc.getFieldValue(BANK_BBTID);
    if (bbid != null) {
      bankDoc = bankDoc.copy(bbtrfnum = bbid.asInstanceOf[Integer].toString)
    }
    val branchname = doc.getFieldValue(BANK_D_BRANCHNAME);
    if (branchname != null) {
      bankDoc = bankDoc.copy(bbtbranchname = Some(branchname.asInstanceOf[String]))
    }
    val ifsc = doc.getFieldValue(BANK_D_IFSC);
    if (ifsc != null) {
      bankDoc = bankDoc.copy(bbtifsccode = ifsc.asInstanceOf[String])
    }
    bankDoc
  }

}
