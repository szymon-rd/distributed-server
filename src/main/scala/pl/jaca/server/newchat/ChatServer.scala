package pl.jaca.server.newchat

import akka.actor.{Actor, Props}
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Distributable, Distribution, Load}
import pl.jaca.server.newchat.messaging.{PrivateMessaging, SharedMessaging}
import pl.jaca.server.newchat.packets.ChatPacketResolver
import pl.jaca.server.newchat.userauth.AuthenticationHandler
import pl.jaca.server.proxy.server.Server
import pl.jaca.server.proxy.server.Server.Subscribe

/**
 * @author Jaca777
 *         Created 2015-11-25 at 17
 */
class ChatServer extends Actor with Distribution with Distributable {

  val server = context.actorOf(Props(new Server(1337, ChatPacketResolver)), "server")
  val authFuture = context.distribute(new AuthenticationHandler, "userAuth")
  val pmFuture = context.distribute(new PrivateMessaging, "pMessaging")
  val smFuture = context.distribute(new SharedMessaging, "sMessaging")

  for {
    authHandler <- authFuture
    pMessagingHandler <- pmFuture
    sMessagingHandler <- smFuture
  } {
    server ! Subscribe(authHandler)
    server ! Subscribe(pMessagingHandler)
    server ! Subscribe(sMessagingHandler)
  }
  
  override def receive: Receive = {
    case _ =>
  }

  override def getLoad: Load = AbsoluteLoad(5.0f)
}
