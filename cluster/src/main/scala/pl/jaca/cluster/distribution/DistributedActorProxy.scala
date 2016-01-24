package pl.jaca.cluster.distribution

import akka.actor.{Actor, ActorLogging, Props}
import pl.jaca.cluster.Configurable

import scala.concurrent.Future

/**
  * @author Jaca777
  *         Created 2016-01-20 at 12
  *         Proxy between distributor and distributed actor.
  *         Allows lazy evaluation of distributed actor.
  */
class DistributedActorProxy(props: Future[Props]) extends Actor with Configurable with ActorLogging {

  implicit val ec = context.dispatcher

  val retry = appConfig.boolAt("server-app.distribution.retry").getOrElse(true)
  val lazyDistr = appConfig.boolAt("server-app.distribution.lazy").getOrElse(false)

  lazy val futureRef = {
    val targetRef = props.map {
      context.actorOf(_, "target")
    }
    targetRef.onFailure {
      case error =>
        log.error(error, "Error occured on actor distribution.")
        if (!retry) context become failed
    }
    Thread.sleep(250) //await initialization
    targetRef
  }

  if (!lazyDistr) futureRef

  override def receive: Receive = proxy

  def proxy(): Receive = {
    case msg =>
      val currSender = sender()
      futureRef.foreach {
        ref =>
          if (currSender == ref) context.parent.tell(msg, currSender)
          else ref.tell(msg, currSender)
      }
  }

  def failed: Receive = {
    case msg =>
      log.error(s"Unable to deliver message $msg to distributed actor. Distribution failed.")
  }
}
