package example.newchat.model.domain

import akka.actor.Actor
import example.newchat.model.domain.AccountStorage.{Result, Create, Get}
import pl.jaca.cluster.distribution.Distributable

/**
 * @author Jaca777
 *         Created 2015-12-25 at 19
 */
class AccountStorage extends Actor with Distributable {
  var accounts = Map[String, Account]("admin" -> new Account("admin", "admin"))

  override def receive: Receive = {
    case Get(name, pass) =>
      println(name + " " + pass)
      sender ! Result(auth(name, pass))
    case Create(name, pass) =>
      accounts += (name -> new Account(name, pass))
  }

  def auth(name: String, pass: String) = {
    accounts.get(name).collect {
      case a@Account(_, `pass`) => a
    }
  }
}

object AccountStorage {

  case class Get(name: String, pass: String)

  case class Create(name: String, pass: String)

  case class Result(account: Option[Account])

}
