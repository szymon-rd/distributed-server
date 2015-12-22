package pl.jaca.server.networking

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.netty.channel.Channel
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.Session
import pl.jaca.server.networking.Server.EventOccurred
import pl.jaca.server.networking.ServerEvent.{ConnectionInactive, ConnectionActive}
import pl.jaca.server.packets.InPacket
import pl.jaca.testutils.server.proxy.{DummyChannelHandlerContext, DummyNettyChannel}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-12-22 at 21
 */
class ChannelHandlerSpec extends TestKit(ActorSystem("ChannelHandlerSpec")) with ImplicitSender with WordSpecLike with Matchers {

  val dummyChannel = new DummyNettyChannel(123, 1)

  object DummyContext extends DummyChannelHandlerContext {
    override def channel(): Channel = dummyChannel
  }


  val proxyFactory = (_: Channel) => null
  "ChannelHandler" should {
    "inform about new connection" in {
      val serverProbe = new TestProbe(system)
      val channelHandler = new ChannelHandler(proxyFactory, serverProbe.ref)

      channelHandler.channelActive(DummyContext)

      val msg = serverProbe.expectMsgType[EventOccurred](200 millis)
      msg.event shouldBe a[ConnectionActive]
      val event = msg.event.asInstanceOf[ConnectionActive]
      event.con.port should be(123)
      ()
    }
    "inform about connection removal" in {
      val serverProbe = new TestProbe(system)
      val channelHandler = new ChannelHandler(proxyFactory, serverProbe.ref)

      channelHandler.channelActive(DummyContext)
      serverProbe.receiveOne(200 millis)
      channelHandler.channelInactive(DummyContext)

      val msg = serverProbe.expectMsgType[EventOccurred](200 millis)
      msg.event shouldBe a[ConnectionInactive]
      val event = msg.event.asInstanceOf[ConnectionInactive]
      event.con.port should be(123)
      ()
    }
    "create packets and forward them to server" in {
      val packetFactory = (s: Session) => new InPacket(321, -1, null, s)
      val serverProbe = new TestProbe(system)
      val channelHandler = new ChannelHandler(proxyFactory, serverProbe.ref)

      channelHandler.channelActive(DummyContext)
      serverProbe.receiveOne(200 millis)
      channelHandler.channelRead(DummyContext, packetFactory)

      val msg = serverProbe.expectMsgType[EventOccurred](200 millis)
      msg.event shouldBe a[InPacket]
      val event = msg.event.asInstanceOf[InPacket]
      event.sender.port should be(123)
      event.id should be(321)
      ()
    }
  }
}
