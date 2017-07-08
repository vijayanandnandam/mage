package actors

import akka.actor._

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! ("I got your message: " + msg)

//    case _ => {
//      Thread.sleep(200)
//      (1 to 1000).foreach(out ! _)
//    }
  }
}