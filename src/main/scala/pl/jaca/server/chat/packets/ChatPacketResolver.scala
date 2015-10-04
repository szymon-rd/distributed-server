package pl.jaca.server.chat.packets

import io.netty.buffer.ByteBuf
import pl.jaca.server.chat.packets.in.{Send, JoinRoom, JoinLobby}
import pl.jaca.server.proxy.PacketResolver
import pl.jaca.server.proxy.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-06-13 at 15
 */
object ChatPacketResolver extends PacketResolver{
  def resolve: Resolve = {
    case 11 => JoinLobby
    case 12 => JoinRoom
    case 13 => Send
  }

}
