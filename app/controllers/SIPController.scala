package controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import helpers.AuthenticatedAction
import play.api.libs.json.Json
import models.FundsJsonFormats.fundDocFormat
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Controller
import service.ProductService

import scala.concurrent.ExecutionContext

class SIPController @Inject()(implicit ec: ExecutionContext, auth: AuthenticatedAction, productService: ProductService)
  extends Controller {
  /**
    * This method is used to get the list of funds to be shown for quick sip
    *
    * @return
    */
  def getQuickSipFunds() = auth.Action.async { request =>
    productService.getQuickSipFunds().map(funds => {
      if (funds.nonEmpty) {
        Ok(Json.toJson(funds));
      } else {
        Ok(Json.obj("success" -> "false", "message" -> "Funds Not found"))
      }
    })
  }
}
