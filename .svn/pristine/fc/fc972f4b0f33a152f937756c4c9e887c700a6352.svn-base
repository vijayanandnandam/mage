package security

import javax.inject.Inject

import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scalaoauth2.provider.OAuth2Provider

/**
  * Created by Fincash on 21-03-2017.
  */
class OAuth2Controller @Inject()(implicit val ec: ExecutionContext, fincashTokenEndpoint :  FincashTokenEndpoint, fincashDataHandler: FincashDataHandler) extends Controller with OAuth2Provider {
  override val tokenEndpoint = fincashTokenEndpoint

  def accessToken = Action.async { implicit request =>
    issueAccessToken(fincashDataHandler)
  }
}
