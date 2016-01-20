package pl.jaca.cluster.distribution

import akka.actor.ActorRef

import scala.concurrent.duration.Duration
import scala.concurrent.{CanAwait, ExecutionContext, Future}
import scala.util.Try

/**
 * @author Jaca777
 *         Created 2016-01-20 at 12
 */
class LazyActorRef(actorRef: => Future[ActorRef], eval: Boolean) extends Future[ActorRef] {

  if (eval) evalRef

  def evalRef() = {
    actorRef
  }

  override def isCompleted: Boolean = true

  override def onComplete[U](f: (Try[ActorRef]) => U)(implicit executor: ExecutionContext): Unit =
    evalRef().onComplete(f)

  override def foreach[U](f: (ActorRef) => U)(implicit executor: ExecutionContext): Unit = evalRef().foreach(f)

  override def value: Option[Try[ActorRef]] = evalRef().value

  override def result(atMost: Duration)(implicit permit: CanAwait): ActorRef = evalRef().result(atMost)(permit)

  override def ready(atMost: Duration)(implicit permit: CanAwait): LazyActorRef.this.type = this

}
