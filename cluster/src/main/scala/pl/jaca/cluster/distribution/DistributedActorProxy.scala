package pl.jaca.cluster.distribution

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.jaca.cluster.Configurable

import scala.concurrent.Future

/**
  * @author Jaca777
  *         Created 2016-01-20 at 12
  *         Proxy between distributor and distributed actor.
  *         Allows lazy evaluation of distributed actor.
  */
class DistributedActorProxy(actorRef: => Future[ActorRef]) extends Actor with Configurable with ActorLogging {

  implicit val ec = context.dispatcher

  val lazyDistr = appConfig.boolAt("server-app.distribution.lazy").getOrElse(false)
  val retry = appConfig.boolAt("server-app.distribution.retry").getOrElse(true)

  if (!lazyDistr) evalRef

  lazy val evalRef = {
    actorRef onFailure {
      case e =>
        log.error(e, "Error occurred on distributing actor")
        if (!retry) context become failed
    }
    Thread.sleep(250) //await initialization
    actorRef
  }

  override def receive: Receive = proxy

  def proxy(): Receive = {
    case msg =>
      val currSender = sender()
      evalRef.foreach {
        _.tell(msg, currSender)
      }
  }

  def failed: Receive = {
    case msg =>
      log.error(s"Unable to deliver message $msg to distributed actor. Distribution failed.")
  }
}
