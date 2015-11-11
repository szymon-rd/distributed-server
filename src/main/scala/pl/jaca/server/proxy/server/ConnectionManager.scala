package pl.jaca.server.proxy.server

import java.net.InetSocketAddress

import akka.actor.ActorRef
import io.netty.channel.Channel
import pl.jaca.server.proxy.Connection

/**
 * @author Jaca777
 *         Created 2015-06-13 at 15
 */
class ConnectionManager(proxyFactory: Channel => ActorRef, server: ActorRef) {
  var connections = Set.empty[Connection]

  def getConnection(channel: Channel): Option[Connection] = connections.find(_.channelEquals(channel))

  def getConnections(f: Connection => Boolean): Set[Connection] = connections.filter(f)

  def getAllConnections: Set[Connection] = connections

  def createConnection(channel: Channel): Unit = {
    if (getConnection(channel).isEmpty) {
      val address = channel.remoteAddress().asInstanceOf[InetSocketAddress]
      val connection = new Connection(address.getHostString, address.getPort, channel, proxyFactory(channel))
      connections += connection
      server ! Server.EventOccurred(ConnectionActive(connection))
    }
  }

  def removeConnection(channel: Channel) {
    val connection = getConnection(channel)
    if (connection.isDefined) {
      connections -= connection.get
      server ! Server.EventOccurred(ConnectionInactive(connection.get))
    }
  }

}