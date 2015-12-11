package pl.jaca.testutils.server.proxy

import akka.actor.ActorRef
import pl.jaca.server.proxy.Connection

/**
 * @author Jaca777
 *         Created 2015-12-11 at 18
 */
class DummyConnection(host: String) extends Connection(host, 1, new DummyNettyChannel(1, 1), ActorRef.noSender)
