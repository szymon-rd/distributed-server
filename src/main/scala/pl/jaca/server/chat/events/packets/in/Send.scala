package pl.jaca.server.chat.events.packets.in

import pl.jaca.server.chat.Chat
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-06-16 at 23
 */
case class Send(i: Short, l: Short, m: Array[Byte], c: Connection) extends InPacket(i, l, m, c) with ChatroomPacket {
  private val roomNameLength = m(0)
  private val splitted = m.splitAt(roomNameLength)
  val roomName = new String(splitted._1, Chat.CHARSET)
  val message = new String(splitted._2, Chat.CHARSET)
}
