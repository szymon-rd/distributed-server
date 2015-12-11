package pl.jaca.testutils

import akka.actor.Actor
import akka.testkit.TestProbe

/**
 * @author Jaca777
 *         Created 2015-09-10 at 17
 */
trait AkkaTools {
  class DummyActor extends Actor {
    override def receive: Receive = {
      case _ =>
    }
  }

  implicit class TestProbeImplicit(testProbe: TestProbe) {
    def ignoreAll() {
      testProbe.ignoreMsg({case _ => true})
    }
  }
}
