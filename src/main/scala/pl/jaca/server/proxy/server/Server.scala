package pl.jaca.server.proxy.server

import akka.actor.Actor
import akka.util.Timeout
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import pl.jaca.server.proxy.packets.InPacket
import pl.jaca.server.proxy.server.Server._

import scala.concurrent.duration._

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Server(val port: Int, val resolver: PacketResolver) extends Actor {

  val bossGroup = new NioEventLoopGroup
  val workersGroup = new NioEventLoopGroup
  val selfRef = context.self
  val bootstrap = new ServerBootstrap()
    .group(bossGroup, workersGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(new ServerInitializer(resolver, self))
  val channel = bootstrap.bind(port).sync().channel()

  implicit val timeout = Timeout(2.seconds)
  implicit val dispatcher = context.dispatcher

  def receive: Receive = {
    case msg: PacketReceived => context.parent ! msg


    case Stop() =>
      bossGroup.shutdownGracefully()
      workersGroup.shutdownGracefully()

  }
}

object Server {

  case class Stop()

  case class PacketReceived(packet: InPacket)

}
