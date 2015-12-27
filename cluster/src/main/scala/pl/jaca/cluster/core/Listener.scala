package pl.jaca.cluster.core

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import pl.jaca.cluster.core.Listener.{MemberUnavailable, MemberAvailable}

/**
 * @author Jaca777
 *         Created 2015-08-16 at 22
 */
class Listener(val subscribers: Set[ActorRef]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[MemberUp])
  cluster.subscribe(self, classOf[MemberRemoved])

  override def receive: Receive = {
    case clusterState: CurrentClusterState =>
      for {
        subscriber <- subscribers
        member <- clusterState.members
      } subscriber ! MemberAvailable(member)
    case MemberUp(clusterMember) =>
      subscribers.foreach(_ ! MemberAvailable(clusterMember))
      log.info(s"New cluster member associated with remote system. Address: ${clusterMember.address}")
    case MemberRemoved(clusterMember, _) =>
      subscribers.foreach(_ ! MemberUnavailable(clusterMember))
      log.info(s"Member became unavailable. Address: ${clusterMember.address}")
  }

  override def postStop() {
    cluster.unsubscribe(self)
  }
}

object Listener {
  /**
   * This message is sent to each subscriber to determine that member is currently available.
   */
  case class MemberAvailable(member: Member)
  

  /**
   * This message is sent to each subscriber to determine that member is currently unavailable.
   */
  case class MemberUnavailable(member: Member)

}
