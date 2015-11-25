package pl.jaca.server.proxy.eventhandling

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.proxy.server.Event
import pl.jaca.testutils.CollectionMatchers

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-09-06 at 13
 */
class EventActorSpec extends TestKit(ActorSystem("EventActorSpec")) with ImplicitSender with WordSpecLike with Matchers with CollectionMatchers {
  implicit val timeout = Timeout(2 seconds)

  case class DummyEvent1(i: Int) extends Event

  case class DummyEvent2(s: String) extends Event

  case class DummyEvent3(b: Boolean) extends Event

  var messages: Set[String] = Set()

  def out(s: String): Unit = {
    messages += s
  }

  val testActor1 = TestActorRef(new TestEventActor)
  val testActor2 = TestActorRef(new TestEventActor2)

  class TestEventActor extends EventActor {
    val stream = AsyncEventStream()

    stream react {
      case DummyEvent1(i) =>
        Action {
          foo(i)
        }
      case DummyEvent2(s) => Route(testActor2)
      case DummyEvent3(b) => Ignore
    }

    def foo(i: Int) = out(s"Actor1 $i")

  }

  class TestEventActor2 extends EventActor {
    val stream = AsyncEventStream()

    stream react {
      case DummyEvent2(s) =>
        Action {
          bar(s)
        }
    }

    def bar(s: String) = out(s"Actor2 $s")
  }

  "EventActor" must {
    "handle events" in {
      testActor1 ! DummyEvent2("lol")
      testActor1 ! DummyEvent1(69)
      testActor1 ! DummyEvent2("lol")
      testActor1 ! DummyEvent2("lol")
      testActor1 ! DummyEvent2("lol")
      testActor1 ! DummyEvent3(false)
      messages should containAll ("Actor1 69", "Actor2 lol", "Actor2 lol", "Actor2 lol", "Actor2 lol")
    }
  }

}
