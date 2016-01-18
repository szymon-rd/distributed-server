package pl.jaca.server.networking

import akka.actor._
import akka.util.Timeout
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import pl.jaca.server.networking.Server.{Subscribe, _}
import pl.jaca.server.packets.InPacket

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-06-12 at 16
 */
class Server(val port: Int, val resolver: PacketResolver) extends Actor with ActorLogging {

  val bossGroup = new NioEventLoopGroup
  val workersGroup = new NioEventLoopGroup
  val proxyFactor = (c: Channel) => context.actorOf(Props(new SessionProxy(c)))
  val bootstrap = new ServerBootstrap()
    .group(bossGroup, workersGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(new ServerInitializer(resolver, self, proxyFactor, context.system))
  val channel = bootstrap.bind(port).sync().channel()

  implicit val timeout = Timeout(2.seconds)
  implicit val dispatcher = context.dispatcher


  def receive = running(Set.empty)

  log.info(s"Application server is now running on port $port")

  def running(subscribers: Set[ActorRef]): Receive = {
    case EventOccurred(event) =>
      if (isPublic(event)) {
        subscribers.foreach(_ ! event)
        setupGuard(event)
      }
    case Stop => shutdown()
    case Subscribe(actor) => context become running(subscribers + actor)
  }

  def isPublic(event: ServerEvent): Boolean = event match {
    case packet: UnresolvedPacket =>
      logUnresolved(packet)
      false
    case packet: InPacket =>
      val sender = packet.sender
      val queueSize = sender.packetsQueueSize.incrementAndGet()
      if (queueSize > MaxSessionEventQueueSize) false
      else true
    case _ => true
  }

  def logUnresolved(packet: UnresolvedPacket) = packet match {
    case event@UnresolvedPacket(cause, id, length, data, session) =>
      event.getAndHandle()
      log.warning(s"Unable to resolve packet from: ${session.host}. $cause")
  }

  def setupGuard(event: Event) = context.system.scheduler.scheduleOnce(2 seconds, new Runnable {
    override def run() =
      if (!event.getAndHandle()) reportUnhandled(event)

    event match {
      case packet: InPacket =>
        val sender = packet.sender
        sender.packetsQueueSize.decrementAndGet()
      case _ => //Connection events are not counted
    }
  })

  def reportUnhandled(event: Event) = log.warning(s"Unhandled event: $event")

  def shutdown() {
    bossGroup.shutdownGracefully()
    workersGroup.shutdownGracefully()
  }

}

object Server {

  val MaxSessionEventQueueSize = 10

  /**
   * Adds new subscription to server event stream.
   */
  case class Subscribe(eventActor: ActorRef)

  /**
   * Stops the server.
   */
  object Stop

  case class EventOccurred(event: ServerEvent)

}
