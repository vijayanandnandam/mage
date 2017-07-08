package controllers

import javax.inject.Inject

import actors.MyWebSocketActor
import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.Timeout
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.mvc.WebSocket._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Random
import play.api.mvc.WebSocket.MessageFlowTransformer

/**
  * Created by Fincash on 28-03-2017.
  */
class SocketTestController @Inject() (implicit system: ActorSystem, materializer: Materializer, implicit val ec: ExecutionContext) {

  val logger, log = LoggerFactory.getLogger(classOf[SocketTestController])
  val (publicOut,publicChannel) = Concurrent.broadcast[String]

  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[Int, Int]

  def socket = WebSocket.accept[Int, Int] { request =>
//    ActorFlow.actorRef(out => MyWebSocketActor.props(out))
    /*Flow[String]
      .map(x => Random.nextInt(10).toString)*/

    val in = publicChannel.push(Random.nextInt(10).toString)
    val out = Enumerator.repeat(publicOut)

    val source = Source(1 to 100).map(x => {Thread.sleep(200); x;}).concat(Source.maybe)
    val sink = Sink.foreach[Int](elem => {Thread.sleep(5000); println(s"sink received: $elem")})
    var flow = Flow[Int].map(x => {Thread.sleep(5000);(x, x)})

    /*val source = Source.fromFuture(
      var kycstatus = KYC_NOTDONE
      userService.getUseridFromRequest(request).map(userid => {
          kycRepository.getUserKYCStatus(userid.get.toLong).map(kycRowList => {
            if (kycRowList.isEmpty) {
              logger.error("Kyc Status for user pk " + userid.toString + " not found")
              kycstatus = KYC_NOTDONE
            } else {
              kycstatus = kycRowList.head.kycstatus
            }
            kycstatus
          })
      })
    )*/

    Flow.fromSinkAndSource(sink, source)
  }

  def chat = WebSocket.using[String]{ request =>
    val (privateOut,privateChannel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach{
      msg:String => if(msg.startsWith("@")){
        publicChannel.push("Broadcasted: " + msg)
      }else{
        privateChannel.push("Private: " + msg)
      }
    }
    val out = Enumerator.interleave(publicOut,privateOut)
    (in, out)
  }

  /*def ws: WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    case rh if sameOriginCheck(rh) =>
      wsFutureFlow(rh).map { flow =>
        Right(flow)
      }.recover {
        case e: Exception =>
          logger.error("Cannot create websocket", e)
          val jsError = Json.obj("error" -> "Cannot create websocket")
          val result = InternalServerError(jsError)
          Left(result)
      }

    case rejected =>
      logger.error(s"Request ${rejected} failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  def sameOriginCheck(rh: RequestHeader): Boolean = {
    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  def originMatches(origin: String): Boolean = {
    origin.contains("localhost:9000") || origin.contains("localhost:19001")
  }

  /**
    * Creates a Future containing a Flow of JsValue in and out.
    */
  def wsFutureFlow(request: RequestHeader): Future[Flow[JsValue, JsValue, NotUsed]] = {
    // create an actor ref source and associated publisher for sink
    val (webSocketOut: ActorRef, webSocketIn: Publisher[JsValue]) = createWebSocketConnections()

    // Create a user actor off the request id and attach it to the source
    val userActorFuture = createUserActor(request.id.toString, webSocketOut)

    // Once we have an actor available, create a flow...
    userActorFuture.map { userActor =>
      createWebSocketFlow(webSocketIn, userActor)
    }
  }

  /**
    * Creates a materialized flow for the websocket, exposing the source and sink.
    *
    * @return the materialized input and output of the flow.
    */
  def createWebSocketConnections(): (ActorRef, Publisher[JsValue]) = {

    // Creates a source to be materialized as an actor reference.
    val source: Source[JsValue, ActorRef] = {
      // If you want to log on a flow, you have to use a logging adapter.
      // http://doc.akka.io/docs/akka/2.4.4/scala/logging.html#SLF4J
      val logging = Logging(actorSystem.eventStream, logger.getName)

      // Creating a source can be done through various means, but here we want
      // the source exposed as an actor so we can send it messages from other
      // actors.
      Source.actorRef[JsValue](10, OverflowStrategy.dropTail).log("actorRefSource")(logging)
    }

    // Creates a sink to be materialized as a publisher.  Fanout is false as we only want
    // a single subscriber here.
    val sink: Sink[JsValue, Publisher[JsValue]] = Sink.asPublisher(fanout = false)

    // Connect the source and sink into a flow, telling it to keep the materialized values,
    // and then kicks the flow into existence.
    source.toMat(sink)(Keep.both).run()
  }

  /**
    * Creates a user actor with a given name, using the websocket out actor for output.
    *
    * @param name         the name of the user actor.
    * @param webSocketOut the "write" side of the websocket, that the user actor sends JsValue to.
    * @return a user actor for this ws connection.
    */
  def createUserActor(name: String, webSocketOut: ActorRef): Future[ActorRef] = {
    // Use guice assisted injection to instantiate and configure the child actor.
    val userActorFuture = {
      implicit val timeout = Timeout(10000.millis)
//      (userParentActor ? UserParentActor.Create(name, webSocketOut)).mapTo[ActorRef]
    }
    userActorFuture
  }

  /**
    * Creates a flow of events from the websocket to the user actor.
    *
    * When the flow is terminated, the user actor is no longer needed and is stopped.
    *
    * @param userActor   the user actor receiving websocket events.
    * @param webSocketIn the "read" side of the websocket, that publishes JsValue to UserActor.
    * @return a Flow of JsValue in both directions.
    */
  def createWebSocketFlow(webSocketIn: Publisher[JsValue], userActor: ActorRef): Flow[JsValue, JsValue, NotUsed] = {
    // http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#stream-materialization
    // http://doc.akka.io/docs/akka/current/scala/stream/stream-integrations.html#integrating-with-actors

    // source is what comes in: browser ws events -> play -> publisher -> userActor
    // sink is what comes out:  userActor -> websocketOut -> play -> browser ws events
    val flow = {
      val sink = Sink.actorRef(userActor, akka.actor.Status.Success(()))
      val source = Source.fromPublisher(webSocketIn)
      Flow.fromSinkAndSource(sink, source)
    }

    // Unhook the user actor when the websocket flow terminates
    // http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#watchTermination
    val flowWatch: Flow[JsValue, JsValue, NotUsed] = flow.watchTermination() { (_, termination) =>
      termination.foreach { done =>
        logger.info(s"Terminating actor $userActor")
//        stocksActor.tell(UnwatchStock(None), userActor)
//        actorSystem.stop(userActor)
      }
      NotUsed
    }

    flowWatch
  }*/
}
