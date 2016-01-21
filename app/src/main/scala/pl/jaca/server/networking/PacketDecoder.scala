package pl.jaca.server.networking

import java.util

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import pl.jaca.server.Session
import pl.jaca.server.networking.PacketReader._

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 */
class PacketDecoder(resolver: => PacketResolver) extends ByteToMessageDecoder {

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]) =
    if (isReadable(in)) tryToRead(ctx, in, out)


  private def tryToRead(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]) {
    val size = readSize(in)
    if (isComplete(in, size)) readPacket(ctx, in, out, size)
  }

  private def readPacket(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef], size: Short) {
    val id = readId(in, size)
    val bytes: Array[Byte] = readData(in, size, id)
    val partialPacket = (s: Session) => resolver.resolve(id, size, bytes, s)
    out.add(partialPacket)
  }
}
