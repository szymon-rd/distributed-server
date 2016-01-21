package pl.jaca.server.packets

import java.nio.charset.Charset

import com.google.common.primitives.{Chars, Ints, Longs, Shorts}

/**
  * @author Jaca777
  *         Created 2015-12-24 at 12
  */
trait DataReader {

  private var position: Short = 0

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
      checkSize(offset, length)
      val slice: Array[Byte] = bytes.slice(offset, offset + length)
      new String(slice, charset)
    }

    def readBoolean(): Boolean = {
      val bool = bytes(position) == 1
      position = (position + 1).toShort
      bool
    }

    def readByte(): Byte = {
      val byte = bytes(position)
      position = (position + 1).toShort
      byte
    }

    def readShort(): Short = {
      val short = Shorts.fromByteArray(bytes.drop(position))
      position = (position + 2).toShort
      short
    }

    def readChar(): Char = {
      val char = Chars.fromByteArray(bytes.drop(position))
      position = (position + 2).toShort
      char
    }

    def readInt(): Int = {
      val int = Ints.fromByteArray(bytes.drop(position))
      position = (position + 4).toShort
      int
    }

    def readLong(): Long = {
      val long = Longs.fromByteArray(bytes.drop(position))
      position = (position + 8).toShort
      long
    }

    def readString(length: Short)(implicit charset: Charset): String = {
      checkSize(position, length)
      val slice: Array[Byte] = bytes.slice(position, position + length)
      val string = new String(slice, charset)
      position = (position + length).toShort
      string
    }

    def checkSize(offset: Short, length: Short) {
      if (bytes.length < offset + length) sys.error("Unable to read packet. Data index out of bounds.")
    }

  }


}
