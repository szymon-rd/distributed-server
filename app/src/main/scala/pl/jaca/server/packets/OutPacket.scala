package pl.jaca.server.packets

import io.netty.buffer.ByteBuf

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 *
 */
abstract class OutPacket(id: Short, length: Short, msg: Array[Byte]) extends Packet(id, length, msg){

  def this(id: Short, msg: Array[Byte]) = this(id, (msg.length + 4).toShort, msg)

  def store(dest: ByteBuf): ByteBuf = {
    dest.writeShort(id)
    dest.writeShort(length)
    dest.writeBytes(msg)
    dest
  }
}
