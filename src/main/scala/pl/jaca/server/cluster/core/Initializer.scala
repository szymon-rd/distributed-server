package pl.jaca.server.cluster.core

import akka.actor.SupervisorStrategy.{Escalate, Stop}
import akka.actor._
import akka.cluster.Cluster
import akka.pattern._
import akka.util.Timeout
import pl.jaca.server.cluster.Node.GetReceptionist
import pl.jaca.server.cluster.core.Initializer.CreateListener
import pl.jaca.server.cluster.{Application, Node}
import pl.jaca.server.oldcluster.FatalClusterError

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-08-16 at 15
 */
class Initializer(application: () => _ <: Application) extends Actor {

  override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 1, withinTimeRange = Duration.Inf) {
    case FatalClusterError(message) => Stop
    case _ => Escalate
  }

  val cluster = Cluster(context.system)
  cluster.join(cluster.selfAddress)

  implicit val executionContext = context.dispatcher

  val clusterNode = context.actorOf(Props[Node])
  implicit val timeout = Timeout(2 seconds)
  (clusterNode ? GetReceptionist).mapTo[Node.Receptionist].map(msg => CreateListener(Set(msg.receptionist))).pipeTo(self)

  clusterNode ! Node.Launch(application)

  override def receive: Receive = {
    case CreateListener(subscribers) =>
      context.actorOf(Props(new Listener(subscribers)))
  }

}

object Initializer {
  /**
   * Creates new cluster state listener with provided subscribers.
   * @param subscribers Set of actor references. Actors in set are informed about major changes in cluster state.
   */
  case class CreateListener(subscribers: Set[ActorRef])
  
}
