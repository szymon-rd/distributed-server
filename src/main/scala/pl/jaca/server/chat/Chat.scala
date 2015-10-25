package pl.jaca.server.chat

import java.nio.charset.Charset

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import pl.jaca.server.chat.Chat.{CreateChatroom, UserCreateChatroom}
import pl.jaca.server.chat.Chatroom.ListenAt
import pl.jaca.server.chat.packets.ChatPacketResolver
import pl.jaca.server.chat.packets.in.{ChatroomPacket, JoinLobby, JoinRoom}
import pl.jaca.server.chat.packets.out.ChatAnnouncement
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Distributable, Distribution}
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.server.Server.{GetEventObservable, REventObservable}
import pl.jaca.server.proxy.server.{PacketReceived, Server}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Chat extends Actor with Distribution with Distributable {
  implicit val executionContext = context.dispatcher
  implicit val askTimeout = Timeout(2 seconds)

  var nicknames = Map[Connection, String]()
  var rooms = Map[String, ActorRef]()
  var userRoom = Map[Connection, ActorRef]()
  val server = context.actorOf(Props(new Server(port = Chat.PORT, resolver = ChatPacketResolver)))
  val packetsObservable = (server ? GetEventObservable).mapTo[REventObservable]
    .map(_.subject).map(_.filter(_.isInstanceOf[PacketReceived]).map(_.asInstanceOf[PacketReceived]).map(_.inPacket))

  def receive: Receive = {
    case CreateChatroom(name) =>
      createChatroom(name)
    case UserCreateChatroom(name, creator) =>
      val future = createChatroom(name)
      for (ref <- future) {
        userRoom += (creator -> ref)
        ref ! Chatroom.Join(nicknames(creator), creator)
      }
  }

  def createChatroom(name: String): Future[ActorRef] = {
    val future = context.distribute(new Chatroom(name))
    val packetsFuture = packetsObservable.map(_.filter(_.isInstanceOf[ChatroomPacket]).map(_.asInstanceOf[ChatroomPacket]))
    for {
      ref <- future
      packets <- packetsFuture
    } {
      rooms += (name -> ref)
      ref ! ListenAt(packets.filter(packet => userRoom(packet.sender) == ref))
    }
    future
  }

  val packetsSubscriber = packetsObservable.foreach(_.foreach({
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
  }))


  def getLoad = AbsoluteLoad(1.0f)
}

object Chat {
  val CHARSET = Charset.forName("UTF-8")
  val PORT = 29359

  case class CreateChatroom(name: String)

  case class UserCreateChatroom(name: String, creator: Connection)

}

