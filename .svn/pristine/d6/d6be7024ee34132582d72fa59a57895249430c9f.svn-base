package controllers

import javax.inject.Inject

import data.model.Tables.{FcactRow, FcactRowWrapper}
import org.slf4j.LoggerFactory
import play.api.mvc.{Action, Controller}
import repository.tables.FcactRepo
import service.{ApplicationConstantService, ReportService}
import slick.jdbc.MySQLProfile.api._
import utils.DateTimeUtils

import scala.concurrent.ExecutionContext.Implicits.global

class TestController extends Controller {

  val logger, log = LoggerFactory.getLogger(classOf[TestController])

  def healthCheck() = Action {
    Ok("Fincash.com!");
  }
}