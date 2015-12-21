package pl.jaca.server.networking

import akka.actor.Actor
import io.netty.channel.Channel
import pl.jaca.server.networking.ConnectionProxy._
import pl.jaca.server.packets.OutPacket

/**
 * @author Jaca777
 *         Created 2015-10-11 at 15
 */
class ConnectionProxy(val channel: Channel) extends Actor {

  override def receive = stateful(None)

  def stateful(state: Option[Any]): Receive = {
    case UpdateState(action) =>
      val newState = action(state)
      context become stateful(Some(newState))
    case WithState(action) => action(Some(state))
    case ForwardPacket(packet) => channel.writeAndFlush(packet)
    case GetChannel => sender ! ProxyChannel(channel)
  }

}

object ConnectionProxy {

  //IN
  case class ForwardPacket(outPacket: OutPacket)

  case class UpdateState(f: (Option[Any] => Any))

  case class WithState(activity: (Option[Any] => Unit))


  object GetChannel

  //OUT
  case class ProxyChannel(channel: Channel)

}
