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

  override def hashCode(): Int = channel.hashCode()

  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[Connection]) {
      val connection = obj.asInstanceOf[Connection]
      channelID == connection.channelID
    } else false

  def channelEquals(channel: Channel): Boolean = channelID == channel.id()
}

