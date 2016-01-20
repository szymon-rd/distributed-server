package pl.jaca.cluster.distribution

import akka.actor._
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.cluster.distribution.Distribution.DistributionInitializer
import pl.jaca.cluster.distribution.Receptionist.{AvailableWorker, GetAvailableWorker}
import pl.jaca.cluster.testing.ClusterTools
import pl.jaca.util.testing.CollectionMatchers

/**
 * @author Jaca777
 *         Created 2016-01-20 at 13
 */
class DistributionSpec extends TestKit(ActorSystem("ReceptionistSpec")) with ImplicitSender with WordSpecLike with Matchers with ClusterTools with CollectionMatchers{
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  var member = createClusterMember(new Address("localhost", "ReceptionistSpec"))
  val registeredMember = new RegisteredMember(member, AbsoluteLoad(0.0f))

  def actorCreator(p: Props) = TestActorRef(p)

  class FakeReceptionist extends Actor {
    override def receive: Receive = {
      case GetAvailableWorker => sender ! AvailableWorker(registeredMember)
    }
  }

  var created = false

  class FakeActor extends Actor with Distributable {
    created = true

    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class MockDistribution extends DummyActor with Distribution

  def setReceptionist(receptionist: ActorRef) = new DistributionInitializer {
    initReceptionist(receptionist)
  }

  "Distribution" should {
    "use lazy distribution if requested" in {
      val receptionist = TestActorRef(new FakeReceptionist)
      setReceptionist(receptionist)
      val distribution = TestActorRef(new MockDistribution).underlyingActor
      System.setProperty("app.config", "testconf1.conf")
      val ref = distribution.distribute(Props(new FakeActor), actorCreator)
      Thread.sleep(50)
      created should be(false)
      ref.foreach(_ ! 2)
      Thread.sleep(50)
      created should be(true)
    }

    "not use lazy distribution if it's disabled" in {
      val receptionist = TestActorRef(new FakeReceptionist)
      setReceptionist(receptionist)
      val distribution = TestActorRef(new MockDistribution).underlyingActor
      System.setProperty("app.config", "testconf2.conf")
      val ref = distribution.distribute(Props(new FakeActor), actorCreator)
      Thread.sleep(50)
      created should be(true)
    }
  }
}

