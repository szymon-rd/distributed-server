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
      bytes.getBoolean(2) should be(true)
      bytes.getBoolean(3) should be(false)
    }
    "read byte" in {
      val bytes = Array[Byte](2, 43, -26, 64)
      bytes.getByte(1) should be(43)
      bytes.getByte(2) should be(-26)
  }
    "read short" in {
      val bytes = Array[Byte](0, 0, 0, 11, 0, 12)
      bytes.getShort(2) should be(11)
      bytes.getShort(4) should be(12)
    }
    "read int" in {
      val bytes = Array[Byte](12, 0, 0, 0,Integer.parseInt("00001100", 2).toByte, Integer.parseInt("10001000", 2).toByte)
      bytes.getInt(2) should be(Integer.parseInt("0000110010001000", 2))
    }
    "read long" in {
      val bytes = Array[Byte](1, 32, 0, 0, 0, 0, 0, 0, 0, -122)
      bytes.getLong(2) should be(134)
      ()
    }
    "read String" in {
      implicit val charset = Charset.forName("UTF-8")
      val str = "test".getBytes(charset)
      val length = str.length.toShort
      val bytes = Array[Byte](1,2,3) ++ str
      bytes.getString(3, length) should be("test")
    }

  }
}
