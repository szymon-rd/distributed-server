package pl.jaca.server

import akka.actor.ActorRef
import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import pl.jaca.server.packets.OutPacket
import pl.jaca.server.networking.ConnectionProxy
import ConnectionProxy._


/**
 * @author Jaca777
 *         Created 2015-06-17 at 20
 */
@SerialVersionUID(201024001L)
class Connection(val host: String, val port: Int, channel: Channel, val proxy: ActorRef) extends Serializable {

  private val channelID = channel.id()

  def write(packet: OutPacket) {
    proxy.tell(ForwardPacket(packet), ActorRef.noSender)
  }

  def mapSessionState(f: (Option[Any] => Any)) = {
    proxy.tell(UpdateState(f), ActorRef.noSender)
  }

  def withSessionState(activity: (Option[Any] => Unit)) = {
    proxy.tell(WithState(activity), ActorRef.noSender)
  }

  override def hashCode(): Int = channel.hashCode()

  override def equals(obj: Any): Boolean =
    obj match {
      case connection: Connection =>
        channelID == connection.channelID
      case _ => false
    }

  def channelEquals(channel: Channel): Boolean = channelID == channel.id()
}

object Connection {

  object NoConnection extends Connection(null, -1, new EmbeddedChannel, ActorRef.noSender) {
    override def write(packet: OutPacket) {
      throw new UnsupportedOperationException
    }
  }

}

