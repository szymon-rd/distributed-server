package pl.jaca.server.newchat.userauth

import akka.actor.Actor
import akka.pattern._
import akka.util.Timeout
import pl.jaca.cluster.Application
import pl.jaca.server.{Session, Session$}
import pl.jaca.server.newchat.model.domain.ChatUser
import pl.jaca.server.newchat.userauth.UserAuth.{GetUser, User}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * @author Jaca777
 *         Created 2015-11-25 at 17
 */
trait Authentication extends Actor {
  implicit val executionContext = context.dispatcher
  implicit val timeout = Timeout(2 seconds)
  private val authActor = context.actorSelection(Application.appActorPath + "/chat/userAuth")

  def user(connection: Session): Future[ChatUser] = (authActor ? GetUser(connection)).mapTo[User].map(_.chatUser)

}
