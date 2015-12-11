package pl.jaca.server.proxy.server

import java.util

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.Channel
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.{InPacket, OutPacket}
import pl.jaca.testutils.server.proxy.{DummyChannelHandlerContext, DummyNettyChannel}

/**
 * @author Jaca777
 *         Created 2015-12-04 at 17
 */
class PacketDecoderSpec extends TestKit(ActorSystem("PacketDecoderSpec")) with ImplicitSender with WordSpecLike with Matchers {

  case class TestPacketA(i: Short, l: Short, m: Array[Byte], s: Connection) extends InPacket(i, l, m, s)

  case class TestPacketB(i: Short, l: Short, m: Array[Byte], s: Connection) extends InPacket(i, l, m, s)

  case class TestPacketC(i: Short, l: Short, m: Array[Byte], s: Connection) extends InPacket(i, l, m, s)

  object TestPacketResolver extends PacketResolver {
    override def resolve: Resolve = {
      case 1 => TestPacketA
      case 2 => TestPacketB
      case 3 => TestPacketC
    }

  }

  class SingleConnectionManager(connection: Connection) extends ConnectionManager(null, null) {
    override def getConnection(channel: Channel): Option[Connection] = Some(connection)

    override def getConnections(f: (Connection) => Boolean): Set[Connection] = super.getConnections(f)

    override def getAllConnections: Set[Connection] = Set(connection)

    override def createConnection(channel: Channel): Unit = throw new UnsupportedOperationException

    override def removeConnection(channel: Channel): Unit = throw new UnsupportedOperationException
  }

  class SingleChannelHandlerContext(c: Channel) extends DummyChannelHandlerContext {
    override def channel(): Channel = c
  }

  class TestOutPacket(id: Short, length: Short, msg: Array[Byte]) extends OutPacket(id, length, msg)

  "PacketDecoder" should {
    val connection = new Connection(null, 0, new DummyNettyChannel(0, 0), null)
    val connectionManager = new SingleConnectionManager(connection)
    val channel = new DummyNettyChannel(0, 0)
    val ctx = new SingleChannelHandlerContext(channel)

    "decode packets 1" in {
      val id: Short = 1
      val msg: Array[Byte] = Array[Byte](12, 66, 92, 2, -8)
      val decoder = new PacketDecoder(TestPacketResolver, connectionManager)

      val list = new util.LinkedList[AnyRef]()
    }
  }

  def createPacket(id: Short, msg: Array[Byte]): ByteBuf = {
    val idBytes = Array((id & 0xFF).toByte, (id >> 8 & 0xFF).toByte)
    val length = msg.length + 4
    val lengthBytes = Array((length & 0xFF).toByte, (length >> 8 & 0xFF).toByte)
    Unpooled.copiedBuffer(idBytes ++ lengthBytes ++ msg)
  }
}
