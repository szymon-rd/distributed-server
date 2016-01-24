package pl.jaca.cluster.distribution

import akka.actor._
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.concurrent.Eventually._
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.cluster.distribution.Distribution.DistributionInitializer
import pl.jaca.cluster.distribution.Receptionist.{AvailableWorker, GetAvailableWorker}
import pl.jaca.cluster.testing.ClusterTools

/**
  * @author Jaca777
  *         Created 2016-01-20 at 13
  */
class DistributionSpec extends TestKit(ActorSystem("ReceptionistSpec")) with ImplicitSender with WordSpecLike with Matchers with ClusterTools {
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  var clusterMember = createClusterMember(new Address("localhost", "ReceptionistSpec"))
  val registeredMember = new RegisteredMember(clusterMember, AbsoluteLoad(0.0f))

  def actorCreator(p: Props, name: String) = TestActorRef(p, self)

  class FakeReceptionist extends Actor {
    override def receive: Receive = {
      case GetAvailableWorker => sender ! AvailableWorker(registeredMember)
    }
  }

  var created = false
  var receivedMsg = false

  object TestMessage

  object TestResponseRequest

  object Response

  object TestParentMsgRequest

  object ParentMsg

  class FakeActor extends Actor with Distributable {
    created = true


    override def receive: Actor.Receive = {
      case TestMessage => receivedMsg = true
      case TestResponseRequest => sender ! Response
      case TestParentMsgRequest => context.parent ! ParentMsg
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
      val ref = distribution.distribute(Props(new FakeActor), "dummy", actorCreator)
      Thread.sleep(50)
      created should be(false)
      ref ! 2
      Thread.sleep(50)
      created should be(true)
      created = false
    }

    "not use lazy distribution if it's disabled" in {
      val receptionist = TestActorRef(new FakeReceptionist)
      setReceptionist(receptionist)
      val distribution = TestActorRef(new MockDistribution).underlyingActor
      System.setProperty("app.config", "testconf2.conf")
      val ref = distribution.distribute(Props(new FakeActor), "dummy", actorCreator)
      Thread.sleep(50)
      created should be(true)
    }

    "forward messages to distributed actor" in {
      val receptionist = TestActorRef(new FakeReceptionist)
      setReceptionist(receptionist)
      val distribution = TestActorRef(new MockDistribution).underlyingActor
      System.setProperty("app.config", "testconf2.conf")
      val ref = distribution.distribute(Props(new FakeActor), "dummy", actorCreator)
      ref ! TestMessage
      eventually {
        receivedMsg should be(true)
      }
    }

    "forward messages from distributed actor" in {
      val receptionist = TestActorRef(new FakeReceptionist)
      setReceptionist(receptionist)
      val distribution = TestActorRef(new MockDistribution).underlyingActor
      System.setProperty("app.config", "testconf2.conf")
      val ref = distribution.distribute(Props(new FakeActor), "dummy", actorCreator)
      ref ! TestResponseRequest
      expectMsg(Response)
      ()
    }

    "not change parent-child actor hierarchy" in {
      val receptionist = TestActorRef(new FakeReceptionist)
      setReceptionist(receptionist)
      val distribution = TestActorRef(new MockDistribution).underlyingActor
      System.setProperty("app.config", "testconf2.conf")
      val ref = distribution.distribute(Props(new FakeActor), "dummy", actorCreator)
      ref ! TestParentMsgRequest
      expectMsg(ParentMsg)
      ()
    }
  }
}

