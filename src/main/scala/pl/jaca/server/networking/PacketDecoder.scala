package pl.jaca.server.networking

import java.net.SocketAddress
import java.util

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import pl.jaca.server.Connection
import PacketReader._

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 */
class PacketDecoder(resolver: PacketResolver) extends ByteToMessageDecoder {

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]) =
    if (isReadable(in)) readPacket(ctx, in, out)


  def readPacket(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]) {
    val size = readSize(in)
    if (isComplete(in, size)) decodePacket(ctx, in, out, size)
  }

  def decodePacket(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef], size: Short) {
    val id = readId(in)
    val bytes: Array[Byte] = readData(in, size, id)
    val sender: Option[Connection] = resolveSender(ctx)
    if (sender.isDefined) out.add(resolver.resolve(id, size, bytes, sender.get))
    else throw new UnknownSenderException(s"Sender with address ${getAddress(ctx)} not found.")
  }

  def resolveSender(ctx: ChannelHandlerContext): Option[Connection] = {
    ???
  }


  def getAddress(ctx: ChannelHandlerContext): SocketAddress = {
    ctx.channel().remoteAddress()
  }
}
