package pl.jaca.server.proxy.server

import java.util

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 */
class PacketDecoder(resolver: PacketResolver, connectionManger: ConnectionManager) extends ByteToMessageDecoder {
  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]) {
    if (in.readableBytes() >= 4) {
      val size = in.getShort(2)
      if (in.readableBytes() >= size) {
        val id = in.readShort()
        in.skipBytes(2)
        val bytes = new Array[Byte](size - 4)
        in.readBytes(bytes)
        val sender =  connectionManger.getConnection(ctx.channel())
        if(sender.isDefined) out.add(Some(resolver.resolve(id, size, bytes, sender.get)))
      }
    }
  }
}
