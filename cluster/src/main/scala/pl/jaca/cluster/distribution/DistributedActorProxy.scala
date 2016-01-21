package pl.jaca.cluster.distribution

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.jaca.cluster.Configurable

import scala.concurrent.Future
import scala.util.{Failure, Success}

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

  if (!lazyDistr) evalRef()

  def evalRef() = {
    actorRef andThen {
      case Success(ref) =>
        self ! Evaluated(ref)

      case Failure(e) =>
        log.error(e, "Error occurred on distributing actor")
        if (!retry) context become failed
    }
  }

  override def receive: Receive = proxy(None)

  private case class Evaluated(distrActor: ActorRef)

  def proxy(distrActor: Option[ActorRef]): Receive = {
    case Evaluated(ref) => context become proxy(Some(ref))

    case msg if distrActor.isDefined =>
      val evaluatedRef = distrActor.get
      val currSender = sender()
      evaluatedRef.tell(msg, currSender)

    case msg if distrActor.isEmpty =>
      val evaluatedRef = evalRef()
      val currSender = sender()
      evaluatedRef.foreach {
        Thread.sleep(250) //await initialization
        _.tell(msg, currSender)
      }
  }

  def failed: Receive = {
    case msg =>
      log.error(s"Unable to deliver message $msg to distributed actor. Distribution failed.")
  }
}
