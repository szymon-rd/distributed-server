package pl.jaca.cluster.distribution

import akka.actor._
import akka.pattern._
import akka.remote.RemoteScope
import akka.util.Timeout
import scala.concurrent.duration._
import pl.jaca.cluster.distribution.Distribution._
import pl.jaca.cluster.distribution.Receptionist.{AvailableWorker, GetAvailableWorker}

import scala.concurrent.Future
import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * @author Jaca777
 *         Created 2015-09-05 at 16
 */
trait Distribution {

  implicit class DistributedContext(context: ActorContext) {

    implicit val executor = context.dispatcher
    implicit val timeout = Timeout(10 seconds)

    /**
     * Creates new actor, that can be possibly running on another cluster member. Children-parent hierarchy is not modified - created actor is a child of the distributing actor.
     * @param p Props of actor object.
     * @tparam T Type of actor.
     * @return Future binded to distributed actor reference.
     */

    private def distributeProps[T <: Distributable with Actor : ClassTag](p: Props): Future[Props] = {
      val availableWorker = (receptionist ? GetAvailableWorker).mapTo[AvailableWorker] map (_.worker)
      availableWorker.foreach(_.load.increase(AbsoluteLoad(1.0f))) //TODO get load
      val props = availableWorker map (member => p.withDeploy(Deploy(scope = RemoteScope(member.clusterMember.address))))
      props
    }

    //Explained: http://docs.scala-lang.org/sips/completed/scala-2-8-arrays.html
    def distribute[T <: Distributable with Actor : ClassTag](creator: => T, name: String): Future[ActorRef] = {
      distribute(Props(creator), name)
    }

    def distribute[T <: Distributable with Actor : ClassTag](creator: => T): Future[ActorRef] = {
      distribute(Props(creator))
    }

    def distribute[T <: Distributable with Actor : ClassTag](props: Props): Future[ActorRef] = {
      val distributedProps = distributeProps(props)
      distributedProps.map(context.actorOf)
    }

    def distribute[T <: Distributable with Actor : ClassTag](props: Props, name: String): Future[ActorRef] = {
      val distributedProps = distributeProps(props)
      distributedProps.map(props => context.actorOf(props, name))
    }
  }

}

object Distribution {


  private var receptionist: ActorRef = null

  private[cluster] trait DistributionInitializer {
    def setReceptionist(receptionist: ActorRef) {
      Distribution.receptionist = receptionist
    }
  }

}
