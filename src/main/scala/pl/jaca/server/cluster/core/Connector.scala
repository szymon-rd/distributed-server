package pl.jaca.server.cluster.core

import akka.cluster.Cluster
import pl.jaca.server.cluster.Configurable

import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-08-16 at 20
 */
class Connector extends ClusterNode with Configurable {
  implicit val configPath = "server-cluster"

  val cluster = Cluster(context.system)
  val localClusterAddress = cluster.selfAddress
  val mainClusterAddress = localClusterAddress.copy(
    protocol = config.stringAt("address.protocol").getOrElse(localClusterAddress.protocol),
    system = config.stringAt("address.system").getOrElse(localClusterAddress.system),
    host = config.stringAt("address.host").orElse(localClusterAddress.host),
    port = config.intAt("address.port").orElse(localClusterAddress.port)
  )
  cluster.join(mainClusterAddress)

}