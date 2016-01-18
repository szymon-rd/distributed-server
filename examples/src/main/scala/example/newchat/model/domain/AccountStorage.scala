package example.newchat.model.domain

import akka.actor.Actor
import example.newchat.model.domain.AccountStorage.{Create, Get, Result}
import pl.jaca.cluster.distribution.Distributable

import scala.concurrent.Future

/**
 * @author Jaca777
 *         Created 2015-12-25 at 19
 */
class AccountStorage extends Actor with Distributable {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case Get(name, pass) =>
      val currSender = sender()
      auth(name, pass).foreach {
        account =>
        currSender ! Result(account)
      }
    case Create(name, pass) =>
      createAccount(name, pass)
  }

  def createAccount(name: String, pass: String): Unit = {
    AccountDAO.insert(new Account(name, pass))
  }

  def auth(name: String, pass: String): Future[Option[Account]] = {
    val future = AccountDAO.forName(name)
    future.map({
      _.filter(_.password == pass)
    })
  }
}

object AccountStorage {

  case class Get(name: String, pass: String)

  case class Create(name: String, pass: String)

  case class Result(account: Option[Account])

}
