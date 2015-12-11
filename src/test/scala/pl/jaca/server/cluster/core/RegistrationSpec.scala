package pl.jaca.server.cluster.core


import akka.actor.{ActorSystem, Address}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.cluster.distribution.Receptionist.PreciseSelectionStrategy
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Register, RegisteredMember}
import pl.jaca.testutils.server.ClusterTools

/**
 * @author Jaca777
 *         Created 2015-09-06 at 13
 */
class RegistrationSpec extends TestKit(ActorSystem("RegistrationSpec")) with WordSpecLike with Matchers with ClusterTools {

  class DummyRegister extends DummyActor with pl.jaca.server.cluster.distribution.Register

  "PreciseSelectionStrategy" must {
    "select the least loaded node 1" in {
      val member = new RegisteredMember(createClusterMember(new Address("a", "b")), AbsoluteLoad(0.0f))
      val members = Set(
        new RegisteredMember(createClusterMember(new Address("a", "a")), AbsoluteLoad(3.0f)),
        member,
        new RegisteredMember(createClusterMember(new Address("a", "c")), AbsoluteLoad(2.0f)))
      PreciseSelectionStrategy.apply(members) should be (member)
    }

    "select the least loaded node 2" in {
      val member = new RegisteredMember(createClusterMember(new Address("a", "b")), AbsoluteLoad(8.0f))
      val members = Set(
        new RegisteredMember(createClusterMember(new Address("a", "a")), AbsoluteLoad(10.0f)),
        new RegisteredMember(createClusterMember(new Address("a", "a")), AbsoluteLoad(10.0f)),
        new RegisteredMember(createClusterMember(new Address("a", "a")), AbsoluteLoad(9.0f)),
        member,
        new RegisteredMember(createClusterMember(new Address("a", "a")), AbsoluteLoad(30.0f)),
        new RegisteredMember(createClusterMember(new Address("a", "c")), AbsoluteLoad(12.0f)))
      PreciseSelectionStrategy.apply(members) should be(member)
    }
  }

  "Registration" must {

    val registration = TestActorRef(new DummyRegister).underlyingActor.asInstanceOf[Register]

    "return valid RegisteredMember" in {
      val member = createClusterMember(new Address("a", "a"))
      val registered1 = registration.register(member).get
      val registered2 = registration.unregister(member).get
      registered1 should be(registered2)
    }

    "register and unregister new members" in {
      registration.register(createClusterMember(new Address("a", "a")))
      val member = registration.register(createClusterMember(new Address("a", "b"))).get
      registration.registeredMembers.size should be(2)
      registration.unregister(member)
      registration.registeredMembers.size should be(1)
    }
  }

}
