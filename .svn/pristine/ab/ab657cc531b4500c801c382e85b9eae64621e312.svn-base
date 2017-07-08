package controllers

import javax.inject.Inject

import constants.MongoConstants
import helpers.AuthenticatedAction
import models.UserLoginJsonFormats._
import models.UserLoginObject
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
import reactivemongo.bson.BSONDocument
import service.{JwtService, MongoDbService}

import scala.concurrent.{ExecutionContext, Future}


class JWTController @Inject()(implicit ec: ExecutionContext, mongoDbService: MongoDbService, auth: AuthenticatedAction, jwtService : JwtService,
                              configuration: play.api.Configuration) extends Controller with MongoConstants {

  val logger, log = LoggerFactory.getLogger(classOf[JWTController])

  def collection(name: String) = mongoDbService.collection(name)

  def getUserDataFromToken = auth.Action.async { request => {
      val token = request.token
    jwtService.isTokenValid(token).flatMap(status => {
        if(status){
          collection(TOKEN_COLLECTION_NAME).flatMap( coll =>{
            mongoDbService.findOneDoc(coll,BSONDocument("token" -> token)).map(results => {
              if(results.nonEmpty){
                val document =  results.get
                val userObject = new UserLoginObject(document.getAs[String]("username"),document.getAs[Long]("userid"),
                  document.getAs[Long]("foliocount"), document.getAs[String]("firstname"),document.getAs[String]("pan"),document.getAs[String]("mobile"))
                val userData  = Json.toJson(userObject)/*.as[JsObject]*/
                Ok(Json.obj("id_token" -> token,"userData" -> userData, "success" -> true))
              }else{
                Ok(Json.obj("id_token" -> token, "success" -> false))
              }
            })
          })
        }else{
          Future.apply(Ok(Json.obj("id_token" -> token, "success" -> false, "error" -> "Invalid Token", "message" -> "Invalid Token")))
        }
      })
    }
  }

  def refreshToken = Action.async(parse.json) {request => {
    val token = (request.body \ "id_token").as[String]
      jwtService.createRefreshToken(token).map(newToken => {
        if (newToken.length>0){
          logger.debug("refresh token generated >>> ", newToken)
          Ok(Json.obj("success" -> true, "id_token" -> newToken))
        }
        else {
          Ok(Json.obj("success" -> false, "error" -> "Invalid Token", "message" -> "Invalid Token"))
        }
      })


    /*jwtService.createRefreshToken(token).flatMap(futurereceivedToken => {
      futurereceivedToken.flatMap(receivedToken => {
        logger.debug("jwtService.createRefreshToken >>> new token >>> " + receivedToken)
        block(new AuthenticatedRequest(token, request)).map(result => setHeader(result, receivedToken))
      })
    })*/
  }}
}