package pl.jaca.server.newchat.userauth

import akka.actor.Actor
import pl.jaca.server.proxy.Connection

/**
 * @author Jaca777
 *         Created 2015-11-25 at 18
 */
class UserAuth extends Actor {
  def receive: Receive = {
    case _ =>
  }

}

object UserAuth {
  case class GetUser(connection: Connection)
  case class User(chatUser: ChatUser)
}
