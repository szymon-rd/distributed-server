package example.newchat.model.sessionstate

import example.newchat.model.domain.Account
import example.newchat.packets.out.{LoginFailure, LoginSuccess}
import pl.jaca.server.Session

/**
 * @author Jaca777
 *         Created 2015-12-24 at 01
 */
class NotLoggedUser(session: Session) {
  def loginFailure(): NotLoggedUser = {
    session.write(LoginFailure)
    this
  }
  def loginSuccess(account: Account): LoggedUser = {
    session.write(LoginSuccess)
    new LoggedUser(account.name, this.session)
  }

}
