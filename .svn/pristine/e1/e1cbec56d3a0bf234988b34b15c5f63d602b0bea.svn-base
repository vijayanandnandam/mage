package repository.module

import javax.inject.Inject

import constants.DBConstants
import data.model.Tables.{Fccnd, FccndRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables.FccndRepo
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fincash on 31-01-2017.
  */
class CNDRepository @Inject()(implicit ec: ExecutionContext, protected val dbConfigProvider: DatabaseConfigProvider, fccndRepo: FccndRepo) extends HasDatabaseConfigProvider[JdbcProfile] with DBConstants{


  def getCndsByParent(id: Long, childGroup:String) : Future[Option[Seq[FccndRow]]] = {
    fccndRepo.filter(x => {
      x.cndcndrfnum === id && x.cndgroup === childGroup
    }).map(values => {
      if(values.nonEmpty){
        Some(values)
      }else{
        None
      }
    })
  }

  def getCnd(id: Long): Future[Option[FccndRow]] = {
    fccndRepo.getById(id).map(value =>{
      if(value.nonEmpty){
        value.headOption
      }else{
        None
      }
    })
  }

  def getCndByCndName(cndName: String, cndGroup: Option[String]): Future[Option[FccndRow]] = {
    if(cndGroup.nonEmpty){
      fccndRepo.filter(x => {
        x.cndname === cndName && x.cndgroup === cndGroup
      }).map(value => {
        if(value.nonEmpty){
          value.headOption
        }else{
          None
        }
      })
    }else{
      fccndRepo.filter(x => {
        x.cndname === cndName
      }).map(value => {
        if(value.nonEmpty){
          value.headOption
        }else{
          None
        }
      })
    }

  }

  def getCndGroup(group: String): Future[Option[Seq[FccndRow]]] = {
    val query = Fccnd.filter(x => (x.cndgroup === group) && (x.cndactive === Y_FLAG)).map(value => value).sortBy(_.cndsequence.asc.nullsLast).result
    db.run(query).map(value => {
      if(value.nonEmpty){
        Some(value)
      }else{
        None
      }
    }).recover {
      case ex: Exception => throw ex
    }
  }

  def getGroupNameFromCndId(cndid: Long): Future[Option[String]] = {
    getCnd(cndid).map(value=>{
      if(value.nonEmpty){
        Some(value.get.cndgroup)
      }else{
        None
      }
    })
  }
}
