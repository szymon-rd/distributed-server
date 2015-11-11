package pl.jaca.server.proxy.server

import akka.actor.{Actor, Props}
import akka.util.Timeout
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
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
  val connectionManager = new ConnectionManager(c => context.actorOf(Props(new ConnectionProxy(c))), self)
  val bootstrap = new ServerBootstrap()
    .group(bossGroup, workersGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(new ServerInitializer(resolver, self, connectionManager))
  val channel = bootstrap.bind(port).sync().channel()

  implicit val timeout = Timeout(2.seconds)
  implicit val dispatcher = context.dispatcher

  val eventSubject = Subject[Event]()

  def receive: Receive = {
    case EventOccurred(event) => eventSubject.onNext(event)
    case Stop => shutdown()
    case GetEventObservable => sender ! REventObservable(eventSubject)
  }

  def shutdown() {
    bossGroup.shutdownGracefully()
    workersGroup.shutdownGracefully()
    eventSubject.onCompleted()
  }
}

object Server {

  //IN
  object Stop

  object GetEventObservable

  case class EventOccurred(event: Event)

  //OUT
  type EventStream = Observable[Event]
  case class REventObservable(stream: EventStream)

}
