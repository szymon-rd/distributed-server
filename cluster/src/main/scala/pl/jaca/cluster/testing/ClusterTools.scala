package pl.jaca.cluster.testing

import akka.actor.Address
import akka.cluster.{Member, UniqueAddress}
import pl.jaca.util.testing.AkkaTools

/**
 * @author Jaca777
 *         Created 2015-09-10 at 17
 */
trait ClusterTools extends AkkaTools {
  def createClusterMember(address: UniqueAddress, roles: Set[String]): Member = {
    val factory = Member.getClass.getMethod("apply", classOf[UniqueAddress], classOf[Set[String]])
    factory.setAccessible(true)
    factory.invoke(Member, address, roles).asInstanceOf[Member]
  }

  def createClusterMember(address: Address): Member = createClusterMember(new UniqueAddress(address, 0), Set())
}
