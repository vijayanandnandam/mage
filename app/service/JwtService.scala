package service

import javax.inject.{Inject, Singleton}

import constants.MongoConstants
import models.JsonFormats._
import org.slf4j.LoggerFactory
import pdi.jwt.{JwtAlgorithm, JwtJson}
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.errors.DatabaseException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author fincash
  *
  */

@Singleton
class JwtService @Inject()(mongoDbService: MongoDbService, configuration: play.api.Configuration) extends MongoConstants {

  val logger, log = LoggerFactory.getLogger(classOf[JwtService])

  def collection(name: String) = mongoDbService.collection(name)

  def createUserToken(name: Option[String]): Future[String] = {
    val userid = 0
    var username = ""
    if (name.isEmpty) {
      username = "Anonymous" + BSONObjectID.generate.stringify
    } else {
      username = name.get
    }
    val retvalTuple = mongoDbService.userDocument(Some(username), Some(userid), None, None, None, None, None, None)
    val tokenDoc = retvalTuple._1
    collection(TOKEN_COLLECTION_NAME).flatMap { coll =>
      val writeRes: Future[WriteResult] = mongoDbService.insertDoc(coll, tokenDoc)
      writeRes.onComplete {
        case Failure(e) => {
          logger.error("Mongo Error :: " + e.getMessage)
        }
        case Success(writeResult) => {
          logger.debug("successfully inserted document")
        }
      }
      writeRes.map(_ => {
        retvalTuple._3
      })
    }
  }

  //  def getRefreshToken(): Future[String] = {}


  def createRefreshToken(token: String): Future[String] = {
    var selector = BSONDocument("token" -> token)
    collection(TOKEN_COLLECTION_NAME).flatMap { coll =>
      coll.find(selector).one[BSONDocument].flatMap { doc =>
        if (doc.nonEmpty) {
          val document = doc.get
          val userTuple = mongoDbService.userDocument(
            document.getAs[String]("username"),
            document.getAs[Long]("userid"),
            document.getAs[Long]("foliocount"),
            document.getAs[String]("firstname"),
            document.getAs[String]("pan"),
            document.getAs[String]("mob"),
            document.getAs[String]("key"),
            document.getAs[String]("isVerified")
          )
          val writeRes: Future[UpdateWriteResult] = mongoDbService.updateDoc(coll, selector, userTuple._1)
          writeRes.onComplete {
            case Failure(e) => {
              logger.error("Mongo Error :: " + e.getMessage)
            }
            case Success(writeResult) => {
              logger.debug("successfully updated token store")
            }
          }
          val a = writeRes.map(_ => {
            userTuple._3
          })
          a
        } else {
          Future.apply("")
        }
      }
    }.recover {
      case dex: DatabaseException => throw dex
      case e: Throwable => throw e
    }
  }

  def invalidateToken(token : String) : Future[Boolean] = {
    collection(TOKEN_COLLECTION_NAME).flatMap(coll => {
      val deleteStatus = mongoDbService.findAndRemove(coll,BSONDocument("token" -> token)).map(_.result[BSONDocument])
      deleteStatus.map(doc =>{
        if(doc.nonEmpty){
          logger.debug("Deleted Document Successfully")
          true
        }else{
          logger.debug("Document not found for deletion")
          false
        }
      }).recover {
        case e: Exception => {
          false
        }
      }
  })

    /*
    collection(TOKEN_COLLECTION_NAME).flatMap { coll =>
      coll.find(findquery).one[BSONDocument].flatMap { doc =>
        if (doc.nonEmpty) {

        } else{
          logger.debug("Token Not Found in mongoDB");
          true
        }
      }
    }.recover {
      case dex: DatabaseException => throw dex
      case e: Throwable => throw e
    }*/
  }

  def isTokenValid(token: String): Future[(Boolean,Boolean)] = {
    val findquery = BSONDocument("token" -> token)
    collection(TOKEN_COLLECTION_NAME).flatMap { coll =>
      coll.find(findquery).one[BSONDocument].map { doc =>
        if (doc.nonEmpty) {
          val key = doc.get.getAs[String]("key").get
          val isValid  = JwtJson.isValid(token, key, JwtAlgorithm.allHmac()/*, new JwtOptions(true,true,true,10)*/)
          (isValid, false)
        } else{
          //logger.debug(" Token cannot be located [" + token+"]")
          (false,true)
        }
      }
    }.recover {
      case dex: DatabaseException => {
        logger.error("DatabaseException",dex)
        (false,true)
      }
      case e: Exception => {
        logger.error("Exception",e)
        (false,true)
      }
    }
  }

  def isAnonymousUser(token: String): Future[Boolean] = {
    val findquery = BSONDocument("token" -> token)
    collection(TOKEN_COLLECTION_NAME).flatMap { coll =>
      coll.find(findquery).one[BSONDocument].map { doc =>
        if (doc.nonEmpty) {
          val username = doc.get.getAs[String]("username").get
          if (username.contains("Anonymous")) {
            true
          }
          else {
            false
          }
        }
        else {
          throw new Exception("Token cannot be located")
        }
      }
    }.recover {
      case dex: DatabaseException => {
        logger.error("DatabaseException",dex)
        false
      }
      case e: Exception => {
        logger.error("Exception",e)
        false
      }
    }
  }

  def isTokenExpired(token: String): Future[Boolean] = {
    val findquery = BSONDocument("token" -> token)
    collection(TOKEN_COLLECTION_NAME).flatMap { coll =>
      coll.find(findquery).one[BSONDocument].map { doc =>
        if (doc.nonEmpty) {
          val expiryOption = doc.get.getAs[Long]("expiry")
          if (expiryOption.nonEmpty) {
            val expiry = expiryOption.get
            expiry <= System.currentTimeMillis // Returns True or false
          } else {
            true
          }
        } else {
          logger.debug("Token cannot be located ["+token+"]")
          true
        }

      }
    }.recover {
      case dex: DatabaseException => {
        logger.error("DatabaseException",dex)
        false
      }
      case e: Exception => {
        logger.error("Exception",e)
        false
      }
    }
  }
}