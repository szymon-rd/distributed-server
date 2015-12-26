package example.newchat.model.domain

import akka.actor.{ActorRef, Actor}
import example.newchat.model.domain.ChatroomStorage._
import pl.jaca.cluster.distribution.{Distribution, Distributable}

import scala.concurrent.Future

/**
 * @author Jaca777
 *         Created 2015-12-25 at 21
 */
class ChatroomStorage extends Actor with Distributable with Distribution {
  implicit val executionContext = context.dispatcher

  var runningChatrooms = Map[String, ActorRef]()

  override def receive: Receive = {
    case Get(name) =>
      if (!runningChatrooms.contains(name)) {
        val chatroom = createChatroom(name)
        val currSender = sender
        chatroom.foreach(currSender ! _)
      } else sender ! Result(runningChatrooms(name))
  }

  def createChatroom(name: String): Future[ActorRef] = {
    val chatroomFuture = context.distribute(new Chatroom(name), s"chatroom-$name")
    chatroomFuture.foreach(chatroom => runningChatrooms += (name -> chatroom))
    chatroomFuture
  }


}

object ChatroomStorage {

  case class Get(name: String)

  case class Result(chatroom: ActorRef)

}
