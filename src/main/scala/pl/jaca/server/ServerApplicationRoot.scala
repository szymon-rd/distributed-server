package pl.jaca.server

import java.lang.reflect.Constructor

import akka.actor.{ActorRef, Props}
import pl.jaca.server.ServerApplicationRoot.{RegisterEventTree, Shutdown}
import pl.jaca.server.cluster.Application.Launch
import pl.jaca.server.cluster.distribution.Distribution
import pl.jaca.server.cluster.{Application, Configurable, SystemNode}
import pl.jaca.server.proxy.server.Server.Subscribe
import pl.jaca.server.proxy.server.{PacketResolver, Server}

/**
 * @author Jaca777
 *         Created 2015-06-15 at 21
 */
class ServerApplicationRoot extends Application with Distribution with Configurable {
  implicit val serverAppPath = "server-app"


  override def receive: Receive = {
    case Launch =>
      val server = launchServer()
      launchApp()
      context become running(server)
  }

  def running(server: ActorRef): Receive = {
    case Shutdown =>
      server ! Server.Stop
      context.parent ! SystemNode.Shutdown
    case RegisterEventTree(root) => server ! Subscribe(root)
  }


  def launchServer() = {
    val port = getPort
    val resolver = getResolvers
    val server = context.actorOf(Props(new Server(port, resolver)))
    server
  }


  def getPort: Int =
    config.intAt("port").getOrElse(ServerApplicationRoot.defaultPort)


  def getResolvers: PacketResolver = {
    val resolverPathsOption = config.stringsAt("packet-resolvers")
    requireConfig(resolverPathsOption.isDefined, "PacketResolvers not specified in configuration file.")
    val resolverPaths = resolverPathsOption.get
    requireConfig(resolverPaths.forall(resolverExists), "Given PacketResolvers not found in classpath.")
    val resolvers = resolverPaths.map(createResolver)
    fold(resolvers)
  }


  def fold(resolvers: Array[PacketResolver]): PacketResolver =
    resolvers.reduce(_ and _)


  def createResolver(path: String): PacketResolver = {
    val c = Class.forName(path)
    c.newInstance().asInstanceOf[PacketResolver]
  }

  def resolverExists(path: String) = {
    def parameterless(constructor: Constructor[_]) = constructor.getParameterCount == 0
    try {
      val c = Class.forName(path)
      ServerApplicationRoot.resolverClass.isAssignableFrom(c)
      c.getConstructors.exists(parameterless)
    } catch {
      case _: ClassNotFoundException => false
    }
  }

  def launchApp() = {
    val serverAppPath = config.stringAt("app-path").get
    val appName = config.stringAt("app-name").get
    val appClass: Class[Application] = Class.forName(serverAppPath).asInstanceOf[Class[Application]]
    requireConfig(appClass.getConstructors.exists(_.getParameterCount == 0), "app-path")
    context.distribute(appClass.newInstance().asInstanceOf[ServerApp], appName)
  }
}

object ServerApplicationRoot {
  private val resolverClass = classOf[PacketResolver]
  val defaultPort = 81154

  //IN
  object Shutdown

  case class RegisterEventTree(root: ActorRef)

}