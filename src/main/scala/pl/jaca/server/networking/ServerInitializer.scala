package pl.jaca.server.networking

import akka.actor.ActorRef
import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.channel.socket.SocketChannel

/**
 * @author Jaca777
 *         Created 2015-10-11 at 15
 */
class ServerInitializer(resolver: PacketResolver, serverRef: ActorRef, proxyFactory: Channel => ActorRef) extends ChannelInitializer[SocketChannel] {

  override def initChannel(channel: SocketChannel) {
    constructPipeline(channel)
  }

  private def constructPipeline(channel: SocketChannel) {
    val pipeline = channel.pipeline()
    pipeline.addLast(new PacketDecoder(resolver))
    pipeline.addLast(new ChannelHandler(proxyFactory, serverRef))
    pipeline.addLast(new PacketEncoder)
  }
}
