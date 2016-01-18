package pl.jaca.server

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorRef
import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import pl.jaca.server.packets.OutPacket
import pl.jaca.server.networking.SessionProxy
import SessionProxy._

import scala.concurrent.Future


/**
 * @author Jaca777
 *         Created 2015-06-17 at 20
 */
@SerialVersionUID(201024001L)
class Session(val host: String, val port: Int, channel: Channel, val proxy: ActorRef) extends Serializable {

  private val channelID = channel.id()

  private[server] val packetsQueueSize = new AtomicInteger()

  def write(packet: OutPacket) {
    proxy ! ForwardPacket(packet)
  }

  def mapState(f: (Option[Any] => Any)) = {
    proxy ! UpdateState(f)
  }

  def mapStateFuture(f: (Option[Any] => Future[Any])) = {
    proxy ! UpdateStateFuture(f)
  }

  def withState(action: (Option[Any] => Unit)) = {
    proxy ! WithState(action)
  }

  override def hashCode(): Int = channel.hashCode()

  override def equals(obj: Any): Boolean =
    obj match {
      case connection: Session =>
        channelID == connection.channelID
      case _ => false
    }

  def channelEquals(channel: Channel): Boolean = channelID == channel.id()
}

object Session {

  object NoSession extends Session(null, -1, new EmbeddedChannel, ActorRef.noSender) {
    override def write(packet: OutPacket) {
      throw new UnsupportedOperationException
    }
  }

}

