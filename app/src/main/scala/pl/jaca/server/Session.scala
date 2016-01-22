package pl.jaca.server

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorRef
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.channel.{Channel, ChannelId}
import pl.jaca.server.networking.SessionProxy._
import pl.jaca.server.packets.OutPacket

import scala.concurrent.Future


/**
  * @author Jaca777
  *         Created 2015-06-17 at 20
  */
@SerialVersionUID(201024001L)
class Session(val host: String, val port: Int, channelID: ChannelId, val proxy: ActorRef) extends Serializable {


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

  override def hashCode(): Int = channelID.hashCode()

  override def equals(obj: Any): Boolean =
    obj match {
      case session: Session =>
        session.hashCode() == hashCode()
      case _ => false
    }

  def channelEquals(channel: Channel): Boolean = channelID == channel.id()
}

object Session {

  object NoSession extends Session(null, -1, new EmbeddedChannel().id(), ActorRef.noSender) {
    override def write(packet: OutPacket) {
      throw new UnsupportedOperationException
    }
  }

}

