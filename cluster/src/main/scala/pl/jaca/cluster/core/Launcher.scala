package pl.jaca.cluster.core

import akka.actor.{Actor, ActorSystem, Props}
import pl.jaca.cluster.Application

/**
 * @author Jaca777
 *         Created 2015-10-02 at 22
 */
class Launcher extends Actor {
  val systemConfig = context.system.settings.config

  val appPath = systemConfig.getString("server-cluster.application")
  val appClass: Class[Application] = Class.forName(appPath).asInstanceOf[Class[Application]]
  context.actorOf(Props(new Initializer(() => appClass.newInstance())), "clusterInitializer")

  override def receive: Receive = {
    case _ => throw new RuntimeException("ClusterLauncher is not capable of receiving any messages.")
  }
}

object Launcher {
  def main(args: Array[String]) {
    val system = ActorSystem.create("Main")
    system.actorOf(Props[Launcher], "launcher")
  }
}