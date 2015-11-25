package pl.jaca.server.newchat.messaging

import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Distributable, Load}
import pl.jaca.server.proxy.eventhandling.EventActor

/**
 * @author Jaca777
 *         Created 2015-11-25 at 19
 */
class SharedMessaging extends EventActor with Distributable {
  override def getLoad: Load = AbsoluteLoad(3.0f)
}
