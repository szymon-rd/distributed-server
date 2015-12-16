package pl.jaca.server

import akka.actor.Actor
import pl.jaca.server.cluster.distribution.Distributable

/**
 * @author Jaca777
 *         Created 2015-12-11 at 22
 */
abstract class ServerApp extends Actor with Distributable {

}
