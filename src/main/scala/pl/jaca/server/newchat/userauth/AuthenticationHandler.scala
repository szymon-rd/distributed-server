package pl.jaca.server.newchat.userauth

import pl.jaca.cluster.distribution.{AbsoluteLoad, Load, Distributable}
import pl.jaca.server.eventhandling.EventActor

/**
 * @author Jaca777
 *         Created 2015-11-25 at 18
 */
class AuthenticationHandler extends EventActor with Distributable {
  override def getLoad: Load = AbsoluteLoad(5.0f)
}
