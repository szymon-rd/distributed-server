package example.newchat.model.service

import java.nio.charset.Charset

import akka.util.Timeout
import example.newchat.model.domain.ChatroomStorage.Result
import example.newchat.model.domain.{Chatroom, ChatroomStorage}
import example.newchat.model.service.Chat.JoinChatroom
import example.newchat.model.sessionstate.LoggedUser
import pl.jaca.cluster.distribution.{AbsoluteLoad, Load}
import pl.jaca.server.Session
import pl.jaca.server.service.Service
import akka.pattern._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-12-17 at 18
 */
class Chat extends Service {
  implicit val executionContext = context.dispatcher
  implicit val timeout = Timeout(2 seconds)

  val chatroomStorage = context.distribute(new ChatroomStorage, "chatroom-storage")

  override def receive: Receive = {
    case JoinChatroom(name, sender) => joinChatroom(name, sender)
  }

  def joinChatroom(name: String, sender: Session) = {
    sender.withSessionState {
      case Some(l: LoggedUser) =>
        for {
          storage <- chatroomStorage
          result <- storage ? ChatroomStorage.Get(name)
          chatroom = result.asInstanceOf[Result].chatroom
        } chatroom ! Chatroom.Join(l)
    }
  }


  override def getLoad: Load = AbsoluteLoad(2.0f)
}

object Chat {

  case class JoinChatroom(channelName: String, sender: Session)

  case class SendMessage(channelName: String, message: String)

  val charset = Charset.forName("UTF-8")
}
