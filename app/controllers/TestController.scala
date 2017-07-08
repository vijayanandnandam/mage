package controllers

import org.slf4j.LoggerFactory
import play.api.mvc.{Action, Controller}

class TestController extends Controller {

  val logger, log = LoggerFactory.getLogger(classOf[TestController])

  def healthCheck() = Action {
    logger.debug("Health Check Ok")
    Ok("Fincash.com!")
  }
}