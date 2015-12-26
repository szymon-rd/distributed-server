package pl.jaca.server.packets

import java.nio.charset.Charset

import com.google.common.primitives.{Chars, Longs, Ints, Shorts}

/**
 * @author Jaca777
 *         Created 2015-12-24 at 12
 */
trait DataReader {

  private var position = 0

  implicit class ByteArrayReader(bytes: Array[Byte]) {
    def getBoolean(offset: Short): Boolean =
      bytes(offset) == 1

    def getByte(offset: Short): Byte =
      bytes(offset)

    def getShort(offset: Short): Short =
      Shorts.fromByteArray(bytes.drop(offset))

    def getChar(offset: Short): Char =
      Chars.fromByteArray(bytes.drop(offset))

    def getInt(offset: Short): Int =
      Ints.fromByteArray(bytes.drop(offset))

    def getLong(offset: Short): Long =
      Longs.fromByteArray(bytes.drop(offset))

    def getString(offset: Short, length: Short)(implicit charset: Charset): String = {
      val slice: Array[Byte] = bytes.slice(offset, offset + length)
      new String(slice, charset)
    }

    def readBoolean(): Boolean = {
      val bool = bytes(position) == 1
      position += 1
      bool
    }

    def readByte(): Byte = {
      val byte = bytes(position)
      position += 1
      byte
    }

    def readShort(): Short = {
      val short = Shorts.fromByteArray(bytes.drop(position))
      position += 2
      short
    }

    def readChar(): Char = {
      val char = Chars.fromByteArray(bytes.drop(position))
      position += 2
      char
    }

    def readInt(): Int = {
      val int = Ints.fromByteArray(bytes.drop(position))
      position += 4
      int
    }

    def readLong(): Long = {
      val long = Longs.fromByteArray(bytes.drop(position))
      position += 8
      long
    }

    def readString(length: Short)(implicit charset: Charset): String = {
      val slice: Array[Byte] = bytes.slice(position, position + length)
      val string = new String(slice, charset)
      position += length
      string
    }

  }

}
