package controllers

import javax.inject.Inject

import helpers.AuthenticatedAction
import play.api.libs.json.Json
import play.api.mvc.Controller
import models.FundDetailJsonFormats._
import service.{PortfolioSummaryService, ReportService, SchemeService, UserService}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 06-02-2017.
  */
class DashboardController @Inject()(implicit ec: ExecutionContext, auth: AuthenticatedAction, portfolioSummaryService: PortfolioSummaryService,
                                    userService: UserService, reportService: ReportService, schemeService: SchemeService) extends Controller {

  def getCategoryWiseFunds() = auth.Action.async { request =>
    userService.getUsernameFromRequest(request).flatMap { userName =>
      userService.getUserByUserName(userName.get).flatMap { user =>
        if (user.nonEmpty){
          reportService.getAssetAllocationFundDetails(user.get.id).map { fundDetailsList =>

            portfolioSummaryService.calculateCurrentValue(fundDetailsList)
            val assetClassFundDetailsList = schemeService.mapSchemesToAssetClass(fundDetailsList)
            Ok(Json.toJson(assetClassFundDetailsList))
          }
        }
        else {
          Future.apply(Ok(Json.obj("success" -> false, "error" -> "user not found", "message" -> "user not found")))
        }
      }
    }
  }

  def getTopThreeFundPerformers() = auth.Action.async { request =>
    userService.getUsernameFromRequest(request).flatMap { userName =>
      userService.getUserByUserName(userName.get).flatMap { user =>
        if (user.nonEmpty){
          reportService.getAssetAllocationFundDetails(user.get.id).map { fundDetailsList =>

            val topFundsList = schemeService.getTopThreeSchemes(fundDetailsList)
            Ok(Json.toJson(topFundsList))
          }
        }
        else {
          Future.apply(Ok(Json.obj("success" -> false, "error" -> "user not found", "message" -> "user not found")))
        }
      }
    }
  }
}
