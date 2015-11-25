package pl.jaca.server.newchat.packets

import pl.jaca.server.proxy.server.{UnknownPacket, PacketResolver}

/**
 * @author Jaca777
 *         Created 2015-06-13 at 15
 */
object ChatPacketResolver extends PacketResolver{
  def resolve: Resolve = {
    case _ => UnknownPacket
  }
}
