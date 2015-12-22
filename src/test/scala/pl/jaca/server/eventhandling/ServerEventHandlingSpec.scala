package pl.jaca.server.eventhandling

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.networking.ServerEvent.ConnectionActive
import pl.jaca.testutils.CollectionMatchers
import pl.jaca.testutils.server.proxy.DummySession
import pl.jaca.testutils.server.proxy.DummyPackets.DummyInPacket

import scala.concurrent.duration._

/**
 * @author Jaca777
 *         Created 2015-12-11 at 18
 */
class ServerEventHandlingSpec extends TestKit(ActorSystem("ServerEventHandlingSpec")) with ImplicitSender with WordSpecLike with Matchers with CollectionMatchers {

  var messages: Set[String] = Set()

  def out(s: String): Unit = {
    messages += s
  }

  class TestPacketA extends DummyInPacket(2)

  class TestEventActor1 extends EventActor with ServerEventHandling {
    val events = AsyncEventStream()
    events.packets react {
      case t: TestPacketA => Action {
        out(t.id.toString)
      }
      case t: ConnectionActive => Action {
        out(t.con.host)
      }
    }
  }

  val testEventActor1 = TestActorRef(new TestEventActor1)
  val testEventActor2 = TestActorRef(new TestEventActor2)
  val dummyConnectionActive = ConnectionActive(new DummySession("abc"))
  val dummyPacket = new TestPacketA

  class TestEventActor2 extends EventActor with ServerEventHandling {
    val events = AsyncEventStream()
    events.serverEvents react {
      case t: TestPacketA => Action {
        out(t.id.toString)
      }
      case t: ConnectionActive => Action {
        out(t.con.host)
      }
    }
  }

  "Class extending ServerEventHandling" must {
    "filter events with AsyncEventStream.packets" in {
      messages = Set.empty
      testEventActor1 ! dummyPacket
      testEventActor1 ! dummyConnectionActive
      within(50 millis) {
        messages should be(Set(dummyPacket.id.toString))
      }
    }

    "filter events with AsyncEventStream.serverEvents" in {
      messages = Set.empty
      testEventActor2 ! dummyPacket
      testEventActor2 ! dummyConnectionActive
      within(50 millis) {
        messages should be(Set(dummyConnectionActive.con.host))
      }
    }
  }

}
