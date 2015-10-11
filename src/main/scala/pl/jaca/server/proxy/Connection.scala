package pl.jaca.server.proxy

import akka.actor.ActorRef
import pl.jaca.server.proxy.packets.OutPacket
import pl.jaca.server.proxy.server.ConnectionProxy.ForwardPacket


/**
 * @author Jaca777
 *         Created 2015-06-17 at 20
 */
@SerialVersionUID(150913001L)
class Connection(val host: String, val port: Int, val proxy: ActorRef) extends Serializable {

  def write(packet: OutPacket): Unit = {
    proxy.tell(ForwardPacket(packet, this), null)
  }

  override def hashCode(): Int = host.hashCode + port

  override def equals(obj: Any): Boolean = obj match {
    case c: Connection => c.host == this.host && c.port == this.port
    case any => false
  }
}

