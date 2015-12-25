package example.newchat.model.domain

import example.newchat.model.sessionstate.LoggedUser

/**
 * @author Jaca777
 *         Created 2015-12-17 at 19
 */
case class Chatroom(users: List[LoggedUser]) { //TODO Actor
}
