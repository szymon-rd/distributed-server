package pl.jaca.server.proxy.server

import pl.jaca.server.proxy.Connection

/**
 * @author Jaca777
 *         Created 2015-10-24 at 12
 */

private[proxy] trait Event
 
sealed trait ServerEvent extends Event

object ServerEvent {

  case class ConnectionEvent(connection: Connection) extends ServerEvent

  class ConnectionActive(con: Connection) extends ConnectionEvent(con)

  class ConnectionInactive(con: Connection) extends ConnectionEvent(con)

  private[proxy] trait InPacketEvent extends ServerEvent

}