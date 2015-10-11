package pl.jaca.server.proxy.server

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.pattern._
import io.netty.channel.Channel
import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.server.ConnectionProxy.GetConnection

import scala.concurrent.Future

/**
 * @author Jaca777
 *         Created 2015-06-13 at 15
 */
class ConnectionManger {
  var connections = Set[Connection]()
  val proxies = Set[ActorRef]()

  def getConnection(channel: Channel): Future[Connection] = ???
  //TODO

  def getConnections(f: Connection => Boolean): Set[Connection] = connections.filter(f)

  def getAllConnections: Set[Connection] = connections

  def addConnection(channel: Channel): Unit = {
    val address = channel.remoteAddress().asInstanceOf[InetSocketAddress]
    connections += new Connection(address.getHostString, address.getPort, channel)
  }

  def removeConnection(channel: Channel) {
    connections = connections.filter(_.channel != channel)
  }


}
