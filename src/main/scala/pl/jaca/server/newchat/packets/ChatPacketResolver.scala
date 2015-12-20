package pl.jaca.server.newchat.packets

import pl.jaca.server.networking.PacketResolver

/**
 * @author Jaca777
 *         Created 2015-06-13 at 15
 */
object ChatPacketResolver extends PacketResolver{
  def resolve: Resolve = {
    case 0 => in.Login
    case 1 => in.Register
    case 2 => in.JoinChat
    case 3 => in.JoinChatroom
    case 4 => in.SendMessage
  }
}
