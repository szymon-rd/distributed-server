package pl.jaca.cluster.core

import akka.cluster.Cluster
import pl.jaca.cluster.Configurable

import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-08-16 at 20
 */
class Connector extends ClusterNode with Configurable {
  val systemConfig = context.system.settings.config

  val cluster = Cluster(context.system)
  val localClusterAddress = cluster.selfAddress
  val mainClusterAddress = localClusterAddress.copy(
    protocol = systemConfig.stringAt("server-cluster.address.protocol").getOrElse(localClusterAddress.protocol),
    system = systemConfig.stringAt("server-cluster.address.system").getOrElse(localClusterAddress.system),
    host = systemConfig.stringAt("server-cluster.address.host").orElse(localClusterAddress.host),
    port = systemConfig.intAt("server-cluster.address.port").orElse(localClusterAddress.port)
  )
  cluster.join(mainClusterAddress)

}