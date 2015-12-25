package example.newchat.model.sessionstate

import pl.jaca.server.Session

/**
 * @author Jaca777
 *         Created 2015-12-24 at 01
 */
class LoggedUser(val nick: String, session: Session) {
  def writeChatroomMsg(roomName: String, message: String) {
    //session.write(RoomM)
  }

  def writeGlobalMsg(message: String) {

  }
}
