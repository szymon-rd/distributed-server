package pl.jaca.server.packets

import java.nio.charset.Charset

import com.google.common.primitives.{Chars, Ints, Longs, Shorts}

/**
 * @author Jaca777
 *         Created 2015-12-25 at 00
 */
object DataConstructor {

  private val DefaultCharset = Charset.forName("UTF-8")

  /**
   * Empty list, represents head of data.
   */
  def dataHead = Array.empty[Byte]

  /**
   * Length of @text in @charset.
   * @param text
   * @param charset
   * @return
   */
  def lengthOf(text: String)(implicit charset: Charset = DefaultCharset): Short = text.getBytes(charset).length.toShort

  /**
   * OutPacked data builder. Example of usage:
   * dataHead \\ passLength \\ password \\ loginLength \\ login
   */
  implicit class ByteArrayConstructor(array: Array[Byte]) {
    def \\(a: AnyVal): Array[Byte] = a match {
      case b: Boolean => array :+ (if (b) 1.toByte else 0.toByte)
      case b: Byte => array :+ b
      case c: Char => array ++ Chars.toByteArray(c)
      case s: Short => array ++ Shorts.toByteArray(s)
      case i: Int => array ++ Ints.toByteArray(i)
      case l: Long => array ++ Longs.toByteArray(l)
    }

    def \\(s: String)(implicit charset: Charset = DefaultCharset): Array[Byte] = array ++ s.getBytes(charset)

    def \(a: Array[_ <: AnyVal])(implicit d: DummyImplicit): Array[Byte] = a.foldLeft[Array[Byte]](array)((acc, elem) => acc \\ elem)

    def \(a: Array[String])(implicit charset: Charset = DefaultCharset): Array[Byte] = a.foldLeft[Array[Byte]](array)((acc, elem) => acc \\ elem)
  }


}
