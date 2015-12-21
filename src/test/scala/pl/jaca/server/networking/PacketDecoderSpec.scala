package pl.jaca.server.networking

import java.util

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.Channel
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.Connection
import pl.jaca.server.packets.{InPacket, OutPacket}
import pl.jaca.testutils.server.proxy.{DummyConnection, DummyChannelHandlerContext}

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


  class TestOutPacket(id: Short, length: Short, msg: Array[Byte]) extends OutPacket(id, length, msg)

  val dummyContext = new DummyChannelHandlerContext
  val dummyConnection = new DummyConnection("test")

  "PacketDecoder" should {
    "decode packets 1" in {
      val msg: Array[Byte] = Array[Byte](12, 66, 92, 2, -8)
      val packetBuf = createPacket(1, msg)
      val decoder = new PacketDecoder(TestPacketResolver)
      val out = new util.ArrayList[AnyRef]()
      decoder.decode(dummyContext, packetBuf, out)
      val packetFactory = out.get(0).asInstanceOf[(Connection => InPacket)]
      val packet = packetFactory(dummyConnection)
      packet.msg should contain(theSameElementsInOrderAs(msg))
      packet.id should be (1)
      packet.sender should be (dummyConnection)
    }
  }

  def createPacket(id: Short, msg: Array[Byte]): ByteBuf = {
    val idBytes = Array((id & 0xFF).toByte, (id >> 8 & 0xFF).toByte)
    val length = msg.length + 4
    val lengthBytes = Array((length & 0xFF).toByte, (length >> 8 & 0xFF).toByte)
    Unpooled.copiedBuffer(lengthBytes ++ idBytes ++ msg)
  }
}
