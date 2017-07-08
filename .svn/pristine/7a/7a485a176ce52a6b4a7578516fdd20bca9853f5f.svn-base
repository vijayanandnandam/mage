package repository.core

import org.slf4j.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.CanBeQueryCondition

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect._

/**
  * Created by sagar on 16/1/17.
  */

trait BaseRepositoryComponent[T <: BaseTable[E], E <: BaseEntity] {
  def getById(id: Long): Future[Option[E]]

  def getAll: Future[Seq[E]]

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Seq[E]]

  def save(row: E): Future[(E, Long)]

  def saveWithKey(row: E): Future[Int]

  def deleteById(id: Long): Future[Int]

  def updateById(id: Long, row: E): Future[Int]
}

trait BaseRepositoryQuery[T <: BaseTable[E], E <: BaseEntity] {


  val query: TableQuery[T]

  def getByIdQuery(id: Long) = {
    query.filter(_.id === id)
  }

  def getAllQuery = {
    query
  }

  def filterQuery[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]) = {
    query.filter(expr)
  }

  def saveWithKeyQuery(row: E) = {
    query += row
  }

  def saveQuery(row: E) = {

    for {
      rowObjectTuple <- query returning query.map(_.id) into ((obj, generatedId) => (obj, generatedId)) += row
    } yield (rowObjectTuple)
  }

  def deleteByIdQuery(id: Long) = {
    query.filter(_.id === id).delete
  }

  def updateByIdQuery(id: Long, row: E) = {
    query.filter(_.id === id).update(row)
  }

}


abstract class BaseRepository[T <: BaseTable[E], E <: BaseEntity : ClassTag](clazz: TableQuery[T]) extends BaseRepositoryQuery[T, E] with BaseRepositoryComponent[T, E] {
  protected val log, logger : Logger
  protected val dbConfigProvider : DatabaseConfigProvider
  val clazzTable: TableQuery[T] = clazz
  lazy val clazzEntity = classTag[E].runtimeClass
  val query: TableQuery[T] = clazz
  lazy val _db = dbConfigProvider.get[JdbcProfile].db

  def getAll: Future[Seq[E]] = {
    _db.run(getAllQuery.result) map {
      x => x
    } recover {
      case ex => logger.error("getAll failed while querying from database.")
        logger.error(ex.getMessage)
        throw new Exception("Error occured while querying all rows")
    }


  }

  def getById(id: Long): Future[Option[E]] = {
    _db.run(getByIdQuery(id).result.headOption) map {
      x => x
    } recover {
      case ex => logger.error("getById failed while querying from database.")
        logger.error(ex.getMessage)
        throw new Exception("Error occured while querying by id")
    }

  }

  def filter[C <: Rep[_]](expr: T => C)(implicit wt: CanBeQueryCondition[C]): Future[Seq[E]] = {

    _db.run(filterQuery(expr).result) recover {
      case ex => logger.error("filter failed while querying from database.")
        logger.error(ex.getMessage)
        throw new Exception("Error occured while querying with filter criteria")
    }

  }

  def save(row: E): Future[(E, Long)] = {
    _db.run(saveQuery(row).transactionally) map {
      x => x
    } recover {
      case ex => logger.error("save failed while inserting in database.")
        logger.error(ex.getMessage)
        throw new Exception("Error occured while saving the row")
    }

  }

  def saveWithKey(row: E): Future[Int] = {
    _db.run(saveWithKeyQuery(row).transactionally) map {
      x => x
    } recover {
      case ex => logger.error("save with key failed while inserting in database.")
        logger.error(ex.getMessage)
        throw new Exception("Error occured while saving the row")
    }

  }

  def updateById(id: Long, row: E) = {
    _db.run(updateByIdQuery(id, row).transactionally) map {
      x => x
    } recover {
      case ex => logger.error("updateById failed while updating in database.")
        logger.error(ex.getMessage)
        throw new Exception("Error occured while updating by id")
    }

  }

  def deleteById(id: Long) = {
    _db.run(deleteByIdQuery(id).transactionally) map {
      x => x
    } recover {
      case ex => logger.error("deleteById failed while deleting row in database.")
        logger.error(ex.getMessage)
        throw new Exception("Error occured while deleting by id")
    }

  }

}

