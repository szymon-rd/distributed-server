package pl.jaca.server.proxy.server

import akka.actor.ActorRef
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel

/**
 * @author Jaca777
 *         Created 2015-10-11 at 15
 */
class ServerInitializer(resolver: PacketResolver, serverRef: ActorRef) extends ChannelInitializer[SocketChannel] {

  override def initChannel(channel: SocketChannel) {
    constructPipeline(channel)
  }

  private def constructPipeline(channel: SocketChannel): Unit = {
    val connectionManager = new ConnectionManger
    val pipeline = channel.pipeline()
    pipeline.addLast(new PacketDecoder(resolver, connectionManager))
    pipeline.addLast(new PacketHandler(connectionManager, serverRef))
    pipeline.addLast(new PacketEncoder)
  }
}
