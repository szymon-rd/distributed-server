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

  object TestPacketResolver extends PacketResolver {
    override def resolve: Resolve = {
      case 1337 => TestPacketA
      case 997 => TestPacketB
    }
  }

  "PacketResolver" must {
    "correctly construct packets" in {
      val packet = TestPacketResolver.resolve(1337, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      packet.msg should be(Array[Byte](9,0,0,0))
    }
    "recognize packets" in {
      val packetA = TestPacketResolver.resolve(1337, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      val packetB = TestPacketResolver.resolve(997, 9, Array[Byte](9,0,0,0), Connection.NoConnection)
      packetA should be(anInstanceOf[TestPacketA])
      packetB should be(anInstanceOf[TestPacketB])
    }
  }
}
