package pl.jaca.server.proxy

import java.net.InetSocketAddress

import akka.actor.ActorRef
import io.netty.channel.Channel

/**
 * @author Jaca777
 *         Created 2015-06-13 at 15
 */
class ConnectionManger(val serverRef: ActorRef) {
  var connections = Set[(Connection, Channel)]()

  def getConnection(channel: Channel): Connection = {
    connections.find(_._2 == channel).map(_._1).get
  }

  def getChannel(connection: Connection): Channel = {
    connections.find(_._1 == connection).map(_._2).get
  }

  def getConnections(f: Connection => Boolean): Set[Connection] = connections.map(_._1).filter(f)

  def getAllConnections: Set[Connection] = connections.map(_._1)

  def addConnection(channel: Channel): Unit = {
    val address = channel.remoteAddress().asInstanceOf[InetSocketAddress]
    connections += ((new Connection(address.getHostString, address.getPort, serverRef), channel))
  }

  def removeConnection(channel: Channel) {
    connections = connections.filter(_._2 != channel)
  }


}
