package controllers

import java.util.Date

import com.google.inject.Inject
import helpers.AuthenticatedAction
import models.TransactionJsonFormats._
import models.integration.enumerations.BuySellEnum
import models.{IRRData, Transaction, TransactionDetails}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repository.module.FolioRepository
import service.{MongoDbService, ReportService, UserService}
import utils.DateTimeUtils

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class IRRController @Inject() (implicit ec: ExecutionContext, mongoDbService: MongoDbService, reportService: ReportService,
                               auth: AuthenticatedAction, userService: UserService, folioRepository: FolioRepository) extends Controller {

  val logger , log = LoggerFactory.getLogger(classOf[IRRController])

  def getIRR = auth.Action.async { request =>

    userService.getUserObjectFromReq(request).flatMap { userObject =>
      reportService.getIRRFundDetails(userObject.get.userid.get).map (transactions =>{

        var data = ListBuffer[IRRData]()

        var totalPays = ListBuffer[Double]()
        var allDates = ListBuffer[Date]()

        for (transaction <- transactions) {

          val folio = transaction.folioNo
          val fund = transaction.fundName
          val trd = transaction.transactionList
          val payments = ListBuffer[Double]()
//          val payments = for (tr <- trd) yield tr.amount
          val dates = for (tr <- trd) yield tr.transDate

          var units = BigDecimal(0.0)
          var currValue = BigDecimal(0.0)
          for (tr <- trd){
            if (tr.transType == BuySellEnum.R){
              units -= tr.units
              payments.+= (tr.amount)
            }
            else {
              units += tr.units
              payments.+= (-tr.amount)
            }
          }
          if (units>0){
            val a = folioRepository.getCurrNavByFundId(transaction.FundId).map(dsdnav => {
              if (dsdnav > 0){
                val currentNav = dsdnav
                currValue = units*currentNav
                payments += currValue.toDouble
                dates += DateTimeUtils.getCurrentDate()
              }
            })
            Await.result(a, Duration.Inf)
          }

          totalPays.++=(payments)
          allDates.++=(dates)

          val xirr = new XirrDate().Newtons_method(0.1, payments.toArray, dates.toArray)
          val irr = IRRData(folio, fund, xirr)
          data += irr
        }
        val totalxirr = new XirrDate().Newtons_method(0.1, totalPays.toArray, allDates.toArray)
        data += IRRData("", "Overall Returns", totalxirr)


        Ok(Json.toJson(data.toList))
      })
    }
  }

}