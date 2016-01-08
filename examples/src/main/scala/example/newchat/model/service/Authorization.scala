package example.newchat.model.service

import akka.actor.ActorRef
import akka.util.Timeout
import example.newchat.model.domain.{Account, AccountStorage}
import example.newchat.model.domain.AccountStorage.{Result, Get}
import example.newchat.model.service.Authorization._
import example.newchat.model.sessionstate.{LoggedUser, NotLoggedUser}
import akka.pattern._
import pl.jaca.server.Session
import pl.jaca.server.service.Service
import scala.concurrent.duration._

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-12-17 at 18
 */
class Authorization extends Service {
  implicit val executionContext = context.dispatcher
  implicit val timeout = Timeout(2 seconds)

  var accountStorage: Future[ActorRef] = context.distribute(new AccountStorage, "account-storage")

  def receive: Receive = {
    case Login(login, password, session) =>
      session.mapStateToFuture {
        case Some(l: NotLoggedUser) =>
          auth(login, password, l)
        case Some(l: LoggedUser) => Future(l)
      }
    case Register(login, password) =>
      accountStorage.map(_ ! AccountStorage.Create(login, password))
  }

  def auth(login: String, password: String, l: NotLoggedUser): Future[Object] = {
    getAccount(login, password).map {
      case Some(account) =>
        l.loginSuccess(account)
      case None => l.loginFailure()
    }
  }

  def getAccount(login: String, password: String): Future[Option[Account]] = {
    accountStorage.flatMap {
      case storage =>
        storage ? Get(login, password)
    } collect {
      case Result(account) =>
        account
    }
  }

}

object Authorization {

  case class Login(login: String, password: String, session: Session)

  case class Register(login: String, password: String)

}