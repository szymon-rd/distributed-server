package pl.jaca.server.packets

import java.nio.charset.Charset

import org.scalatest.{Matchers, WordSpecLike}

/**
 * @author Jaca777
 *         Created 2015-12-17 at 20
 */
class DataReaderProps extends WordSpecLike with Matchers with DataReader {
  "DataReader" should {
    "read boolean" in {
      val bytes = Array[Byte](0, 0, 1, 2)
      bytes.readBoolean(2) should be(true)
      bytes.readBoolean(3) should be(false)
    }
    "read byte" in {
      val bytes = Array[Byte](2, 43, -26, 64)
      bytes.readByte(1) should be(43)
      bytes.readByte(2) should be(-26)
  }
    "read short" in {
      val bytes = Array[Byte](0, 0, 0, 11, 0, 12)
      bytes.readShort(2) should be(11)
      bytes.readShort(4) should be(12)
    }
    "read int" in {
      val bytes = Array[Byte](12, 0, 0, 0,Integer.parseInt("00001100", 2).toByte, Integer.parseInt("10001000", 2).toByte)
      bytes.readInt(2) should be(Integer.parseInt("0000110010001000", 2))
    }
    "read long" in {
      val bytes = Array[Byte](1, 32, 0, 0, 0, 0, 0, 0, 0, -122)
      bytes.readLong(2) should be(134)
      ()
    }
    "read String" in {
      val charset = Charset.forName("UTF-8")
      val str = "test".getBytes(charset)
      val length = str.length.toShort
      val bytes = Array[Byte](1,2,3) ++ str
      bytes.readString(3, length, charset) should be("test")
    }

  }
}
