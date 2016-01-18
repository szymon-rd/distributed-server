package pl.jaca.server.networking

import java.util.concurrent.atomic.AtomicBoolean

import pl.jaca.server.{Session, Session$}

/**
 * @author Jaca777
 *         Created 2015-10-24 at 12
 */

private[server] trait Event {
  private val handled = new AtomicBoolean(false)

  private[server] def getAndHandle(): Boolean = handled.getAndSet(true)
}

sealed trait ServerEvent extends Event

sealed trait ServerStateEvent extends ServerEvent

object ServerEvent {

  abstract class SessionEvent(session: Session) extends ServerStateEvent

  case class SessionActive(s: Session) extends SessionEvent(s)

  case class SessionInactive(s: Session) extends SessionEvent(s)

  private[server] trait InPacketEvent extends ServerEvent

}