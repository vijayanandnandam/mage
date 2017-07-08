package controllers

import com.google.inject.Inject
import helpers.AuthenticatedAction
import models.FundsJsonFormats._
import play.api.libs.json.Json
import play.api.mvc.Controller
import service.ProductService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxController @Inject()(auth: AuthenticatedAction, productService: ProductService) extends Controller {

  /**
    * This method is used to get the list of funds to be shown for quick sip
    *
    * @return
    */
  def getTaxSaverFunds = auth.Action.async { request =>
    productService.getTaxSaverFunds().map(funds => {
      if (funds.nonEmpty) {
        Ok(Json.toJson(funds));
      } else {
        Ok(Json.obj("success" -> false, "message" -> "Funds Not found"))
      }
    })
  }
}
