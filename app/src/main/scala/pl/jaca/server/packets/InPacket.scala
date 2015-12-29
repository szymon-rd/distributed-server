package pl.jaca.server.packets

import pl.jaca.server.Session
import pl.jaca.server.networking.ServerEvent

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 *         Contains received packet.
 */
class InPacket(id: Short, length: Short, msg: Array[Byte], val sender: Session) extends Packet(id, length, msg) with ServerEvent.InPacketEvent with DataReader

