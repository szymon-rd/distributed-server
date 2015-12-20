package pl.jaca.server.networking

import java.util.concurrent.atomic.AtomicBoolean

import pl.jaca.server.Connection

/**
 * @author Jaca777
 *         Created 2015-10-24 at 12
 */

private[server] trait Event {
  private val handled = new AtomicBoolean(false)
  def getAndHandle(): Boolean = handled.getAndSet(true)
}

sealed trait ServerEvent extends Event

sealed trait ServerStateEvent extends ServerEvent

object ServerEvent {

  abstract class ConnectionEvent(connection: Connection) extends ServerStateEvent

  case class ConnectionActive(con: Connection) extends ConnectionEvent(con)

  case class ConnectionInactive(con: Connection) extends ConnectionEvent(con)

  private[server] trait InPacketEvent extends ServerEvent

}