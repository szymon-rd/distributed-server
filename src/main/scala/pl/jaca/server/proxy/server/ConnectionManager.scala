package pl.jaca.server.proxy.server

import java.net.InetSocketAddress

import akka.actor.ActorRef
import io.netty.channel.Channel
import pl.jaca.server.proxy.Connection

/**
 * @author Jaca777
 *         Created 2015-06-13 at 15
 */
class ConnectionManager(proxyFactory: Channel => ActorRef) {
  var connections = Set.empty[Connection]

  def getConnection(channel: Channel): Option[Connection] = connections.find(_.channelEquals(channel))

  def getConnections(f: Connection => Boolean): Set[Connection] = connections.filter(f)

  def getAllConnections: Set[Connection] = connections

  def createConnection(channel: Channel): Unit = {
    val address = channel.remoteAddress().asInstanceOf[InetSocketAddress]
    connections += new Connection(address.getHostString, address.getPort, channel, proxyFactory(channel))
  }

  def removeConnection(channel: Channel) {
    connections = connections.filter(_ != channel)
  }

}
