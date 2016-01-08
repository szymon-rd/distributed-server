package example.newchat.handlers

import akka.actor.ActorRef
import example.newchat.model.service.Chat
import example.newchat.packets.in.{SendMessage, JoinChatroom}
import pl.jaca.server.Inject
import pl.jaca.server.eventhandling.EventActor

/**
 * @author Jaca777
 *         Created 2015-12-17 at 18
 */
class ChatPacketHandler(@Inject(serviceName = "chat") chat: ActorRef) extends EventActor {
  val eventStream = AsyncEventStream()
  eventStream.packets react {
    case p: JoinChatroom =>
      chat ! Chat.JoinChatroom(p.channelName, p.sender)
    case m: SendMessage => chat ! Chat.SendMessage(m.channelName, m.message, m.sender)
  }
}
