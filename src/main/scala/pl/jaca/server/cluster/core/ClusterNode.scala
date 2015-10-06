package pl.jaca.server.cluster.core

import akka.actor.SupervisorStrategy.{Escalate, Stop}
import akka.actor.{Props, ActorRef, AllForOneStrategy, Actor}
import akka.util.Timeout
import akka.pattern._
import pl.jaca.server.cluster.Node
import pl.jaca.server.cluster.Node._
import pl.jaca.server.cluster.core.ClusterNode.CreateListener
import pl.jaca.server.oldcluster.FatalClusterError

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-10-06 at 17
 */
abstract class ClusterNode extends Actor {
  implicit val executionContext = context.dispatcher

  override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 1, withinTimeRange = Duration.Inf) {
    case FatalClusterError(message) => Stop
    case _ => Escalate
  }

  val clusterNode = context.actorOf(Props[Node])
  implicit val timeout = Timeout(2 seconds)
  val receptionist = (clusterNode ? GetReceptionist).mapTo[Receptionist] map (_.receptionist)
  receptionist map (receptionist => CreateListener(Set(receptionist))) pipeTo self

  override def receive: Receive = {
    case CreateListener(subscribers) =>
      context.actorOf(Props(new Listener(subscribers)))
  }
}
object ClusterNode {

  /**
   * Creates new cluster state listener with provided subscribers.
   * @param subscribers Set of actor references. Actors in set are informed about major changes in cluster state.
   */
  case class CreateListener(subscribers: Set[ActorRef])

}
