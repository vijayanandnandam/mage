package controllers

import com.google.inject.Inject
import helpers.{AuthenticatedAction, SolrIndexHelper}
import models.FundDoc
import models.FundsJsonFormats._
import play.api.libs.json.Json
import play.api.mvc.Controller
import service.ProductService

import scala.concurrent.ExecutionContext

class CashController @Inject() (implicit val ec: ExecutionContext, indexHelper: SolrIndexHelper, auth: AuthenticatedAction,
                                productService: ProductService) extends Controller {

  var options = Seq("Divendend Re-investment", "Growth")
 
  def getSavingsPlusFunds = auth.Action.async { request =>
    productService.getSavingsPlusFunds().map(funds => {
      if (funds.nonEmpty) {
        Ok(Json.toJson(funds));
      } else {
        Ok(Json.obj("success" -> "false", "message" -> "Funds Not found"))
      }
    })
  }
}
