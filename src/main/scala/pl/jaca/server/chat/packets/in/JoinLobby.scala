package pl.jaca.server.chat.packets.in


import pl.jaca.server.chat.Chat._
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-06-13 at 17
 */
case class JoinLobby(i: Short, l: Short, m: Array[Byte], c: Connection) extends InPacket(i, l, m, c) {
  val nickname = new String(m, CHARSET)
}


