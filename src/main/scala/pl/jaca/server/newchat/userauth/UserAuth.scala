package pl.jaca.server.newchat.userauth

import akka.actor.Actor
import pl.jaca.server.{Session}
import pl.jaca.server.newchat.model.domain.ChatUser

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
  case class GetUser(connection: Session)
  case class User(chatUser: ChatUser)
}
