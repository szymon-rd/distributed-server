package pl.jaca.server.proxy.packets

import io.netty.buffer.ByteBuf

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 */
abstract class OutPacket(id: Short, length: Short, msg: Array[Byte]) extends Packet(id, length, msg){
  def store(dest: ByteBuf): ByteBuf = {
    dest.writeShort(id)
    dest.writeShort(length)
    dest.writeBytes(msg)
    dest
  }
}
