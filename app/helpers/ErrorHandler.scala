package helpers

import org.slf4j.LoggerFactory
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.mvc.Results.{InternalServerError, Status}

import scala.concurrent.Future

//@Singleton
class ErrorHandler extends HttpErrorHandler {
  val logger, log = LoggerFactory.getLogger(classOf[ErrorHandler])
  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    logger.error(message + " << statusCode >>" + statusCode+" request path >>> " + request.path);
    Future.successful(
      Status(statusCode)(Json.obj("success" -> false, "error" -> ("A client error occurred: " + message), "message" -> message))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    //exception.printStackTrace()
    logger.error("Error",exception)
    logger.error("request path >>> " + request.path + " exception >>> " +exception.getMessage)
    Future.successful(
      InternalServerError(Json.obj("success" -> false, "error" -> ("A server error occurred: " + exception.getMessage), "message" -> exception.getMessage))
    )
  }
}