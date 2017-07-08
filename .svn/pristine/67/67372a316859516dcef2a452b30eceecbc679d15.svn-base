package security

import scalaoauth2.provider._

/**
  * Created by Fincash on 21-03-2017.
  */
class FincashTokenEndpoint extends TokenEndpoint {
  override val handlers = Map(
    OAuthGrantType.AUTHORIZATION_CODE -> new AuthorizationCode(),
    OAuthGrantType.REFRESH_TOKEN -> new RefreshToken(),
    OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials(),
    OAuthGrantType.PASSWORD -> new Password(),
    OAuthGrantType.IMPLICIT -> new Implicit()
  )
}
