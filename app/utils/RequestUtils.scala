package utils

import org.slf4j.LoggerFactory
import play.api.mvc.Request

/**
  * Created by fincash on 22-02-2017.
  */
object RequestUtils {
  val logger, log = LoggerFactory.getLogger(getClass)

  def getIpAddress(request: Request[Any]): String = {


    /*
    below logic is to handle mod-proxy request which are forwarded in nature.
    X-Forwarded-For
    The IP address of the client.
    X-Forwarded-Host
    The original host requested by the client in the Host HTTP request header.
      X-Forwarded-Server
  */
    val XforwardedFor = request.headers.get("X-Forwarded-For")
    val XforwardedHost = request.headers.get("X-Forwarded-Host")
    val XforwardedServer = request.headers.get("X-Forwarded-Server")

    if(XforwardedFor.nonEmpty){
      this.logger.debug("request ip address >>> " + XforwardedFor.get)
      return XforwardedFor.get
    }

    if(XforwardedHost.nonEmpty){
      this.logger.debug("request for host >>> " + XforwardedHost.get)
    }

    if(XforwardedServer.nonEmpty){
      this.logger.debug("request for Server >>> " + XforwardedServer.get)
    }

    request.remoteAddress
  }

}
