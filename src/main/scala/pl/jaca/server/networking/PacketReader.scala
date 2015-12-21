package pl.jaca.server.networking

import io.netty.buffer.ByteBuf

/**
 * @author Jaca777
 *         Created 2015-12-21 at 21
 */
object PacketReader {

  def readSize(in: ByteBuf) = {
    in.getShort(0)
  }

  def readId(in: ByteBuf, size: Short) = {
    in.skipBytes(2) //skip size
    in.readShort()
  }

  def readData(in: ByteBuf, size: Short, id: Short): Array[Byte] = {
    val bytes = new Array[Byte](size - 4)
    in.readBytes(bytes)
    bytes
  }

  def isReadable(in: ByteBuf) = {
    in.readableBytes() >= 4
  }

  def isComplete(in: ByteBuf, size: Short) = in.readableBytes() >= size
}
