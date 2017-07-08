package helpers

import javax.inject.Inject

import models.{Bank, BankSearchQuery}
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrDocument, SolrDocumentList}
import org.slf4j.LoggerFactory
import play.Configuration

class SolrBankSearchHelper @Inject() (config: Configuration) {

  val logger, log = LoggerFactory.getLogger(classOf[SolrBankSearchHelper])

  val TEXT = "txt"
  val IFSC = "IFSC"
  val BR = "dbr"
  val ADDR = "daddr"
  val CITY = "dcity"
  val BANK = "db"
  val ID = "bid"

  //standard solr operators
  val AND_OPERATOR = " AND "
  val OR_OPERATOR = " OR "
  val TO_OPERATOR = " TO "

  def execute(query: SolrQuery): QueryResponse = {
    logger.debug("query: " + query)
    val urlString = config.getString("solr.url") + config.getString("solr.core.banks");
    val solr = new HttpSolrClient.Builder(urlString).build();
    solr.query(query)
  }

  def branchSearch(searchQuery: BankSearchQuery): QueryResponse = {
    val query = new SolrQuery();
    var keyword: String = ""

    //keyword
    if (searchQuery.keyword.get.length > 0)
      keyword = TEXT + ":" + searchQuery.keyword.get
//    else
//      keyword = TEXT + ":" + "*"
//    
    logger.debug(keyword)
    
    //bank
    if (searchQuery.bank_id.get > 0){
      
      var banknameSearchQuery = BANK + ":" + searchQuery.bank_name.get
      var bankIdSearchQuery = ID + ":" + searchQuery.bank_id.get
//      searchQuery.bank_name.get.zipWithIndex.foreach {
//        case (bank_name, i) => {
//          banknameSearchQuery = banknameSearchQuery + BANK + ":" + bank_name
//        }
//        if (!(searchQuery.bank_name.get.length() == i+1)){
//          banknameSearchQuery = banknameSearchQuery + OR_OPERATOR
//        }
//      }
      keyword = bankIdSearchQuery + AND_OPERATOR + keyword;
    }

    logger.debug(keyword)
   
    //sorting always break tie on dname
//    if (searchQuery.sortElement.get.length() > 0) {
//      query.set("sort", searchQuery.sortElement.get + " " + searchQuery.sortOrder.get + ",dbr")
//    } else {
//      query.set("sort", "dbr asc") //if no sorting found then default sort will be on name
//    }
    
    query.set("q", keyword)
//    query.set("rows", searchQuery.rows.get.toInt)
//    query.set("start", searchQuery.from.get.toInt)
//    logger.debug(query.getSorts)
//    logger.debug(query.getQuery)
    execute(query)
  }
  
  def getResultsAsBankDoc(searchResults: SolrDocumentList) = {
    val iterator = searchResults.iterator();
    var bankDocList: Seq[Bank] = Seq.empty
    
    while (iterator.hasNext()) {
      val solrDoc = iterator.next()
      
      bankDocList = bankDocList :+ fromBanksSolrDocToBankDoc(solrDoc);
    }
    bankDocList
  }
  
  def fromBanksSolrDocToBankDoc(doc: SolrDocument): Bank = {
    var bank: Bank = new Bank(Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""))
    
    val name = doc.getFieldValue("db");
    if (name != null) {
      bank = bank.copy(bankName = Option(name.asInstanceOf[String]))
    }
    
    val IFSC = doc.getFieldValue("dIFSC");
    if (IFSC != null) {
      bank = bank.copy(IFSC = IFSC.asInstanceOf[Option[String]])
    }
    
    val MICR = doc.getFieldValue("dMICR");
    if (MICR != null) {
      bank = bank.copy(MICR = MICR.asInstanceOf[Option[String]])
    }
    
    val branch = doc.getFieldValue("dbr")
    if (branch != null) {
      bank = bank.copy(branch = branch.asInstanceOf[Option[String]])
    }
    
    val address = doc.getFieldValue("daddr")
    if (address != null) {
      bank = bank.copy(address = address.asInstanceOf[Option[String]])
    }
    
    val city = doc.getFieldValue("dcity")
    if (city != null) {
      bank = bank.copy(city = city.asInstanceOf[Option[String]])
    }
    
    val district = doc.getFieldValue("ddis")
    if (district != null) {
      bank = bank.copy(district = district.asInstanceOf[Option[String]])
    }
    
    val state = doc.getFieldValue("ds")
    if (state != null) {
      bank = bank.copy(state = state.asInstanceOf[Option[String]])
    }
    bank
  }

}