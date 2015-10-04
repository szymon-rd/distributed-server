package pl.jaca.server.proxy

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import pl.jaca.server.proxy.packets.OutPacket

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 */
class PacketEncoder extends MessageToByteEncoder[OutPacket]{
  override def encode(ctx: ChannelHandlerContext, packet: OutPacket, out: ByteBuf) {
    packet.store(out)
  }
}
