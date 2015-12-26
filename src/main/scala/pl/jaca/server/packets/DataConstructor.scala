package pl.jaca.server.packets

import java.nio.charset.Charset

import com.google.common.primitives.{Chars, Ints, Longs, Shorts}

/**
 * @author Jaca777
 *         Created 2015-12-25 at 00
 */
object DataConstructor {

  private val defaultCharset = Charset.forName("UTF-16")

  def dataHead = Array.empty[Byte]

  def lengthOf(text: String)(implicit charset: Charset = defaultCharset): Short = text.getBytes(charset).length.toShort

  implicit class ByteArrayConstructor(array: Array[Byte]) {
    def \(a: AnyVal): Array[Byte] = a match {
      case b: Boolean => array :+ (if (b) 1.toByte else 0.toByte)
      case b: Byte => array :+ b
      case c: Char => array ++ Chars.toByteArray(c)
      case s: Short => array ++ Shorts.toByteArray(s)
      case i: Int => array ++ Ints.toByteArray(i)
      case l: Long => array ++ Longs.toByteArray(l)
    }

    def \\(s: String)(implicit charset: Charset = defaultCharset): Array[Byte] = array ++ s.getBytes(charset)

    def \(a: Array[_ <: AnyVal])(implicit d: DummyImplicit): Array[Byte] = a.foldLeft[Array[Byte]](array)((acc, elem) => acc \ elem)

    def \(a: Array[String])(implicit charset: Charset = defaultCharset): Array[Byte] = a.foldLeft[Array[Byte]](array)((acc, elem) => acc \\ elem)
  }


}
