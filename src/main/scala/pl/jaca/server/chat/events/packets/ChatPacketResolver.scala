package pl.jaca.server.chat.events.packets

import pl.jaca.server.chat.events.packets.in.{JoinLobby, JoinRoom, Send}
import pl.jaca.server.proxy.server.PacketResolver

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
