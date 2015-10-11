package pl.jaca.server.cluster.core

import akka.actor.{ActorSystem, Address}
import akka.pattern._
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.cluster.core.Listener._
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Receptionist}
import pl.jaca.server.cluster.distribution.Receptionist._
import pl.jaca.testutils.CollectionMatchers

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

    val receptionist = TestActorRef(new Receptionist(RandomSelectionStrategy))
    val member1 = createClusterMember(new Address("a", "a1"))
    val member2 = createClusterMember(new Address("a", "a2"))
    val member3 = createClusterMember(new Address("a", "a3"))
    receptionist ! member1
    receptionist ! member2
    receptionist ! member3

    val members = Await.result(receptionist ? ListMembers, timeout.duration).asInstanceOf[Members].members.toList

    "correctly manage and list registered members" in {
      members.map(_.clusterMember) should containOnly(member1, member2)
      receptionist ! MemberUnavailable(member1)
      val newState = Await.result(receptionist ? ListMembers, timeout.duration).asInstanceOf[Members].members
      newState.map(_.clusterMember) should containOnly(member2)
    }

    "compare load of each members when available member is requested" in {
      (members(0).load, members(2).load, members(3).load) match {
        case (load1: AbsoluteLoad, load2: AbsoluteLoad, load3: AbsoluteLoad) =>
          load1.setLoad(0)
          load2.setLoad(1)
          load3.setLoad(2)
          Await.result(receptionist ? GetAvailableWorker, timeout.duration).asInstanceOf[AvailableWorker].worker should be(member1)
          load1.increase(AbsoluteLoad(3.0f))
          Await.result(receptionist ? GetAvailableWorker, timeout.duration).asInstanceOf[AvailableWorker].worker should be(member2)
      }
    }
  }
}
