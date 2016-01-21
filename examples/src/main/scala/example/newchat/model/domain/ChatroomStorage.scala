package example.newchat.model.domain

import akka.actor.{Actor, ActorRef}
import example.newchat.model.domain.ChatroomStorage._
import pl.jaca.cluster.distribution.{Distributable, Distribution}

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
        currSender ! Result(chatroom)
      } else sender ! Result(runningChatrooms(name))
  }

  def createChatroom(name: String): ActorRef = {
    val chatroom = context.distribute(new Chatroom(name), s"chatroom-$name")
    runningChatrooms += (name -> chatroom)
    chatroom
  }


}

object ChatroomStorage {

  case class Get(name: String)

  case class Result(chatroom: ActorRef)

}
