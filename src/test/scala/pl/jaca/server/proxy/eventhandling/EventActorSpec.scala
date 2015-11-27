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

  case class TestEvent1(i: Int) extends Event

  case class TestEvent2(s: String) extends Event

  case class TestEvent3(b: Boolean) extends Event

  var messages: Set[String] = Set()

  def out(s: String): Unit = {
    messages += s
  }

  val testActor1 = TestActorRef(new TestEventActor)
  val testActor2 = TestActorRef(new TestEventActor2)

  class TestEventActor extends EventActor {
    val stream = AsyncEventStream()

    stream react {
      case TestEvent1(i) =>
        Action {
          foo(i)
        }
      case TestEvent2(s) => Route(testActor2)
      case TestEvent3(b) => Ignore
    }

    def foo(i: Int) = out(s"Actor1 $i")

  }

  class TestEventActor2 extends EventActor {
    val stream = AsyncEventStream()

    stream react {
      case TestEvent2(s) => Action {
        bar(s)
      }
      case TestEvent3(b) => Action {
        bar(b.toString)
      }
    }

    def bar(s: String) = out(s"Actor2 $s")
  }

  "EventActor" must {
    "handle events" in {
      messages = Set.empty
      testActor1 ! TestEvent1(1)
      testActor1 ! TestEvent1(2)
      testActor1 ! TestEvent1(3)
      messages should be(Set("Actor1 1", "Actor1 2", "Actor1 3"))
    }
    "route events" in {
      messages = Set.empty
      testActor1 ! TestEvent2("a")
      testActor1 ! TestEvent1(9)
      testActor1 ! TestEvent2("b")
      messages should be(Set("Actor1 9", "Actor2 a", "Actor2 b"))
    }
    "ignore events" in {
      messages = Set.empty
      testActor1 ! TestEvent1(3)
      testActor1 ! TestEvent3(true)
      testActor1 ! TestEvent2("b")
      testActor1 ! TestEvent3(false)
      messages should be(Set("Actor1 3", "Actor2 b"))
    }
  }

}
