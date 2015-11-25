package pl.jaca.server.cluster.core

import akka.actor.Actor

/**
 * @author Jaca777
 *         Created 2015-09-10 at 17
 */
trait AkkaTools {
  class DummyActor extends Actor {
    override def receive: Receive = {
      case _ =>
    }
  }
}
