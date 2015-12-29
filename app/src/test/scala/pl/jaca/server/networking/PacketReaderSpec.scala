package pl.jaca.server.networking

import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.testing.DummyPackets
import DummyPackets._
import pl.jaca.util.testing.TypeMatchers

/**
 * @author Jaca777
 *         Created 2015-12-22 at 22
 */
class PacketReaderSpec extends WordSpecLike with Matchers with TypeMatchers {
  val data = Array[Byte](1,2,3)
  def completePacket = createPacket(2, data)
  def incompletePacket = completePacket.copy(0, 3)
  "PacketReader" should {
    "identify readable packets" in {
      PacketReader.isReadable(completePacket) should be(true)
    }
    "identify unreadable pakets" in {
      PacketReader.isReadable(incompletePacket) should be(false)
    }
    "read packet size" in {
      PacketReader.readSize(completePacket) should be(completePacket.capacity())
    }
    "read packet id" in {
      val completePacket = this.completePacket
      val size = PacketReader.readSize(completePacket)
      PacketReader.readId(completePacket, size) should be(2)
    }
    "read packet data" in {
      val completePacket = this.completePacket
      val size = PacketReader.readSize(completePacket)
      val id = PacketReader.readId(completePacket, size)
      PacketReader.readData(completePacket, size, id) should contain theSameElementsInOrderAs (data)
    }

    completePacket.release()
    incompletePacket.release()
    ()
  }
}
