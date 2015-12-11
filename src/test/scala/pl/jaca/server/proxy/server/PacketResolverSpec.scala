package pl.jaca.server.proxy.server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.InPacket
import pl.jaca.testutils.TypeMatchers

/**
 * @author Jaca777
 *         Created 2015-11-27 at 17
 */
class PacketResolverSpec extends TestKit(ActorSystem("PacketResolverSpec")) with ImplicitSender with WordSpecLike with Matchers with TypeMatchers {

  case class TestPacketA(i: Short, l: Short, m: Array[Byte], c: Connection) extends InPacket(i, l, m, c)

  case class TestPacketB(i: Short, l: Short, m: Array[Byte], c: Connection) extends InPacket(i, l, m, c)

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
      val packet = TestPacketResolverA.resolve(1337, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      packet.msg should be(Array[Byte](9,0,0,0))
    }
    "recognize packets" in {
      val packetA = TestPacketResolverA.resolve(1337, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      val packetB = TestPacketResolverA.resolve(997, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      packetA should be(anInstanceOf[TestPacketA])
      packetB should be(anInstanceOf[TestPacketB])
    }
    "combine with other resolvers" in {
      val resolver = TestPacketResolverA and TestPacketResolverB
      val packetA = resolver.resolve(1337, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      val packetB = resolver.resolve(231, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      packetA should be(anInstanceOf[TestPacketA])
      packetB should be(anInstanceOf[TestPacketB])
    }
    "combine with other resolvers (resolving order)" in {
      val resolver = TestPacketResolverA and TestPacketResolverB
      val packet = resolver.resolve(997, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      packet should be(anInstanceOf[TestPacketA])
    }
  }
}
