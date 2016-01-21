package pl.jaca.cluster.core

import akka.actor.{ActorSystem, Address}
import akka.pattern._
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.cluster.core.Listener._
import pl.jaca.cluster.distribution.Receptionist._
import pl.jaca.cluster.distribution.{AbsoluteLoad, Receptionist}
import pl.jaca.cluster.testing.ClusterTools
import pl.jaca.util.testing.CollectionMatchers

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-09-06 at 13
 */
class ReceptionistSpec extends TestKit(ActorSystem("ReceptionistSpec")) with ImplicitSender with WordSpecLike with Matchers with ClusterTools with CollectionMatchers {
  implicit val timeout = Timeout(2 seconds)



  "Receptionist actor" must {

    "collect available members" in {
      val receptionist = TestActorRef(new Receptionist(PreciseSelectionStrategy))
      val member1 = createClusterMember(new Address("a", "a1"))
      val member2 = createClusterMember(new Address("a", "a2"))
      val member3 = createClusterMember(new Address("a", "a3"))
      receptionist ! MemberAvailable(member1)
      receptionist ! MemberAvailable(member2)
      receptionist ! MemberAvailable(member3)

      val members = Await.result(receptionist ? ListMembers, timeout.duration).asInstanceOf[Members].members.toList
      members.map(_.clusterMember) should containOnly(member1, member2, member3)
    }

    "remove unavailable members" in {
      val receptionist = TestActorRef(new Receptionist(PreciseSelectionStrategy))
      val member1 = createClusterMember(new Address("a", "a1"))
      val member2 = createClusterMember(new Address("a", "a2"))
      val member3 = createClusterMember(new Address("a", "a3"))
      receptionist ! MemberAvailable(member1)
      receptionist ! MemberAvailable(member2)
      receptionist ! MemberAvailable(member3)
      receptionist ! MemberUnavailable(member2)

      val newState = Await.result(receptionist ? ListMembers, timeout.duration).asInstanceOf[Members].members
      newState.map(_.clusterMember) should be(Set(member1, member3))
    }



    "use selection strategy when worker is requested" in {
      val receptionist = TestActorRef(new Receptionist(PreciseSelectionStrategy))
      val member1 = createClusterMember(new Address("a", "a1"))
      val member2 = createClusterMember(new Address("a", "a2"))
      val member3 = createClusterMember(new Address("a", "a3"))
      receptionist ! MemberAvailable(member2)
      receptionist ! MemberAvailable(member3)
      receptionist ! MemberAvailable(member1)
      val members = Await.result(receptionist ? ListMembers, timeout.duration).asInstanceOf[Members].members.toList
      val lMember1 = members(0)
      val lMember2 = members(1)
      val lMember3 = members(2)
      val load1 = lMember1.load.asInstanceOf[AbsoluteLoad]
      val load2 = lMember2.load.asInstanceOf[AbsoluteLoad]
      val load3 = lMember3.load.asInstanceOf[AbsoluteLoad]
      load1.setLoad(2.0f)
      load2.setLoad(1.0f)
      load3.setLoad(3.0f)
      Await.result(receptionist ? GetAvailableWorker, timeout.duration).asInstanceOf[AvailableWorker].worker.clusterMember should be(lMember2.clusterMember)
      load2.setLoad(8.0f)
      load1.setLoad(4.0f)
      Await.result(receptionist ? GetAvailableWorker, timeout.duration).asInstanceOf[AvailableWorker].worker.clusterMember should be(lMember3.clusterMember)
    }

  }
}
