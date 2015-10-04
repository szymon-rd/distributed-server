package pl.jaca.server.chat

import java.nio.charset.Charset

import akka.actor.{Actor, ActorRef, Props}
import pl.jaca.server.chat.Chat.{CreateChatroom, UserCreateChatroom}
import pl.jaca.server.chat.packets.ChatPacketResolver
import pl.jaca.server.chat.packets.in.{ChatroomPacket, JoinLobby, JoinRoom}
import pl.jaca.server.chat.packets.out.ChatAnnouncement
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Distributable, Distribution}
import pl.jaca.server.proxy.Server.Message
import pl.jaca.server.proxy.{Connection, Server}

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Chat extends Actor with Distribution with Distributable {
  implicit val executionContext = context.dispatcher

  var nicknames = Map[Connection, String]()
  var rooms = Map[String, ActorRef]()
  var userRoom = Map[Connection, ActorRef]()
  val server = context.actorOf(Props(new Server(port = Chat.PORT, resolver = ChatPacketResolver)))

  def receive: Receive = {
    case m: Message => handleMessage(m)
    case CreateChatroom(name) =>
      context.distribute(new Chatroom(name)).foreach(ref => rooms += (name -> ref))
    case UserCreateChatroom(name, creator) =>
      val ref = context.distribute(new Chatroom(name))
      ref.foreach(ref => rooms += (name -> ref))
      ref.foreach(ref => userRoom += (creator -> ref))
      ref.foreach(_ ! Chatroom.Join(nicknames(creator), creator))
  }

  def handleMessage(m: Message): Unit = m.packet match {
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
    case roomPacket: ChatroomPacket =>
      userRoom(roomPacket.sender) ! m
  }

  def getLoad = AbsoluteLoad(1.0f)
}

object Chat {
  val CHARSET = Charset.forName("UTF-8")
  val PORT = 29359

  case class CreateChatroom(name: String)

  case class UserCreateChatroom(name: String, creator: Connection)

}

