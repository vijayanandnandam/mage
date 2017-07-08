package controllers

import javax.inject.Inject

import helpers.{SolrBankSearchHelper, SolrIndexHelper}
import models.{BankSearchQuery, BankSearchResult}
import models.JsonFormats._
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

class BankController @Inject()(implicit val ec: ExecutionContext, bankSearchHelper: SolrBankSearchHelper, indexHelper: SolrIndexHelper) extends Controller {
  val logger = LoggerFactory.getLogger(classOf[BankController])

  def searchBanks = Action(parse.json) { request =>
    val searchQuery = request.body.as[BankSearchQuery]
    logger.debug("searchQuery: " + searchQuery);
    val searchResults = bankSearchHelper.branchSearch(searchQuery).getResults()
    if (searchResults.size() > 0) {
      val out = bankSearchHelper.getResultsAsBankDoc(searchResults)
      Ok(Json.toJson(new BankSearchResult(searchResults.getNumFound, out)))
    } else {
      Ok(Json.toJson(new BankSearchResult(0, Seq.empty)))
    }
    //  Ok("done")

  }

  def addBanks = Action { request =>
    //indexHelper.addIds();
    indexHelper.addBankDocuments("D://BanksWithoutAddress.csv") /*("D://Ahmedabad Mercantile Cooperative Bank.xls")*/
    Ok("data added")
  }

}