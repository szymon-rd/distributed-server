package example.newchat.model.domain

import akka.actor.Actor
import example.newchat.model.domain.Chatroom._
import example.newchat.model.sessionstate.LoggedUser
import pl.jaca.cluster.distribution.Distributable

/**
 * @author Jaca777
 *         Created 2015-12-17 at 19
 */
class Chatroom(val name: String) extends Actor with Distributable {
  var users: Set[LoggedUser] = Set()

  override def receive: Receive = {
    case Join(user) => users += user
    case Message(user: LoggedUser, msg: String) => sendMessage(user, msg)
  }

  def sendMessage(user: LoggedUser, msg: String) = {
    val message = s"${user.name}: $msg"
    broadcast(message)
  }

  def broadcast(msg: String) = users.foreach(_.writeChatroomMsg(name, msg))
}

object Chatroom {

  case class Join(user: LoggedUser)

  case class Message(user: LoggedUser, msg: String)

}
