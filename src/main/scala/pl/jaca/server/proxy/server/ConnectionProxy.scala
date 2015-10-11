package pl.jaca.server.proxy.server

import java.io.IOException

import akka.actor.Actor
import io.netty.channel.Channel

import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.OutPacket
import pl.jaca.server.proxy.server.ConnectionProxy.{ConnectionFound, GetConnection, ForwardPacket}

/**
 * @author Jaca777
 *         Created 2015-10-11 at 15
 */
class ConnectionProxy extends Actor {
  var connections: Set[(Connection, Channel)] = Set()

  override def receive: Receive = {
    case ForwardPacket(packet, receiver) =>
      val channel = connections.find(_._1 == receiver).map(_._2)
      if(channel.isDefined) channel.get.writeAndFlush(packet)
      else throw new IOException("Unable to send packet - unrecognized connection.")
    case GetConnection(channel: Channel) =>
      val connection = connections.find(_._2 == channel).map(_._1)
      if(connection.isDefined) sender ! ConnectionFound(connection.get)
  }


}

object ConnectionProxy {

  //IN
  case class ForwardPacket(outPacket: OutPacket, connection: Connection)

  case class GetConnection(channel: Channel)

  //OUT
  case class ConnectionFound(connection: Connection)
}
