package pl.jaca.server.networking

import java.util.concurrent.atomic.AtomicBoolean

import pl.jaca.server.{Session, Session$}

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

  abstract class ConnectionEvent(connection: Session) extends ServerStateEvent

  case class ConnectionActive(con: Session) extends ConnectionEvent(con)

  case class ConnectionInactive(con: Session) extends ConnectionEvent(con)

  private[server] trait InPacketEvent extends ServerEvent

}