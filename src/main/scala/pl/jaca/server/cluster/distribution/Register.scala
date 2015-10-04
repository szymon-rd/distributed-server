package pl.jaca.server.cluster.distribution

import akka.cluster.Member

import scala.concurrent.{Future, Promise}

/**
 * @author Jaca777
 *         Created 2015-08-16 at 22
 */
/**
 * Register containing registered cluster members.
 */
trait Register {
  registration =>

  private var memberPromise = Promise[RegisteredMember]

  implicit class MemberRegistration(member: Member) {
    def register(): RegisteredMember = registration.register(member)

    def unregister(): Option[RegisteredMember] = registration.unregister(member)
  }

  private var loadFactory: (Member => Load) = (_ => new AbsoluteLoad(0))
  var registeredMembers: Set[RegisteredMember] = Set()

  def useLoadFactory(factory: Member => Load): Unit = {
    this.loadFactory = factory
  }

  def register(member: Member): RegisteredMember = {
    val registeredMember = new RegisteredMember(member, loadFactory(member))
    registeredMembers += registeredMember
    if (!memberPromise.isCompleted) memberPromise.success(registeredMember)
    registeredMember
  }

  def unregister(member: Member): Option[RegisteredMember] = {
    registeredMembers.find(_.clusterMember == member) match {
      case Some(toUnregister) =>
        registeredMembers = registeredMembers.filterNot(_.clusterMember == member)
        if (memberPromise.future.value.get.get == toUnregister) memberPromise = Promise[RegisteredMember]
        Some(toUnregister)
      case None => None
    }

  }

  def unregister(registeredMember: RegisteredMember): Unit = unregister(registeredMember.clusterMember)

  def anyMember: Future[RegisteredMember] = memberPromise.future
}

