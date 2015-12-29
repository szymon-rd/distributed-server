package pl.jaca.server.testing

import io.netty.buffer.{ByteBuf, Unpooled}
import pl.jaca.server.Session
import pl.jaca.server.packets.{InPacket, OutPacket}

/**
 * @author Jaca777
 *         Created 2015-12-04 at 17
 */
object DummyPackets {

  class DummyInPacket(id: Short) extends InPacket(id, -1, Array.empty, Session.NoSession){
    def this() = this(-1)
  }

  class DummyOutPacket(id: Short) extends OutPacket(id, -1, Array.empty) {
    def this() = this(-1)
  }

  def createPacket(id: Short, msg: Array[Byte]): ByteBuf = {
    val idBytes = Array((id >> 8 & 0xFF).toByte, (id & 0xFF).toByte)
    val length = msg.length + 4
    val lengthBytes = Array((length >> 8 & 0xFF).toByte,(length & 0xFF).toByte)
    val packet = lengthBytes ++ idBytes ++ msg
    val buffer = Unpooled.copiedBuffer(packet)
    buffer.retain()
  }

}
