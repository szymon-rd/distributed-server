package pl.jaca.server.networking

import akka.actor.ActorSystem
import akka.testkit._
import io.netty.channel.ChannelFuture
import org.scalatest.{Matchers, WordSpecLike}
import ConnectionProxy.ForwardPacket
import pl.jaca.testutils.AkkaTools
import pl.jaca.testutils.server.proxy.DummyPackets.DummyOutPacket
import pl.jaca.testutils.server.proxy.DummyNettyChannel

/**
 * @author Jaca777
 *         Created 2015-12-04 at 17
 */
class ConnectionProxySpec  extends TestKit(ActorSystem("ConnectionProxySpec")) with ImplicitSender with WordSpecLike with Matchers with AkkaTools {
  class TestChannel extends DummyNettyChannel(0, 0) {
    var packetsSent: List[Any] = List.empty[Any]
    override def writeAndFlush(packet: Any): ChannelFuture = {
      packetsSent ::= packet
      null
    }
  }
  
  "ConnectionProxy" must {
    "forward packets" in {
      val channel = new TestChannel
      val packet1 = new DummyOutPacket
      val packet2 = new DummyOutPacket
      val packet3 = new DummyOutPacket
      val proxy = TestActorRef(new ConnectionProxy(channel))
      proxy ! ForwardPacket(packet1)
      proxy ! ForwardPacket(packet2)
      proxy ! ForwardPacket(packet3)
      channel.packetsSent should be (List(packet3, packet2, packet1))
    }
  }
}
