package pl.jaca.server.newchat.packets.in

import pl.jaca.server.Connection
import pl.jaca.server.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-12-17 at 19
 */
case class Register(i: Short, l: Short, m: Array[Byte], s: Connection) extends InPacket(i, l, m, s) {

}
