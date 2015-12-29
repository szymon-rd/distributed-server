package pl.jaca.server.networking

import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.Session
import pl.jaca.server.packets.InPacket
import pl.jaca.util.testing.TypeMatchers

/**
 * @author Jaca777
 *         Created 2015-11-27 at 17
 */
class PacketResolverSpec extends WordSpecLike with Matchers with TypeMatchers {

  case class TestPacketA(i: Short, l: Short, m: Array[Byte], c: Session) extends InPacket(i, l, m, c)

  case class TestPacketB(i: Short, l: Short, m: Array[Byte], c: Session) extends InPacket(i, l, m, c)

  object TestPacketResolverA extends PacketResolver {
    override def resolve: Resolve = {
      case 1337 => TestPacketA
      case 997 => TestPacketB
    }
  }

  object TestPacketResolverB extends PacketResolver {
    override def resolve: Resolve = {
      case 231 => TestPacketB
      case 997 => TestPacketA
    }
  }

  "PacketResolver" must {
    "construct packets" in {
      val packet = TestPacketResolverA.resolve(1337, 9, Array[Byte](9,0,0,0), Session.NoSession)
      packet.msg should be(Array[Byte](9,0,0,0))
    }
    "recognize packets" in {
      val packetA = TestPacketResolverA.resolve(1337, 9, Array[Byte](9,0,0,0), Session.NoSession)
      val packetB = TestPacketResolverA.resolve(997, 9, Array[Byte](9,0,0,0), Session.NoSession)
      packetA should be(anInstanceOf[TestPacketA])
      packetB should be(anInstanceOf[TestPacketB])
    }
    "combine with other resolvers" in {
      val resolver = TestPacketResolverA and TestPacketResolverB
      val packetA = resolver.resolve(1337, 9, Array[Byte](9,0,0,0), Session.NoSession)
      val packetB = resolver.resolve(231, 9, Array[Byte](9,0,0,0), Session.NoSession)
      packetA should be(anInstanceOf[TestPacketA])
      packetB should be(anInstanceOf[TestPacketB])
    }
    "combine with other resolvers (resolving order)" in {
      val resolver = TestPacketResolverA and TestPacketResolverB
      val packet = resolver.resolve(997, 9, Array[Byte](9,0,0,0), Session.NoSession)
      packet should be(anInstanceOf[TestPacketA])
    }
  }
}
