package pl.jaca.server.proxy.server

import akka.actor.{Props, Actor}
import akka.util.Timeout
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import pl.jaca.server.proxy.packets.InPacket
import pl.jaca.server.proxy.server.Server._
import rx.lang.scala.{Observable, Subject}

import scala.concurrent.duration._

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Server(val port: Int, val resolver: PacketResolver) extends Actor {

  val bossGroup = new NioEventLoopGroup
  val workersGroup = new NioEventLoopGroup
  val selfRef = context.self
  val connectionManager = new ConnectionManager(c => context.actorOf(Props(new ConnectionProxy(c))))
  val bootstrap = new ServerBootstrap()
    .group(bossGroup, workersGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(new ServerInitializer(resolver, self, connectionManager))
  val channel = bootstrap.bind(port).sync().channel()

  implicit val timeout = Timeout(2.seconds)
  implicit val dispatcher = context.dispatcher

  val packetSubject = Subject[InPacket]()

  def receive: Receive = {
    case msg: PacketReceived => packetSubject.onNext(msg.packet)
    case Stop => shutdown()
    case GetPacketObservable => sender ! RPacketObservable(packetSubject)
  }

  def shutdown() {
    bossGroup.shutdownGracefully()
    workersGroup.shutdownGracefully()
    packetSubject.onCompleted()
  }
}

object Server {

  //IN
  object Stop

  object GetPacketObservable

  case class PacketReceived(packet: InPacket)

  //OUT
  case class RPacketObservable(subject: Observable[InPacket])

}
