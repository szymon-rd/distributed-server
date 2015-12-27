package pl.jaca.server.networking

import akka.actor.Actor
import io.netty.channel.Channel
import pl.jaca.server.networking.ConnectionProxy._
import pl.jaca.server.packets.OutPacket

import scala.concurrent.Future

/**
 * @author Jaca777
 *         Created 2015-10-11 at 15
 */
class ConnectionProxy(val channel: Channel) extends Actor {
  implicit val executionContext = context.dispatcher

  override def receive = stateful(None)

  def stateful(state: Option[Any]): Receive = {
    case UpdateState(action) =>
      val newState = action(state)
      context become stateful(Some(newState))
    case UpdateStateFuture(action) =>
      val futureState = action(state)
      for(newState <- futureState)
        context become stateful(Some(newState))
    case WithState(action) => action(state)
    case ForwardPacket(packet) => channel.writeAndFlush(packet)
    case GetChannel => sender ! ProxyChannel(channel)
  }

}

object ConnectionProxy {

  //IN
  case class ForwardPacket(outPacket: OutPacket)

  case class UpdateState(f: (Option[Any] => Any))

  case class UpdateStateFuture(f: (Option[Any] => Future[Any]))

  case class WithState(action: (Option[Any] => Unit))


  object GetChannel

  //OUT
  case class ProxyChannel(channel: Channel)

}
