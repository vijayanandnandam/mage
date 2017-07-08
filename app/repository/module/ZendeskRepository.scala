package repository.module

import javax.inject.{Inject, Singleton}

import constants.{IntegrationConstants, ZendeskConstants}
import data.model.Tables.{Fcotpt, Fctkt, FctktRow, FctktRowWrapper, Fctkth, FctkthRowWrapper}
import org.slf4j.LoggerFactory
import org.zendesk.client.v2.model.Status
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repository.tables.{FctktRepo, FctkthRepo}
import slick.jdbc.JdbcProfile
import utils.DateTimeUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by fincash on 31-01-2017.
  */

@Singleton
class ZendeskRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                  fctktRepo: FctktRepo, fctkthRepo: FctkthRepo)
  extends IntegrationConstants with HasDatabaseConfigProvider[JdbcProfile] with ZendeskConstants{

  import profile.api._
  val logger, log = LoggerFactory.getLogger(classOf[ZendeskRepository])

  def updateTicketInDatabase(tktrfnum : Long, userName :  String): Future[Boolean] = {

    /*val query = for{
      tktList <- Fctkt.filter(x => (x.id === tktrfnum)).map( x => x).forUpdate.result
      tktUpdated <- tktList.size match{
        case 0 => DBIO.successful(1)
        case n => Fctkt.update(tktList.head.copy(modifydate = DateTimeUtils.getCurrentTimeStamp())).map( x => x)
      }
    }yield(tktList)
    db.run(query.transactionally).map(values => {
      true
    })*/


    val query = {
      sql"""SELECT TKTRFNUM FROM FCTKT WHERE TKTRFNUM = ${tktrfnum} FOR UPDATE""".as[Long]
    } andThen {
      sqlu"""UPDATE FCTKT SET MODIFYDATE = CURRENT_TIMESTAMP, LASTMODIFIEDBY = ${userName}  WHERE TKTRFNUM = ${tktrfnum}"""
    }
    db.run(query.transactionally).map(value => {
      true
    })
  }

  def getTicketsByUserIdANDpurpose(userId: Long, purpose: Option[Long]): Future[Seq[FctktRow]] = {
    if (purpose.nonEmpty) {
      val query = for{
        tktList <- Fctkt.filter(x => (x.tktubdrfnum === userId && x.tktcndpurpose === purpose.get)).map( x => x).sortBy(_.modifydate.desc).result
      }yield(tktList)
      db.run(query.transactionally).map(values => {
        values
      })
    } else {
      val query = for{
        tktList <- Fctkt.filter(x => x.tktubdrfnum === userId).map( x => x).sortBy(_.modifydate.desc).result
      }yield(tktList)
      db.run(query.transactionally).map(values => {
        values
      })
    }
  }




  def getTicketByUserIdANDorderId(userId: Long, subOrderId: Long): Future[Seq[FctktRow]] = {
    val query = for{
      tktList <- Fctkt.filter(x => (x.tktubdrfnum === userId && x.tktsotrfnum === subOrderId)).map( x => x).sortBy(_.modifydate.desc).result
    }yield(tktList)
    db.run(query.transactionally).map(values => {
      values
    })
  }

  def getTicketByOrderId(subOrderId: Long): Future[Seq[FctktRow]] = {
    val query = for{
      tktList <- Fctkt.filter(x => (x.tktsotrfnum === subOrderId)).map( x => x).sortBy(_.modifydate.desc).forUpdate.result
    }yield(tktList)
    db.run(query.transactionally).map(values => {
      values
    })
  }

  def updateTicketStatus(tktId: Long, fromStatus : String, toStatus : String) : Future[Boolean] = {
    fctktRepo.filter(x => x.tktticketid === tktId.toString).flatMap(x => {
      var fctkt = x.head
      val fromState = fctkt.tktstate.getOrElse(fromStatus)
      val toState = toStatus
      fctkt = fctkt.copy(tktstate = Some(toState), modifydate = DateTimeUtils.getCurrentTimeStamp)
      fctktRepo.updateById(fctkt.id, fctkt).flatMap(_retval => {
        fctkthRepo.filter(x => x.tkthtktrfnum === fctkt.id).flatMap(y => {
          var fctkth = y.head
          fctkth = fctkth.copy(tkthfromstate = Some(fromState), tkthtostate = Some(toState), modifydate = DateTimeUtils.getCurrentTimeStamp)
          fctkthRepo.updateById(fctkth.id, fctkth).map(res => {
            true
          })
        })
      })
    }) recover {
      case ex => {
        logger.error("{}", ex)
        false
      }
    }
  }

  def updateZendeskTicket(tktRow:FctktRow):Future[Int] = {
    val updatedTktRow = tktRow.copy(modifydate = DateTimeUtils.getCurrentTimeStamp, lastmodifiedby = SYSTEM_USER)
    fctktRepo.updateById(updatedTktRow.id,updatedTktRow)
  }

  def saveTicket(_tktId:String, _tktcndpurpose:Option[Long]=None, _userid:Long, _username:String, tktstate:String, tktsource:String,subOrderId:Option[Long]): Future[Option[FctktRow]] = {
    var fctktRow = new FctktRowWrapper(None,_tktId, _tktcndpurpose, Some(_userid), subOrderId, None, Some(tktstate), Some(tktsource)).get(_username)
    val fctkthRow = new FctkthRowWrapper(None, Some(tktstate), Some(tktstate), None, None,1).get(_username)
    val query = for {
      fctktnewinstance <- Fctkt returning Fctkt.map(_.id) into ((fctktObj, id) => fctktObj.copy(id = id)) += (fctktRow)
      fctkthnewinstance <- Fctkth returning Fctkth.map(_.id) into ((fctkthObj, id) => fctkthObj.copy(id = id)) += (fctkthRow.copy(tkthtktrfnum = fctktnewinstance.id))
    } yield (fctktnewinstance)
    db.run(query.transactionally).map( value => {
      Some(value)
    }) recover {
      case ex => {
        logger.error("{}", ex)
        None
      }
    }
  }

  def getTicketById(ticketId : Long) : Future[Option[FctktRow]] = {
    fctktRepo.getById(ticketId)
  }

  /**
    *
    * @param ticketId
    * @return
    */
  def getOrderByZendeskTktId(ticketId: String) : Future[Option[FctktRow]] = {
    val query = for {
      tktObj <- Fctkt.filter(x=>x.tktticketid === ticketId && x.tktsotrfnum.isDefined).sortBy(_.modifydate.desc).result
    } yield(tktObj)

    db.run(query).map(_.headOption)
  }
}
