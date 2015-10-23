package pl.jaca.server.cluster.core

import akka.actor.{Actor, ActorSystem, Props}
import pl.jaca.server.cluster.{Application, Configurable}

/**
 * @author Jaca777
 *         Created 2015-10-02 at 22
 */
class Launcher extends Actor with Configurable {
  implicit val configPath = "server-cluster"

  val appPath = config.stringAt("application").get
  val appClass: Class[Application] = Class.forName(appPath).asInstanceOf[Class[Application]]
  context.actorOf(Props(new Initializer(() => appClass.newInstance())))

  override def receive: Receive = {
    case _ => throw new RuntimeException("ClusterLauncher is not capable of receiving any messages.")
  }
}

object Launcher {
  def main(args: Array[String]) {
    val system = ActorSystem.create("Main")
    system.actorOf(Props[Launcher])
  }
}