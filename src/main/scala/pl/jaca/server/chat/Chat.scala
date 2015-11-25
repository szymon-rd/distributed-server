package pl.jaca.server.chat

import java.nio.charset.Charset

import akka.actor.{ActorRef, Props}
import pl.jaca.server.chat.packets.ChatPacketResolver
import pl.jaca.server.chat.packets.in.JoinRoom
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
  implicit val executionContext = context.dispatcher

  var nicknames = Map[Connection, String]()
  var rooms = Map[String, ActorRef]()
  var userRoom = Map[Connection, ActorRef]()
  val server = context.actorOf(Props(new Server(port = Chat.PORT, resolver = ChatPacketResolver)))

  implicit val stream = AsyncEventStream()

  stream react {
    case joinRoom: JoinRoom =>
      val roomName = joinRoom.channelName
      val room = rooms.get(roomName)
      if (room.isDefined) Route(room.get)
      else Action {
        //stream emit CreateChatroom(roomName) WRONG WAY!
      }

/*    case CreateChatroom(name) => Action { WRONG WAY!

    }*/

  }


  def createChatroom(name: String): Future[ActorRef] = {
    val future = context distribute new Chatroom(name)
    future foreach (room => rooms += (name -> room))
    future
  }

  /*  /*  def receive: Receive = {
           case CreateChatroom(name) =>
             createChatroom(name)
           case UserCreateChatroom(name, creator) =>
             val future = createChatroom(name)
             for (ref <- future) {
               userRoom += (creator -> ref)
               ref ! Chatroom.Join(nicknames(creator), creator)
             }*/
    case _ =>
  }*/


  /*  val packetsSubscriber = packetsObservable.foreach(_.foreach({
      case packet: ChatroomPacket =>

      case packet: JoinLobby =>
        nicknames += (packet.sender -> packet.nickname)
        packet.sender.write(new ChatAnnouncement(s"Hello ${packet.nickname}!"))
      case packet: JoinRoom =>
        if (rooms.contains(packet.channelName)) {
          val roomRef = rooms(packet.channelName)
          userRoom += (packet.sender -> roomRef)
          roomRef ! Chatroom.Join(nicknames(packet.sender), packet.sender)
        } else {
          self ! UserCreateChatroom(packet.channelName, packet.sender)
        }
    }))*/


  def getLoad = AbsoluteLoad(1.0f)
}

object Chat {
  val CHARSET = Charset.forName("UTF-8")
  val PORT = 29359

  case class CreateChatroom(name: String)

  case class UserCreateChatroom(name: String, creator: Connection)

}

