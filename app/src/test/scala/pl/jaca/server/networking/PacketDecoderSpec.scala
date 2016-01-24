package pl.jaca.server.networking

import java.util

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.Session
import pl.jaca.server.packets.{InPacket, OutPacket}
import pl.jaca.server.testing.{DummySession, DummyChannelHandlerContext, DummyPackets}
import DummyPackets._

/**
 * @author Jaca777
 *         Created 2015-12-04 at 17
 */
class PacketDecoderSpec extends TestKit(ActorSystem("PacketDecoderSpec")) with ImplicitSender with WordSpecLike with Matchers {

  case class TestPacketA(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s)

  case class TestPacketB(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s)

  case class TestPacketC(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s)

  object TestPacketResolver extends PacketResolver {
    override def resolve: Resolve = {
      case 1 => TestPacketA
      case 2 => TestPacketB
      case 3 => TestPacketC
    }

  }

  class TestOutPacket(id: Short, length: Short, msg: Array[Byte]) extends OutPacket(id, length, msg)

  val dummyContext = new DummyChannelHandlerContext
  val dummyConnection = new DummySession("test")

  "PacketDecoder" should {
    "decode packets 1" in {
      val msg: Array[Byte] = Array[Byte](12, 66, 92, 2, -8)
      val packetBuf = createPacket(1, msg)
      val decoder = new PacketDecoder(TestPacketResolver)
      val out = new util.ArrayList[AnyRef]()

      for(_ <- 1 to 3)decoder.decode(dummyContext, packetBuf, out)
      val packetFactory = out.get(0).asInstanceOf[(Session => InPacket)]
      val packet = packetFactory(dummyConnection)

      packet.msg should be(msg)
      packet.id should be (1)
      packet.sender should be (dummyConnection)
      packetBuf.release()
      ()
    }
  }
}
