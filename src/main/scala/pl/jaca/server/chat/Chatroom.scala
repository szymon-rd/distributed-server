package pl.jaca.server.chat

import pl.jaca.server.chat.events.packets.in.JoinRoom
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Distributable, Load}
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.eventhandling.EventActor
import pl.jaca.server.proxy.packets.OutPacket

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Chatroom(val roomName: String) extends EventActor with Distributable{
  println(s"Chatroom $roomName created!")

  var users = Map[Connection, String]()
  val asyncEventStream = AsyncEventStream()

  asyncEventStream react {
    case joinRoom: JoinRoom =>
      if(users.contains(joinRoom.sender)) Ignore
      else Action {

      }
  }


  def sendToAll(m: OutPacket) = users.foreach(_._1.write(m))

  override def getLoad: Load = AbsoluteLoad(1.0f)

}

