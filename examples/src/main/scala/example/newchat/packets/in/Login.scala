package example.newchat.packets.in

import example.newchat.model.service.Chat
import pl.jaca.server.Session
import pl.jaca.server.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-12-17 at 19
 */
case class Login(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s) {
  implicit val charset = Chat.charset
  private val loginLength = m.readShort()
  val login = m.readString(loginLength)
  private val passwordLength = m.readShort()
  val password = m.readString(passwordLength)
}
