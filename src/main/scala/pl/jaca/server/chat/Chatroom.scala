package pl.jaca.server.chat

import akka.actor.Actor
import pl.jaca.server.chat.Chatroom.Join
import pl.jaca.server.chat.packets.in.Send
import pl.jaca.server.chat.packets.out.ChatAnnouncement
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Load, Distributable}
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.server.Server
import Server.PacketReceived
import pl.jaca.server.proxy.packets.OutPacket

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Chatroom(val roomName: String) extends Actor with Distributable{
  println(s"Chatroom $roomName created!")

  var users = Map[Connection, String]()

  override def receive: Receive = {
    case m: PacketReceived => handleMessage(m)
    case Join(nick, connection) =>
      users += (connection -> nick)
      sendToAll(new ChatAnnouncement(s"Welcome $nick to $roomName!"))
  }

  def handleMessage(m: PacketReceived) = m.packet match {
    case s: Send =>
      val nick = users(s.sender)
      sendToAll(new ChatAnnouncement(s"$nick: ${s.message}"))
  }

  def sendToAll(m: OutPacket) = users.foreach(_._1.write(m))

  override def getLoad: Load = AbsoluteLoad(1.0f)
}
object Chatroom {
  case class Join(nick: String, channel: Connection)
}
