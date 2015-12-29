package pl.jaca.server.networking

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import pl.jaca.server.networking.Server.{Subscribe, _}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Server(val port: Int, val resolver: PacketResolver) extends Actor with ActorLogging {

  val bossGroup = new NioEventLoopGroup
  val workersGroup = new NioEventLoopGroup
  val proxyFactor = (c: Channel) => context.actorOf(Props(new ConnectionProxy(c)))
  val bootstrap = new ServerBootstrap()
    .group(bossGroup, workersGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(new ServerInitializer(resolver, self, proxyFactor))
  val channel = bootstrap.bind(port).sync().channel()

  implicit val timeout = Timeout(2.seconds)
  implicit val dispatcher = context.dispatcher

  def receive = running(Set.empty)

  log.info(s"Application server is now running on port $port")

  def running(subscribers: Set[ActorRef]): Receive = {
    case EventOccurred(event) =>
      subscribers.foreach(_ ! event)
      setupGuard(event)
    case Stop => shutdown()
    case Subscribe(actor) => context become running(subscribers + actor)
  }


  def setupGuard(event: Event) = context.system.scheduler.scheduleOnce(2 seconds, new Runnable {
    override def run() = if (!event.getAndHandle()) reportUnhandled(event)
  })

  def reportUnhandled(event: Event) = log.warning(s"Unhandled event: $event")

  def shutdown() {
    bossGroup.shutdownGracefully()
    workersGroup.shutdownGracefully()
  }
}

object Server {

  /**
   * Adds new subscription to server event stream.
   */
  case class Subscribe(eventActor: ActorRef)

  //IN
  /**
   * Stops the server.
   */
  object Stop

  case class EventOccurred(event: ServerEvent)

}
