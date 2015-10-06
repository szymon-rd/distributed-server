package pl.jaca.server.proxy

import akka.actor.Actor
import akka.util.Timeout
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import pl.jaca.server.proxy.Server._
import pl.jaca.server.proxy.packets.{InPacket, OutPacket}

import scala.concurrent.duration._

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Server(val port: Int, val resolver: PacketResolver) extends Actor {
  val connectionManager = new ConnectionManger(self)
  val bossGroup = new NioEventLoopGroup()
  val workersGroup = new NioEventLoopGroup()
  val selfRef = context.self
  val bootstrap = new ServerBootstrap()
      .group(bossGroup, workersGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(channel: SocketChannel) {
          val pipeline = channel.pipeline()
          pipeline.addLast(new PacketDecoder(resolver, connectionManager))
          pipeline.addLast(new PacketHandler(connectionManager, self))
          pipeline.addLast(new PacketEncoder)
        }
      })
  val channel = bootstrap.bind(port).sync().channel()

  implicit val timeout = Timeout(2.seconds)
  implicit val dispatcher = context.dispatcher

  def receive: Receive = {
    case msg: Message => context.parent ! msg

    case SendTo(connection, msg) =>
      connectionManager.getChannel(connection).writeAndFlush(msg)

    case Stop() =>
      bossGroup.shutdownGracefully()
      workersGroup.shutdownGracefully()

  }
}
object Server {
  case class Stop()
  case class Message(packet: InPacket)
  case class SendTo(connection: Connection, msg: OutPacket)
}
