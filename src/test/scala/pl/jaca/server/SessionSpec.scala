package pl.jaca.server

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.networking.ConnectionProxy.{WithState, UpdateState, ForwardPacket}
import pl.jaca.testutils.TypeMatchers
import pl.jaca.testutils.server.proxy.DummyPackets.DummyOutPacket
import pl.jaca.testutils.server.proxy.DummySession
import scala.concurrent.duration._
/**
 * @author Jaca777
 *         Created 2015-12-22 at 23
 */
class SessionSpec extends TestKit(ActorSystem("SessionSpec")) with WordSpecLike with Matchers with TypeMatchers  {
  
  "Session" should {
    "forward packets" in {
      val proxyProbe = new TestProbe(system)
      val session = new DummySession(proxyProbe.ref)
      val dummyPacket = new DummyOutPacket(123)
      session.write(dummyPacket)
      val msg = proxyProbe.expectMsgType[ForwardPacket](200 millis)
      msg.outPacket should be (dummyPacket)
    }

    "forward UpdateState action" in {
      val proxyProbe = new TestProbe(system)
      val session = new DummySession(proxyProbe.ref)
      val dummyAction = (_: Option[Any]) => null
      session.mapSessionState(dummyAction)
      val msg = proxyProbe.expectMsgType[UpdateState](200 millis)
      msg.f should be (dummyAction)
    }

    "forward WithState action" in {
      val proxyProbe = new TestProbe(system)
      val session = new DummySession(proxyProbe.ref)
      val dummyAction = (_: Option[Any]) => ()
      session.withSessionState(dummyAction)
      val msg = proxyProbe.expectMsgType[WithState](200 millis)
      msg.action should be (dummyAction)
    }
  }
}
