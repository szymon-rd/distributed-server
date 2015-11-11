package pl.jaca.server.proxy.server

import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-10-24 at 12
 */
abstract class Event

case class PacketReceived(inPacket: InPacket) extends Event

class ConnectionEvent(connection: Connection)  extends Event

case class ConnectionActive(connection: Connection) extends ConnectionEvent(connection)

case class ConnectionInactive(connection: Connection) extends ConnectionEvent(connection)