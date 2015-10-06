package pl.jaca.server.cluster.core


import akka.actor.{ActorSystem, Address}
import akka.testkit.{TestKit, TestActorRef}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.cluster.distribution.Register

/**
 * @author Jaca777
 *         Created 2015-09-06 at 13
 */
class RegistrationSpec extends TestKit(ActorSystem("Testsystem")) with WordSpecLike with Matchers with ClusterTools {

  class DummyRegister extends DummyActor with pl.jaca.server.cluster.distribution.Register

  "Registration" must {

    "return valid RegisteredMember" in {
      val registration = new DummyRegister
      val member = createClusterMember(new Address("a", "a"))
      val registered1 = registration.register(member)
      val registered2 = registration.unregister(member)
      registered1 should be (registered2.get)
    }

    "register and unregister new members" in {
      val registration = TestActorRef[DummyRegister].underlyingActor.asInstanceOf[Register]
      registration.register(createClusterMember(new Address("a", "a")))
      val member = registration.register(createClusterMember(new Address("a", "b"))).get
      registration.registeredMembers.count(_.clusterMember.address.system == "b") should be (1)
      registration.unregister(member)
      registration.registeredMembers.count(_.clusterMember.address.system == "b") should be (0)
      registration.registeredMembers.size should be(1)
    }
  }
}
