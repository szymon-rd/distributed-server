package pl.jaca.server.packets

import java.nio.charset.Charset

import org.scalatest.{Matchers, WordSpecLike}

/**
 * @author Jaca777
 *         Created 2015-12-24 at 14
 */
class DataConstructorProps extends WordSpecLike with Matchers {
  import DataConstructor._
  "DataConstructor" should {
    "construct arrays with bytes" in {
      val data = dataHead \\ 1.toByte \\ 2.toByte \\ 3.toByte
      data should be(Array[Byte](1,2,3))
    }
    "construct arrays with shorts and chars" in {
      val data = dataHead \\ 1.toByte \\ 2.toShort \\ 3.toChar
      data should be(Array[Byte](1,0, 2, 0, 3))
    }
    "construct arrays with ints and longs" in {
      val data = dataHead \\ 1.toByte \\ 2 \\ 3L
      data should be(Array[Byte](1,0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 3))
    }
    "construct arrays with strings" in {
      implicit val charset: Charset = Charset.forName("UTF-8")
      val data = dataHead \\ 1.toByte \\ "test"
      data should be(Array[Byte](1) ++ "test".getBytes(charset))
    }
    "construct arrays with arrays of type AnyVal" in {

      val array1 = Array[Int](1,7)
      val array2 = Array[Boolean](true, false, true)
      val data = dataHead \ array1 \ array2
      data should be(Array[Byte](0,0,0,1,0,0,0,7,1,0,1))
    }
    "construct arrays with arrays of type String" in {
      implicit val charset: Charset = Charset.forName("UTF-8")
      val array1 = Array[Int](1,7)
      val array2 = Array[String]("ab", "cd", "ef")
      val data = dataHead \ array1 \ array2
      data should be(Array[Byte](0,0,0,1,0,0,0,7) ++ "abcdef".getBytes(charset))
    }

  }
}
