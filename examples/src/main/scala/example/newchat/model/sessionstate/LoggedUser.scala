package example.newchat.model.sessionstate

import example.newchat.packets.out.{ChatroomJoined, RoomMessage}
import pl.jaca.server.Session

/**
 * @author Jaca777
 *         Created 2015-12-24 at 01
 */
class LoggedUser(val name: String, session: Session) extends Serializable {
  def writeChatroomMsg(roomName: String, message: String) {
    session.write(new RoomMessage(roomName, message))
  }
  def chatroomJoined(roomName: String): Unit = {
    session.write(new ChatroomJoined(roomName))
  }
}
