package repository.module

import javax.inject.Inject

import constants.DBConstants
import data.model.Tables.FcactRow
import org.slf4j.LoggerFactory
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables.FcactRepo
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 06-04-2017.
  */
class ApplicationConstantRepository @Inject()(implicit ec: ExecutionContext,protected val dbConfigProvider: DatabaseConfigProvider,
                                              fcactRepo: FcactRepo) extends HasDatabaseConfigProvider[JdbcProfile] with DBConstants{

  val logger, log = LoggerFactory.getLogger(classOf[ApplicationConstantRepository])

  import profile.api._

  def getIsipAmcs():Future[Seq[FcactRow]] = {
    fcactRepo.filter(x => x.actconstantname === ISIP_AMC_KEY)
  }
}
