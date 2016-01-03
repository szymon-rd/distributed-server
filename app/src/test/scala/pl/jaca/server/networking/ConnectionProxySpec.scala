package pl.jaca.server.networking

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit._
import io.netty.channel.ChannelFuture
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.networking.SessionProxy.{UpdateState, WithState, ForwardPacket}
import pl.jaca.server.testing.{DummyPackets, DummyNettyChannel}
import DummyPackets.DummyOutPacket
import pl.jaca.util.testing.AkkaTools
import scala.concurrent.duration._

/**
 * @author Jaca777
 *         Created 2015-12-04 at 17
 */
class ConnectionProxySpec extends TestKit(ActorSystem("ConnectionProxySpec")) with ImplicitSender with WordSpecLike with Matchers with AkkaTools {

  class TestChannel extends DummyNettyChannel(0, 0) {
    var packetsSent: List[Any] = List.empty[Any]

    override def writeAndFlush(packet: Any): ChannelFuture = {
      packetsSent ::= packet
      null
    }
  }

  def getState(proxy: ActorRef) = {
    val testProbe = new TestProbe(system)
    proxy ! WithState(state => testProbe.ref ! state)
    testProbe.receiveOne(200 millis)
  }


  "ConnectionProxy" must {
    "forward packets" in {
      val channel = new TestChannel
      val packet1 = new DummyOutPacket
      val packet2 = new DummyOutPacket
      val packet3 = new DummyOutPacket
      val proxy = TestActorRef(new SessionProxy(channel))
      proxy ! ForwardPacket(packet1)
      proxy ! ForwardPacket(packet2)
      proxy ! ForwardPacket(packet3)
      channel.packetsSent should be(List(packet3, packet2, packet1))
    }
    "have none state" in {
      val testProbe = new TestProbe(system)
      val channel = new DummyNettyChannel
      val proxy = TestActorRef(new SessionProxy(channel))
      getState(proxy) should be(None)
    }
    "update state" in {
      val testProbe = new TestProbe(system)
      val channel = new DummyNettyChannel
      val proxy = TestActorRef(new SessionProxy(channel))
      proxy ! UpdateState(_ => 2)
      proxy ! UpdateState {
        case Some(i: Int) => i + 1
      }
      getState(proxy) should be(Some(3))
    }
    "perform actions with state" in {
      val testProbe = new TestProbe(system)
      val channel = new DummyNettyChannel
      val proxy = TestActorRef(new SessionProxy(channel))
      var t = "test"
      proxy ! UpdateState(_ => "abc")
      proxy ! WithState(s => t += s.get)
      within(100 millis) {
        t should be ("testabc")
      }
    }

  }

}
