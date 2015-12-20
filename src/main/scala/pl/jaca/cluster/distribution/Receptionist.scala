package pl.jaca.cluster.distribution

import akka.actor.Actor
import akka.pattern._
import pl.jaca.cluster.core.Listener.{MemberUnavailable, MemberAvailable}
import pl.jaca.cluster.distribution.Receptionist._

import scala.concurrent.Future
import scala.util.Random

/**
 * @author Jaca777
 *         Created 2015-08-16 at 20
 */
class Receptionist(strategy: SelectionStrategy) extends Actor with Register {

  implicit val executionContext = context.dispatcher

  def receive: Receive = {
    case MemberAvailable(clusterMember) => if (!isRegistered(clusterMember)) clusterMember.register
    case MemberUnavailable(clusterMember) => if (isRegistered(clusterMember)) clusterMember.unregister
    case ListMembers => sender ! Members(registeredMembers)
    case GetAvailableWorker =>
      availableWorker.map(AvailableWorker).pipeTo(sender)
  }

  def availableWorker: Future[RegisteredMember] = {
    anyMember.map(_ => strategy(registeredMembers))
  }
}

object Receptionist {

  /**
   * Receptionist available worker selection strategy.
   */
  type SelectionStrategy = (Set[RegisteredMember] => RegisteredMember)

  /**
   * Randomly selects member.
   */
  object RandomSelectionStrategy extends SelectionStrategy {
    override def apply(members: Set[RegisteredMember]): RegisteredMember = members.toList(Random.nextInt(members.size))
  }

  /**
   * Selects member with the least load.
   */
  object PreciseSelectionStrategy extends SelectionStrategy {
    override def apply(members: Set[RegisteredMember]): RegisteredMember =
      members.tail.foldLeft((members.head.load, members.head))((acc, member) => acc match {
        case (load, current) => if (load > member.load) (member.load, member) else acc
      })._2
  }

  /**
   * Input message. When received, Receptionist responses with set of members.
   */
  object ListMembers

  /**
   * Input message. When received, Receptionist responses with cluster member that has capability to perform some work.
   */
  object GetAvailableWorker

  /**
   * Response to ListMembers message.
   * @param members Set of currently registered members.
   */
  case class Members(members: Set[RegisteredMember])

  /**
   * Response to GetAvailableWorker message.
   * @param worker Cluster member with ability to perform some work.
   */
  case class AvailableWorker(worker: RegisteredMember)

}

