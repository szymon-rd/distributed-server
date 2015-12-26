package pl.jaca.server.service

import akka.actor.Actor
import pl.jaca.cluster.distribution.{Distribution, Distributable}

/**
 * @author Jaca777
 *         Created 2015-12-16 at 16
 */
abstract class Service extends Actor with Distribution with Distributable {

}
