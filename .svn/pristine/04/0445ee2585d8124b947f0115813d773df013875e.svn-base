package service

import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

@Singleton
class ApplicationConstantService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val logger, log = LoggerFactory.getLogger(classOf[ApplicationConstantService])
  
}