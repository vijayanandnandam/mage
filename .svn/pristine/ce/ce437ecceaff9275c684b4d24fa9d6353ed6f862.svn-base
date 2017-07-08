package security

import com.google.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import service.JwtService
import utils.DateTimeUtils

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider._

/**
  * Created by Fincash on 21-03-2017.
  */

class FincashDataHandler @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, implicit val ec: ExecutionContext,
                                    jwtService: JwtService) extends DataHandler[User] {

  def validateClient(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = ???
  /*{
    val clientCredential = request.parseClientCredential
    if (clientCredential.nonEmpty){
      if (clientCredential.get.isRight){
        if (clientCredential.get.right == maybeClientCredential){
          true
        }
        else{
          false
        }
      }
      else {
        false
      }
    }
  }*/

  def findUser(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] = ???

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = {
    val username = Some(authInfo.user.username)
    jwtService.createUserToken(username).map(token => {
      new AccessToken(token, None, None, Some(1000), DateTimeUtils.getCurrentDate())
    })
  }

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = ???

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = ???/*{
    if (isRefreshTokenValid(refreshToken)){
      jwtService.createUserToken(Some(authInfo.user.name)).map(token => {
        new AccessToken(token, Some(refreshToken), None, Some(300), DateTimeUtils.getCurrentDate())
      })
    }
  }*/

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = ???

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = ???

  def deleteAuthCode(code: String): Future[Unit] = ???

  def findAccessToken(token: String): Future[Option[AccessToken]] = ???

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = ???

}