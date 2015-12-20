package pl.jaca.server.networking

import java.io.IOException

import akka.actor.ActorRef
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import pl.jaca.server.packets.InPacket

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class ChannelHandler(connectionManager: ConnectionManager, server: ActorRef) extends ChannelInboundHandlerAdapter {

  override def channelActive(ctx: ChannelHandlerContext) {
    connectionManager.createConnection(ctx.channel())
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    connectionManager.removeConnection(ctx.channel())
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: Object) {
    val packet = msg.asInstanceOf[InPacket]
    server ! Server.EventOccurred(packet)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) = cause match {
    case e: IOException => ctx.close()
    case any =>
      any.printStackTrace()
      ctx.close()
  }
}
