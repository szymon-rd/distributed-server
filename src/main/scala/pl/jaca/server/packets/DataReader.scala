package pl.jaca.server.packets

import java.nio.charset.Charset

import com.google.common.primitives.{Chars, Longs, Ints, Shorts}

/**
 * @author Jaca777
 *         Created 2015-12-24 at 12
 */
trait DataReader {

  implicit class ByteArrayReader(bytes: Array[Byte]) {
    def readBoolean(offset: Short): Boolean =
      bytes(offset) == 1

    def readByte(offset: Short): Byte =
      bytes(offset)

    def readShort(offset: Short): Short =
      Shorts.fromByteArray(bytes.drop(offset))

    def readChar(offset: Short): Char =
      Chars.fromByteArray(bytes.drop(offset))

    def readInt(offset: Short): Int =
      Ints.fromByteArray(bytes.drop(offset))

    def readLong(offset: Short): Long =
      Longs.fromByteArray(bytes.drop(offset))

    def readString(offset: Short, length: Short, charset: Charset): String = {
      val slice: Array[Byte] = bytes.slice(offset, offset + length)
      new String(slice, charset)
    }

  }

}
