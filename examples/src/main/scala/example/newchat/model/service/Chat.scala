package example.newchat.model.service

import java.nio.charset.Charset

import akka.util.Timeout
import example.newchat.model.domain.ChatroomStorage.Result
import example.newchat.model.domain.{Chatroom, ChatroomStorage}
import example.newchat.model.service.Chat.{SendMessage, JoinChatroom}
import example.newchat.model.sessionstate.{NotLoggedUser, LoggedUser}
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
    case JoinChatroom(name, sender) => sender.withState {
      case Some(l: LoggedUser) =>
        joinChatroom(name, l)
      case Some(l: NotLoggedUser) =>
    }
    case SendMessage(name, msg, sender) => sender.withState {
      case Some(l: LoggedUser) => sendMessage(name, msg, l)
    }
  }

  def joinChatroom(name: String, sender: LoggedUser) = {
    for {
      chatroom <- getChatroom(name)
    } {
      chatroom ! Chatroom.Join(sender)
    }
  }


  def sendMessage(channelName: String, msg: String, sender: LoggedUser): Unit = {
    for {
      chatroom <- getChatroom(channelName)
    } chatroom ! Chatroom.Message(sender, msg)
  }

  def getChatroom(name: String) = {
    for {
      result <- chatroomStorage ? ChatroomStorage.Get(name)
    } yield {
      result.asInstanceOf[Result].chatroom
    }

  }


  override def getLoad: Load = AbsoluteLoad(2.0f)
}

object Chat {

  case class JoinChatroom(channelName: String, sender: Session)

  case class SendMessage(channelName: String, message: String, sender: Session)

  val charset = Charset.forName("UTF-8")
}
