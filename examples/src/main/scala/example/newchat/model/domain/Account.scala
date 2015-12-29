package example.newchat.model.domain

import pl.jaca.server.db.DbAccessor
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}


/**
 * @author Jaca777
 *         Created 2015-12-25 at 14
 */
case class Account(name: String, password: String)

class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
  def name = column[String]("username")

  def password = column[String]("password")

  def * = (name, password) <>((Account.apply _).tupled, Account.unapply)
}

object AccountDAO {
  import DbAccessor._

  private val users = TableQuery[AccountTable]

  def all(): Future[Seq[Account]] = Db.run(users.result)

  private def get(name: String)(implicit ec: ExecutionContext): Future[Seq[Account]] = {
    Db.run({
     users.filter(_.name === name).result
    })
  }

  def forName(name: String)(implicit ec: ExecutionContext): Future[Option[Account]] = get(name).map {
    case a :: _ => Some(a)
    case Seq() => None
  }

  def insert(user: Account)(implicit ec: ExecutionContext): Future[Unit] = {
    Db.run(users += user).map { _ => () }
  }
}