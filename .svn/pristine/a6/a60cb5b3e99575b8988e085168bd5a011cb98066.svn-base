package service

import java.util.Base64
import javax.inject.{Inject, Singleton}

import constants.DBConstants
import org.slf4j.LoggerFactory
import repository.tables.FcactRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author fincash
  *
  */

@Singleton
class BasicSecurityService @Inject()(configuration: play.api.Configuration, fcactRepo: FcactRepo) extends DBConstants {

  val logger, log = LoggerFactory.getLogger(classOf[BasicSecurityService])

  def isServerUserValid(token: String): Future[Boolean] = {
    val userDetails = new String(Base64.getDecoder.decode(token), "UTF-8");
    //logger.debug(userDetails);
    fcactRepo.getById(SERVER_USER_PK).map(actRow => {
      if (actRow.nonEmpty) {
        actRow.get.actconstantvalue.equals(userDetails)
      } else {
        logger.info("Missing ACT row for server user")
        false
      }
    })
  }

}