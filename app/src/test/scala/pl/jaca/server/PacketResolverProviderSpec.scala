package pl.jaca.server

import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.PacketResolverProviderSpec._
import pl.jaca.server.packets.InPacket
import pl.jaca.server.networking.PacketResolver
import pl.jaca.server.providers.PacketResolverProvider

/**
 * @author Jaca777
 *         Created 2015-12-17 at 20
 */
class PacketResolverProviderSpec extends WordSpecLike with Matchers {

  val properConfig1 = ConfigFactory.load("server/conf1.conf")
  val wrongConfig1 = ConfigFactory.load("server/conf2.conf")
  val wrongConfig2 = ConfigFactory.load("server/conf3.conf")
  val wrongConfig3 = ConfigFactory.load("server/conf4.conf")
  val wrongConfig4 = ConfigFactory.load("server/conf5.conf")

  def provider: PacketResolverProvider = new PacketResolverProvider(properConfig1)

  "PacketResolverProvider" must {
    "load resolvers from config" in {
      val resolver = provider.getResolver
      val packet = resolver.resolve(1, 2, null, null)
      packet shouldBe a[TestPacketA]
    }
    "combine given resolvers" in {
      val resolver = provider.getResolver
      val packet = resolver.resolve(3, 2, null, null)
      packet shouldBe a[TestPacketC]
    }
    "keep resolving order" in {
      val resolver = provider.getResolver
      val packet = resolver.resolve(2, 2, null, null)
      packet shouldBe a[TestPacketC]
    }
    "throw exception when class in not type of resolver" in {
      intercept[ServerConfigException] {
        new PacketResolverProvider(wrongConfig1)
      }.getMessage should be ("pl.jaca.server.PacketResolverProviderSpec$SomeClass is not type of PacketResolver.")
    }
    "throw exception when class is not found" in {
      intercept[ServerConfigException] {
        new PacketResolverProvider(wrongConfig2)
      }.getCause shouldBe a[ClassNotFoundException]
    }
    "throw exception when class has no parameterless constructor defined" in {
      intercept[ServerConfigException] {
        new PacketResolverProvider(wrongConfig3)
      }.getMessage should be ("Resolver pl.jaca.server.PacketResolverProviderSpec$ParametrizedResolver has no parameterless constructor defined.")
    }
    "throw exception when class is abstract" in {
      intercept[ServerConfigException] {
        new PacketResolverProvider(wrongConfig4)
      }.getMessage should be("Resolver pl.jaca.server.PacketResolverProviderSpec$AbstractResolver is an abstract class.")
    }
  }

}

object PacketResolverProviderSpec {

  case class TestPacketA(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s)

  case class TestPacketB(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s)

  case class TestPacketC(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s)

  class ResolverA extends PacketResolver {
    override def resolve: Resolve = {
      case 1 => TestPacketA
      case 2 => TestPacketB
    }
  }

  class ResolverB extends PacketResolver {
    override def resolve: Resolve = {
      case 3 => TestPacketC
    }
  }

  class ResolverC extends PacketResolver {
    override def resolve: Resolve = {
      case 2 => TestPacketC
    }
  }

  class SomeClass

  class ParametrizedResolver(someParameter: Any) extends PacketResolver {
    override def resolve: Resolve = throw new UnsupportedOperationException
  }

  abstract class AbstractResolver extends PacketResolver

}



