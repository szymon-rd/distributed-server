package pl.jaca.server.cluster.distribution

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Member

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
 * @author Jaca777
 *         Created 2015-08-16 at 22
 */
/**
 * Register containing registered cluster members.
 */
trait Register extends ActorLogging {
  registration: Actor =>

  private var memberPromise = Promise[RegisteredMember]

  implicit class MemberRegistration(member: Member) {
    def register(): Try[RegisteredMember] = registration.register(member)

    def unregister(): Try[RegisteredMember] = registration.unregister(member)
  }

  private var loadFactory: (Member => Load) = (_ => new AbsoluteLoad(0))
  var registeredMembers: Set[RegisteredMember] = Set()

  def useLoadFactory(factory: Member => Load) = {
    this.loadFactory = factory
  }

  def register(member: Member): Try[RegisteredMember] = {
    registeredMembers.find(_.clusterMember == member) match {
      case Some(_) => Failure(new RegisterException(s"Unable to register a member. The given member is already registered: ${member.address}"))
      case None =>
        val registeredMember = new RegisteredMember(member, loadFactory(member))
        registeredMembers += registeredMember
        if (!memberPromise.isCompleted) memberPromise.success(registeredMember)
        log.debug(s"Member successfully registered. Address: ${member.address}")
        Success(registeredMember)
    }
  }

  def unregister(member: Member): Try[RegisteredMember] = {
    registeredMembers.find(_.clusterMember == member) match {
      case Some(toUnregister) =>
        registeredMembers = registeredMembers.filterNot(_.clusterMember == member)
        if (registeredMembers.isEmpty) memberPromise = Promise[RegisteredMember]
        log.debug(s"Member successfully unregistered. Address: ${member.address}")
        Success(toUnregister)
      case None => Failure(new RegisterException(s"Member unregistration failed. Requested member not found: ${member.address}"))
    }

  }

  def isRegistered(member: Member): Boolean = registeredMembers.find(_.clusterMember == member) match {
    case Some(_) => true
    case None => false
  }

  def unregister(registeredMember: RegisteredMember): Unit = unregister(registeredMember.clusterMember)

  def anyMember: Future[RegisteredMember] = memberPromise.future
}

class RegisterException(val message: String) extends Exception(message)

