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
  val STATE_SIZE = 0
  val STATE_OPCODE = 1
  val STATE_PAYLOAD = 2

  var state = STATE_SIZE
  var size = 0.toShort
  var opcode = 0.toShort

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
    if (in.isReadable(2)) {
      state match {
        case STATE_SIZE =>
          size = in.readShort()
          state = STATE_OPCODE
        case STATE_OPCODE =>
          opcode = in.readShort()
          state = STATE_PAYLOAD
        case STATE_PAYLOAD =>
          if (in.isReadable(size - 4)) {
            val data: Array[Byte] = readData(in, size, opcode)
            val partialPacket = (s: Session) => resolver.resolve(opcode, size, data, s)
            out.add(partialPacket)
            state = STATE_SIZE
          }
      }
    }
    //getInt(0) -> bad
    //    if (isReadable(in))
    //      tryToRead(ctx, in, out)
  }


  //  private def tryToRead(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]) {
  //    val size = readSize(in) + 2
  //    if (isComplete(in, size.toShort)) readPacket(ctx, in, out, size.toShort)
  //  }
  //
  //  private def readPacket(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef], size: Short) {
  //    val id = readId(in, size)
  //    val bytes: Array[Byte] = readData(in, size, id)
  //    val partialPacket = (s: Session) => resolver.resolve(id, size, bytes, s)
  //    out.add(partialPacket)
  //  }
}
