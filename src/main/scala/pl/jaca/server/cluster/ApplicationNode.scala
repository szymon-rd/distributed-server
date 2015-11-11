package pl.jaca.server.cluster

import akka.actor.{Actor, ActorRef, Props}
import pl.jaca.server.cluster.distribution.Distribution.DistributionInitializer
import pl.jaca.server.cluster.distribution.Receptionist.PreciseSelectionStrategy
import pl.jaca.server.cluster.distribution.{Distribution, Receptionist}

/**
 * @author Jaca777
 *         Created 2015-08-16 at 20
 */
class ApplicationNode extends Actor with DistributionInitializer with Distribution {

  val receptionist = context.actorOf(Props(new Receptionist(PreciseSelectionStrategy)))
  setReceptionist(receptionist)

  override def receive: Receive = {
    case ApplicationNode.Launch(appFactory) =>
      val app = context.actorOf(Props(appFactory()))
      app ! Application.Launch
    case ApplicationNode.GetReceptionist => sender ! ApplicationNode.Receptionist(receptionist)
  }
}

object ApplicationNode {
  
  case class Launch(appFactory: () => _ <: Application)
  
  /**
   * Input message. When received, Node responses with object containing reference to receptionist actor.
   */
  object GetReceptionist

  /**
   * Response to GetReceptionist message.
   * @param receptionist Reference to receptionist actor.
   */
  case class Receptionist(receptionist: ActorRef)
}