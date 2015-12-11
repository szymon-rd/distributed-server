package pl.jaca.server.chat

import java.nio.charset.Charset

import akka.actor.{ActorRef, Props}
import pl.jaca.server.chat.events.packets.ChatPacketResolver
import pl.jaca.server.chat.events.packets.in.{JoinLobby, JoinRoom, Send}
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Distributable, Distribution}
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.eventhandling._
import pl.jaca.server.proxy.server.Server

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Chat extends EventActor with Distribution with Distributable {

  var nicknames = Map[Connection, String]()
  var rooms = Map[String, ActorRef]()
  val server = context.actorOf(Props(new Server(port = Chat.PORT, resolver = ChatPacketResolver)))

  implicit val stream = AsyncEventStream()
  stream react {
    case joinRoom: JoinRoom =>
      val roomName = joinRoom.channelName
      val room = rooms.get(roomName)
      if (room.isDefined) Route(room.get)
      else Action {
        createChatroom(roomName).onSuccess {
          case _ =>
        }
      }

    case joinLobby: JoinLobby =>
      if(nicknames.contains(joinLobby.sender)) Ignore
      else Action {
        nicknames += (joinLobby.sender -> joinLobby.nickname)
      }

    case send: Send =>
      val room = rooms(send.roomName)
      Route(room)
  }


  def createChatroom(name: String): Future[ActorRef] = {
    val future = context distribute new Chatroom(name)
    future foreach (room => rooms += (name -> room))
    future
  }


  def getLoad = AbsoluteLoad(3.0f)
}

object Chat {
  val CHARSET = Charset.forName("UTF-8")
  val PORT = 29359
  case class GetNickname(connection: Connection)
}

