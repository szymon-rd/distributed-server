package pl.jaca.server.cluster.core

import akka.actor.{Props, Actor, ActorSystem}
import pl.jaca.server.cluster.{Configurable, Application}
import pl.jaca.server.oldcluster.FatalClusterError

/**
 * @author Jaca777
 *         Created 2015-10-02 at 22
 */
class Launcher extends Actor with Configurable {
  implicit val configPath = "server-cluster"

  val appPath = config.get("application")
  val appClass: Class[Application] = Class.forName(appPath).asInstanceOf[Class[Application]]
  context.actorOf(Props(new Initializer(() => appClass.newInstance())))

  override def receive: Receive = {
    case _ => throw new FatalClusterError("Launcher actor is not capable of receiving messages")
  }

}

object Launcher {
  def main(args: Array[String]) {
    val system = ActorSystem.create("Main")
    system.actorOf(Props[Launcher])
  }
}