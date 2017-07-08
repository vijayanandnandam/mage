package service

import com.google.inject.Inject
import constants.SolrConstants
import helpers.SolrSearchHelper
import models._
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrDocument, SolrDocumentList}

import scala.collection.mutable.ListBuffer

class SolrCNDSearchService @Inject()(solrSearchHelper: SolrSearchHelper) extends SolrConstants {

  /**
    * This method is used to get the list of AMC available in the index
    *
    * @return
    */
  def cndSearch(cndGroup: String, cndName: String): ListBuffer[CNDDoc] = {

    val query = new SolrQuery();
    query.setQuery(cndName)
    query.set("defType", "edismax")
    /*
    query.add("pf", "dtextShingle")
    query.add("pf", "dtext")
    query.add("ps", "10")
    query.add("qf", "dtext")
    query.add("qf", "dtextShingle")
    query.add("qf", "dname^1")
    query.add("tie", "0.1")
    */


    val inputQuery = cndName;
    var shortQuery = ""

    if (inputQuery.length > 4) {
      shortQuery = inputQuery.substring(0, inputQuery.length - 2)
    } else {
      shortQuery = inputQuery
    }

    var qr: QueryResponse = new QueryResponse()
    var totalResults: Long = 0
    //    val splits: Array[String] = solrSearchHelper.splitQuery(shortQuery);

    if (totalResults == 0) {
      query.setQuery(inputQuery+ AND_OPERATOR +CND_D_GROUP_NAME + ":" + cndGroup)
      qr = solrSearchHelper.execute(query, CND_CORE)
      totalResults = qr.getResults().getNumFound()

    }

    if (totalResults == 0) {
      query.setQuery(inputQuery + AND_OPERATOR +CND_D_GROUP_NAME + ":" + cndGroup)
      qr = solrSearchHelper.spellCheckSearch(query, CND_CORE)
      totalResults = qr.getResults.getNumFound
    }

    if (totalResults == 0) {
      var keyword = inputQuery
      if (inputQuery.length > 3) {
        keyword = inputQuery.substring(0, inputQuery.length - 1) + "*"
      }
      query.setQuery(keyword+ AND_OPERATOR +CND_D_GROUP_NAME + ":" + cndGroup)
      qr = solrSearchHelper.spellCheckSearch(query, CND_CORE)
      totalResults = qr.getResults.getNumFound
    }

    if (totalResults > 0) {
      getResultAsCNDList(qr.getResults)
    } else {
      return ListBuffer.empty[CNDDoc]
    }

  }


  /**
    * Converts SolrDocumentList to Seq[FundDoc]
    *
    * @param searchResults
    * @return
    */
  def getResultAsCNDList(searchResults: SolrDocumentList): ListBuffer[CNDDoc] = {
    val iterator = searchResults.iterator();
    val cndList = ListBuffer.empty[CNDDoc];

    while (iterator.hasNext()) {
      val solrDoc = iterator.next()
      cndList += getCndFromCNDSolrDoc(solrDoc);
    }

    return cndList
  }

  /**
    *
    * This api maps solr result documents to individual FundDoc model
    *
    * @param doc
    * @return
    */
  def getCndFromCNDSolrDoc(doc: SolrDocument): CNDDoc = {
    var cndDoc: CNDDoc = new CNDDoc(cndRfnum = 0,
      cndName = "",
      cndDescription = "",
      cndGroupName = "",
      cndActiveFlag = "",
      cndCndRfnum = 0,
      cndExternalField1 = "",
      cndExternalField2 = "",
      cndExternalField3 = "",
      cndExternalField4 = "",
      cndCode = "",
      cndSequence = 0);
    //doc.getFieldValue("dfields").asInstanceOf[FundDoc];
    val cid = doc.getFieldValue(CND_ID);
    if (cid != null) {
      cndDoc = cndDoc.copy(cndRfnum = cid.asInstanceOf[Integer].toLong)
    }
    val ccid = doc.getFieldValue(CND_CND_ID);
    if (ccid != null) {
      cndDoc = cndDoc.copy(cndCndRfnum = ccid.asInstanceOf[Integer].toLong)
    }
    val name = doc.getFieldValue(CND_D_NAME);
    if (name != null) {
      cndDoc = cndDoc.copy(cndName = name.asInstanceOf[String])
    }
    val description = doc.getFieldValue(CND_D_DESC);
    if (description != null) {
      cndDoc = cndDoc.copy(cndDescription = description.asInstanceOf[String])
    }
    val groupName = doc.getFieldValue(CND_D_GROUP_NAME);
    if (groupName != null) {
      cndDoc = cndDoc.copy(cndGroupName = groupName.asInstanceOf[String])
    }
    val isActive = doc.getFieldValue(CND_D_IS_ACTIVE);
    if (isActive != null) {
      cndDoc = cndDoc.copy(cndActiveFlag = isActive.asInstanceOf[String])
    }
    val ef1 = doc.getFieldValue(CND_D_EXTERNAL_FIELD_1);
    if (ef1 != null) {
      cndDoc = cndDoc.copy(cndExternalField1 = ef1.asInstanceOf[String])
    }
    val ef2 = doc.getFieldValue(CND_D_EXTERNAL_FIELD_2);
    if (ef2 != null) {
      cndDoc = cndDoc.copy(cndExternalField2 = ef2.asInstanceOf[String])
    }
    val ef3 = doc.getFieldValue(CND_D_EXTERNAL_FIELD_3);
    if (ef3 != null) {
      cndDoc = cndDoc.copy(cndExternalField3 = ef3.asInstanceOf[String])
    }
    val ef4 = doc.getFieldValue(CND_D_EXTERNAL_FIELD_4);
    if (ef4 != null) {
      cndDoc = cndDoc.copy(cndExternalField4 = ef4.asInstanceOf[String])
    }
    val code = doc.getFieldValue(CND_D_CODE);
    if (code != null) {
      cndDoc = cndDoc.copy(cndCode = code.asInstanceOf[String])
    }
    val seq = doc.getFieldValue(CND_D_SEQUENCE);
    if (seq != null) {
      cndDoc = cndDoc.copy(cndSequence = seq.asInstanceOf[Double])
    }
    cndDoc
  }

}