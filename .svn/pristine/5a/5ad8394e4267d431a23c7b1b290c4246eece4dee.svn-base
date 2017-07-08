package helpers

import javax.inject.Inject

import com.google.inject.name.Named
import constants.SolrConstants
import models.Facet
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.{QueryResponse, SpellCheckResponse}
import org.slf4j.LoggerFactory
import play.Configuration

import scala.collection.mutable.ListBuffer

class SolrSearchHelper @Inject()(config: Configuration,
                                 @Named("solrPoolingClientConnectionManager") solrPoolingClientConnectionManager: PoolingClientConnectionManager)
  extends SolrConstants {

  val logger, log = LoggerFactory.getLogger(classOf[SolrSearchHelper])

  val fundSolrClient = new HttpSolrClient.Builder(config.getString("solr.url") + config.getString("solr.core." + FUND_CORE)).withHttpClient(solrPoolingClientConnectionManager.getHttpClient).build();
  val bankSolrClient = new HttpSolrClient.Builder(config.getString("solr.url") + config.getString("solr.core." + BANK_CORE)).withHttpClient(solrPoolingClientConnectionManager.getHttpClient).build();
  val cndSolrClient = new HttpSolrClient.Builder(config.getString("solr.url") + config.getString("solr.core." + CND_CORE)).withHttpClient(solrPoolingClientConnectionManager.getHttpClient).build();

  def execute(query: SolrQuery, coreName: String): QueryResponse = {
    logger.debug(query.toString);

    if(coreName == CND_CORE){
      cndSolrClient.query(query)
    } else if(coreName == BANK_CORE){
      bankSolrClient.query(query)
    } else{
      fundSolrClient.query(query)
    }
  }

  def spellCheckSearch(query: SolrQuery, coreName: String): QueryResponse = {
    var totalResults: Long = 0;
    var qr = execute(query, coreName);
    if (qr != null) {
      totalResults = qr.getResults().getNumFound();
      if (totalResults == 0) {
        val spresponse: SpellCheckResponse = qr.getSpellCheckResponse();
        if (spresponse != null) {
          if (!spresponse.isCorrectlySpelled()) {
            val collations = spresponse.getCollatedResults();
            if (collations != null && collations.size() > 0) {
              val collationsItr = collations.iterator;
              var keepLooping = true;
              while (collationsItr.hasNext() && keepLooping) {
                val collation = collationsItr.next;
                val collationQuery: String = collation.getCollationQueryString();
                if (collationQuery != null && collationQuery.trim().length() > 0) {
                  query.setQuery(collationQuery);
                  qr = execute(query, coreName);
                  totalResults = qr.getResults().getNumFound();
                  if (totalResults > 0) {
                    keepLooping = false;
                  }
                }
              }
            } else {
              val collation: String = spresponse.getCollatedResult();
              if (collation != null && collation.trim().length() > 0) {
                query.setQuery(collation);
                qr = execute(query, coreName);
              }
            }
          }
        }
      }
    }
    return qr;

  }

  def getFacetData(query: SolrQuery, facetQueryString: String, field: String, coreName: String): ListBuffer[Facet] = {
    query.setFacet(true);
    query.addFacetField(field);
    query.addFacetQuery(facetQueryString);
    //query.setFacetMinCount(1);
    logger.debug(query.toString())
    query.getFacetQuery.foreach { x => logger.debug(x) }
    query.getFacetFields.foreach { x => logger.debug(x) }
    val response = execute(query, coreName);
    return getFacetsFromSolrFacetResponse(response);
  }

  def getFacetDataForIntervals(query: SolrQuery, facetQueryString: String, field: String, intervals: Array[String], coreName: String): ListBuffer[Facet] = {
    //val query = new SolrQuery();
    //query.setQuery(queryString);
    query.setFacet(true);
    query.addFacetField(field);
    query.addFacetQuery(facetQueryString);
    query.setFacetMinCount(1);
    query.setFacetSort(field);
    query.addNumericRangeFacet(field, 1, 100, 2);
    query.addIntervalFacets(field, intervals);
    logger.debug(query.toString());
    val response = execute(query, coreName);
    logger.debug(response.getFacetRanges.toString);
    return getFacetRangesFromSolrFacetResponse(response);
  }

  def getFacetsFromSolrFacetResponse(response: QueryResponse): ListBuffer[Facet] = {

    val fields = response.getFacetFields.get(0)
    val counts = fields.getValues();
    val itr = counts.listIterator();
    val facets = ListBuffer.empty[Facet];
    while (itr.hasNext()) {
      val entry = itr.next();
      facets += new Facet(entry.getName, entry.getCount);
    }
    return facets
  }

  def getFacetRangesFromSolrFacetResponse(response: QueryResponse): ListBuffer[Facet] = {
    val fields = response.getIntervalFacets.get(0)
    val counts = fields.getIntervals;
    val itr = counts.listIterator();
    val facets = ListBuffer.empty[Facet];
    while (itr.hasNext()) {
      val entry = itr.next();
      facets += new Facet(entry.getKey, entry.getCount);
    }
    return facets
  }

  def getDefaultSort(): ListBuffer[(String, SolrQuery.ORDER)] = {
    val sortingArr = ListBuffer[(String, SolrQuery.ORDER)]();
    sortingArr.+=((SCORE_SORT, ORDER.desc));
    sortingArr.+=((FUND_RATING, ORDER.desc));
    sortingArr.+=((FUND_D_NAME, ORDER.asc));
  }

  /**
    *
    * @param array
    * @return
    */
  def makeQueryFromArray(array: Array[String]): String = {
    val build: StringBuilder = new StringBuilder();
    array.foreach { string =>
      if (build.length() > 0 && string != null && string.trim().length() > 0) {
        build.append(AND_OPERATOR);
      }
      if (string != null && string.trim().length() > 0) {
        build.append(string.trim());
      }
    }
    return build.toString();
  }

  /**
    *
    * @param keyword
    * @return
    */
  def splitQuery(keyword: String): Array[String] = {
    if (keyword != null && keyword.length() > 0 && keyword.indexOf(AND_OPERATOR) != -1) {
      val array: Array[String] = keyword.split(AND_OPERATOR);
      return array;
    } else {
      if (keyword != null && keyword.trim().length() > 0) {
        val array = new Array[String](1);
        array(0) = keyword;
        return array;
      } else {
        return null;
      }
    }
  }


}