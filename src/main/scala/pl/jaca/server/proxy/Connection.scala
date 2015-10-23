package pl.jaca.server.proxy


import akka.actor.ActorRef
import io.netty.channel.Channel
import pl.jaca.server.proxy.packets.OutPacket
import pl.jaca.server.proxy.server.ConnectionProxy.ForwardPacket


/**
 * @author Jaca777
 *         Created 2015-06-17 at 20
 */
@SerialVersionUID(201024001L)
class Connection(val host: String, val port: Int, channel: Channel, val proxy: ActorRef) extends Serializable {

  private val channelID = channel.id()

  def write(packet: OutPacket): Unit = {
    proxy.tell(ForwardPacket(packet), ActorRef.noSender)
  }

  def channelEquals(channel: Channel) = channel.id == channelID

  override def hashCode(): Int = host.hashCode + port

  override def equals(obj: Any): Boolean = obj match {
    case c: Connection => c.host == this.host && c.port == this.port
    case any => false
  }
}

