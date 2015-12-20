package pl.jaca.server.networking

import pl.jaca.server.Connection
import pl.jaca.server.packets.InPacket


/**
 * @author Jaca777
 *         Created 2015-06-13 at 17
 */
abstract class PacketResolver {

  type Resolve = PartialFunction[Short, (Short, Short, Array[Byte], Connection) => InPacket]

  def resolve(id: Short, length: Short, data: Array[Byte], sender: Connection): InPacket = (resolve orElse unknown)(id)(id, length, data, sender)

  private val unknown: Resolve = {
    case _ => UnknownPacket
  }
  def resolve: Resolve

  def and(resolver: PacketResolver): PacketResolver = {
    new PacketResolver {
      override def resolve: Resolve = resolver.resolve orElse PacketResolver.this.resolve
    }
  }
}
case class UnknownPacket(i: Short, l: Short, data: Array[Byte], s: Connection) extends InPacket(i,l,data,s)
