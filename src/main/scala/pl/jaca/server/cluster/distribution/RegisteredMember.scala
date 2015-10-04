package pl.jaca.server.cluster.distribution

import akka.cluster.Member

/**
 * @author Jaca777
 *         Created 2015-08-16 at 22
 */
case class RegisteredMember(clusterMember: Member, load: Load) {

}

