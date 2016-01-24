package pl.jaca.server.eventhandling

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.networking.Event
import pl.jaca.server.networking.ServerEvent.SessionEvent
import pl.jaca.server.packets.InPacket
import pl.jaca.server.testing.DummyPackets.DummyInPacket

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-09-06 at 13
 */
class EventActorSpec extends TestKit(ActorSystem("EventActorSpec")) with ImplicitSender with WordSpecLike with Matchers {
  implicit val timeout = Timeout(2 seconds)

  case class TestEvent1(i: Int) extends Event

  case class TestEvent2(i: Int) extends Event

  case class TestEvent3(i: Int) extends Event

  case class TestEvent4() extends Event

  case class PacketEvent(i: Short) extends DummyInPacket(i)

  case class DummyServerEvent() extends SessionEvent(null)

  var messages: Set[String] = Set()

  def out(s: String): Unit = {
    messages += s
  }

  val testActor1 = TestActorRef(new TestEventActor)

  class TestEventActor extends EventActor {
    val stream = AsyncEventStream()

    stream react {
      case TestEvent1(i) => foo(i)
      case TestEvent2(i) => foo(i)
      case TestEvent3(i) => foo(i)
      case p: PacketEvent => foo(-1)
      case _: DummyServerEvent => foo(3)
    }

    stream.packets react {
      case p: PacketEvent => bar(p)
    }

    stream.sessionEvents react {
      case DummyServerEvent() => foo(3)
    }

    stream react {
      case TestEvent3(i) => foo(i + 3)
    }

    def foo(i: Int) = out(i.toString)

    def bar(p: InPacket) = out(p.id.toString)

  }

  "EventActor" must {
    "handle event" in {
      messages = Set.empty
      val testEvent = TestEvent1(1)
      testActor1 ! testEvent
      messages should be(Set("1"))
      within(100 millis) {
        testEvent.getAndHandle() should be(true)
      }
    }

    "handle packets" in {
      messages = Set.empty
      testActor1 ! PacketEvent(1)
      testActor1 ! PacketEvent(2)
      testActor1 ! PacketEvent(3)
      messages should be(Set("1", "2", "3"))
    }

    "handle session events" in {
      messages = Set.empty
      testActor1 ! PacketEvent(1)
      testActor1 ! PacketEvent(2)
      testActor1 ! PacketEvent(3)
      messages should be(Set("1", "2", "3"))
    }

    "filter events" in {
      messages = Set.empty
      testActor1 ! TestEvent2(1)
      messages should be(Set("1"))
    }

    "override event handlers" in {
      messages = Set.empty
      testActor1 ! TestEvent3(1)
      messages should be(Set("4"))
    }

    "do not throw exception on unhandled event" in {
      testActor1 ! TestEvent4() //shouldn't throw exception
    }


  }

}
