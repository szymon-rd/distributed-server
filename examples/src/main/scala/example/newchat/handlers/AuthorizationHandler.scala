package example.newchat.handlers

import akka.actor.ActorRef
import example.newchat.model.service.Authorization
import example.newchat.model.sessionstate.NotLoggedUser
import pl.jaca.server.Inject
import pl.jaca.server.eventhandling.{EventActor, ServerEventHandling}
import example.newchat.packets.in.{Register, Login}
import pl.jaca.server.networking.ServerEvent.SessionActive


/**
 * @author Jaca777
 *         Created 2015-12-17 at 18
 */


class AuthorizationHandler(@Inject(serviceName = "authorization") authorization: ActorRef) extends EventActor with ServerEventHandling {
  val eventStream = AsyncEventStream()

  eventStream.sessionEvents react {
    case SessionActive(s) =>
      s.mapState(_ => new NotLoggedUser(s))
  }

  eventStream.packets react {
    case p: Login =>
      authorization ! Authorization.Login(p.login, p.password, p.sender)
    case r: Register =>
      authorization ! Authorization.Register(r.login, r.password)
  }

}
