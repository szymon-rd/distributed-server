package pl.jaca.cluster.distribution

import akka.actor._
import akka.pattern._
import akka.remote.RemoteScope
import akka.util.Timeout
import pl.jaca.cluster.Configurable
import pl.jaca.cluster.distribution.Distribution._
import pl.jaca.cluster.distribution.Receptionist.{AvailableWorker, GetAvailableWorker}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

/**
 * @author Jaca777
 *         Created 2015-09-05 at 16
 */
trait Distribution extends Configurable with Actor {
  distr =>

  private val lazyDistr = appConfig.boolAt("server-app.lazy-distribution").getOrElse(false)
  private implicit val executor = context.dispatcher
  private implicit val timeout = Timeout(10 seconds)

  implicit class DistributedContext(context: ActorContext) {

    /**
     * Creates new actor, that can be possibly running on another cluster member.
     * Children-parent hierarchy is not modified - created actor is a child of the distributing actor.
     * @tparam T Type of actor.
     * @return Future binded to distributed actor reference.
     */

    def distribute[T <: Distributable with Actor : ClassTag](creator: => T, name: String): DistrActorRef =
      distr.distribute(Props(creator), name, (p: Props, name: String) => context.actorOf(p, name))

    def distribute[T <: Distributable with Actor : ClassTag](creator: => T): DistrActorRef =
      distr.distribute(Props(creator), (p: Props) => context.actorOf(p))

    def distribute[T <: Distributable with Actor : ClassTag](props: Props): DistrActorRef =
      distr.distribute(props, (p: Props) => context.actorOf(p))

    def distribute[T <: Distributable with Actor : ClassTag](props: Props, name: String): DistrActorRef =
      distr.distribute(props, name, (p: Props, name: String) => context.actorOf(p, name))

  }


  private[cluster] def distributeProps[T <: Distributable with Actor : ClassTag](p: Props): Future[Props] = {
    val availableWorker = (receptionist ? GetAvailableWorker).mapTo[AvailableWorker] map (_.worker)
    availableWorker.foreach(_.load.increase(AbsoluteLoad(1.0f)))
    val props = availableWorker map (member => p.withDeploy(Deploy(scope = RemoteScope(member.clusterMember.address))))
    props
  }


  def distribute[T <: Distributable with Actor : ClassTag](props: Props, actorCreator: Props => ActorRef): DistrActorRef = {
    val distributedProps = distributeProps(props)
    new LazyActorRef(distributedProps.map(props => actorCreator(props)), !lazyDistr)
  }

  def distribute[T <: Distributable with Actor : ClassTag](props: Props, name: String,
                                                           actorCreator: (Props, String) => ActorRef): DistrActorRef = {
    val distributedProps = distributeProps(props)
    new LazyActorRef(distributedProps.map(props => actorCreator(props, name)), !lazyDistr)
  }

  type DistrActorRef = Future[ActorRef]

  implicit class DistrActorRefImplicit(ref: DistrActorRef) {
    def !(msg: Any) = ref.foreach(_ ! msg)

    def ?(q: Any)(implicit timeout: Timeout) = ref.flatMap(_ ? q)

    def tell(msg: Any, sender: ActorRef) = ref.foreach(_.tell(msg, sender))
  }

}

object Distribution {


  private var receptionist: ActorRef = null

  private[cluster] trait DistributionInitializer {
    def initReceptionist(receptionist: ActorRef) {
      Distribution.receptionist = receptionist
    }
  }

}
