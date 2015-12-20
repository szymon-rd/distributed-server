package pl.jaca.cluster.core

import akka.actor.SupervisorStrategy.{Escalate, Stop}
import akka.actor._
import akka.cluster.Cluster
import pl.jaca.cluster.Application
import pl.jaca.cluster.SystemNode.Launch

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-08-16 at 15
 */
class Initializer(application: () => _ <: Application) extends ClusterNode {

  override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 1, withinTimeRange = Duration.Inf) {
    case FatalClusterError(message) => Stop
    case _ => Escalate
  }

  val cluster = Cluster(context.system)
  cluster.join(cluster.selfAddress)

  appNode ! Launch(application)
}

