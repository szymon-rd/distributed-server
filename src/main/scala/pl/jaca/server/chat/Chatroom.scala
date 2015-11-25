package pl.jaca.server.chat

import akka.actor.Actor
import pl.jaca.server.chat.Chatroom.{ListenAt, Join, Message}
import pl.jaca.server.chat.packets.in.{Send, ChatroomPacket}
import pl.jaca.server.chat.packets.out.ChatAnnouncement
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Distributable, Load}
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.OutPacket
import rx.lang.scala.{Subscription, Observable}

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Chatroom(val roomName: String) extends Actor with Distributable{
  println(s"Chatroom $roomName created!")

  var users = Map[Connection, String]()
  var currentSubscription = Subscription()

  override def receive: Receive = {
    case Join(nick, connection) =>
      users += (connection -> nick)
      sendToAll(new ChatAnnouncement(s"Welcome $nick to $roomName!"))
    case Message(sender, msg) =>
      val nick = users(sender)
      sendToAll(new ChatAnnouncement(s"$nick: $msg"))
      throw new RuntimeException(s"$nick: $msg")
    case ListenAt(observable) =>
      currentSubscription.unsubscribe()
      currentSubscription = observable subscribe(_ match {
        case s: Send => self ! Message(s.sender, s.message)
      })
  }

  def sendToAll(m: OutPacket) = users.foreach(_._1.write(m))

  override def getLoad: Load = AbsoluteLoad(1.0f)
}
object Chatroom {
  //In
  case class Join(nick: String, connection: Connection)
  case class Message(connection: Connection, msg: String)
  case class ListenAt(observable: Observable[ChatroomPacket])
}
