package pl.jaca.server

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorLogging, ActorRef, Props}
import pl.jaca.cluster.Application.Launch
import pl.jaca.cluster.distribution.Distribution
import pl.jaca.cluster.{Application, Configurable, SystemNode}
import pl.jaca.server.ServerApplicationRoot.Shutdown
import pl.jaca.server.networking.Server
import pl.jaca.server.networking.Server.Subscribe
import pl.jaca.server.providers.{EventHandlerProvider, PacketResolverProvider, ServiceProvider}

/**
  * @author Jaca777
  *         Created 2015-06-15 at 21
  *         ServerApplicationRoot is responsible for constructing and launching server. Creates services and handlers.
  *
  */
class ServerApplicationRoot extends Application with Distribution with Configurable with ActorLogging {

  private implicit val executor = context.dispatcher

  val systemConfig = context.system.settings.config
  lazy val resolverProvider = new PacketResolverProvider(appConfig)
  lazy val serviceProvider = new ServiceProvider(appConfig, createService, log)
  lazy val handlerProvider = new EventHandlerProvider(appConfig, serviceProvider, createHandler)

  /**
    * Awaiting command to launch.
    */
  override def receive: Receive = {
    case Launch =>
      val server = launchServer()
      context become running(server)
  }

  def running(server: ActorRef): Receive = {
    case Shutdown =>
      server ! Server.Stop
      context.parent ! SystemNode.Shutdown
  }

  /**
    * Launches both server and client application.
    */
  def launchServer() = {
    val port = getPort
    val resolver = resolverProvider.getResolver
    val handlersFuture = handlerProvider.getEventActors
    handlersFuture.onFailure {
      case error => log.error(error, "Error occurred on handler creation.")
    }
    val server = context.actorOf(Props(new Server(port, resolver)))
    for {
      handlers <- handlersFuture
      handler <- handlers
    } server ! Subscribe(handler)
    server
  }


  def getPort: Int =
    systemConfig.intAt("server-app.port").getOrElse(ServerApplicationRoot.defaultPort)

  //HOTFIX
  val serviceid = new AtomicInteger()
  def createService(p: Props, name: String) = context.distribute(p, name + s"service-${serviceid.incrementAndGet()}")

  def createHandler(p: Props, name: String) = {
    context.distribute(p, name)
  }

}

object ServerApplicationRoot {
  val defaultPort = 41154

  //IN
  object Shutdown

}