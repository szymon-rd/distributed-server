package pl.jaca.server.newchat.handlers

import akka.actor.ActorRef
import pl.jaca.server.DI
import pl.jaca.server.eventhandling.{EventActor, ServerEventHandling}
import pl.jaca.server.newchat.packets.in.Login



/**
 * @author Jaca777
 *         Created 2015-12-17 at 18
 */


class AuthorizationPacketHandler(@DI(serviceName = "authorizationService") authorization: ActorRef) extends EventActor with ServerEventHandling {
  val eventStream = AsyncEventStream()

  eventStream.packets react {
    case p: Login => ???
  }
}
