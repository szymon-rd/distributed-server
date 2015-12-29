package pl.jaca.cluster

import akka.actor.{ActorLogging, Actor}

/**
 * @author Jaca777
 *         Created 2015-10-02 at 22
 */
abstract class Application extends Actor with ActorLogging {
  log.debug(s"Starting application at ${self.path}")
}
object Application {
  val AppActorPath = "user/launcher/clusterInitializer/sysNode/app"
  object Launch
}

