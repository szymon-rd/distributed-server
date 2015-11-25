package pl.jaca.server.newchat.userauth

import akka.actor.Actor
import akka.pattern._
import akka.util.Timeout
import pl.jaca.server.cluster.Application
import pl.jaca.server.newchat.userauth.UserAuth.{GetUser, User}
import pl.jaca.server.proxy.Connection

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * @author Jaca777
 *         Created 2015-11-25 at 17
 */
trait Authentication extends Actor {
  implicit val timeout = Timeout(2 seconds)
  private val authActor = context.actorSelection(Application.appActorPath + "/chat/userAuth")

  def user(connection: Connection): Future[ChatUser] = (authActor ? GetUser(connection)).mapTo[User].map(_.chatUser)

}
