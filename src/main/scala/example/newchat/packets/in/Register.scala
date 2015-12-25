package example.newchat.packets.in

import pl.jaca.server.Session
import pl.jaca.server.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-12-17 at 19
 */
case class Register(i: Short, l: Short, m: Array[Byte], s: Session) extends InPacket(i, l, m, s) {

}
