package pl.jaca.server.chat.events.packets.in

import pl.jaca.server.chat.Chat._
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-06-16 at 23
 */
case class JoinRoom(i: Short, l: Short, m: Array[Byte], c: Connection)extends InPacket(i, l, m, c) {
  val channelName = new String(m, CHARSET)
}
