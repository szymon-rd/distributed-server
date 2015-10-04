package pl.jaca.server.chat.packets.in

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import pl.jaca.server.chat.Chat._
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-06-16 at 23
 */
case class Send(i: Short, l: Short, m: Array[Byte], c: Connection) extends InPacket(i, l, m, c) with ChatroomPacket {
  val message = new String(m, CHARSET)
}
