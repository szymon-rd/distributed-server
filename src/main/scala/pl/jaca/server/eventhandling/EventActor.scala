package pl.jaca.server.eventhandling

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.jaca.server.networking.Event

import scala.concurrent.Future


/**
 * @author Jaca777
 *         Created 2015-11-20 at 21
 */
abstract class EventActor extends Actor with ActorLogging {
  implicit val executionContext = context.dispatcher

  type EventHandler = PartialFunction[Event, (Event => Action)]
  type ActionExpr = Event => Action

  def receive: Receive = reacting(PartialFunction.empty)


  def reacting(handler: EventHandler): Receive = {
    case e: Event =>
      e.getAndHandle()
      handler(e)(e).perform()
    case a: Action =>
      a.perform()
  }

  val unhandled: EventHandler = {
    case e => Ignore
  }

  private[eventhandling] class AsyncEventStream {

    def react(handler: EventHandler) = context become reacting(handler orElse unhandled)

  }

  object AsyncEventStream {
    def apply() = {
      new AsyncEventStream()
    }
  }

  class Action(action: => Unit) {
    def perform() = action
  }

  object Action {
    def apply(action: => Unit): ActionExpr = _ => new Action(action)
  }


  object FutureAction {
    def apply(action: => Future[Unit]): ActionExpr = Action {
      action.onSuccess({case _ => self ! _})
    }
  }

  object Route {
    def apply(actorRef: ActorRef): ActionExpr = event => new Action {
      actorRef ! event
    }
  }

  object Ignore extends ActionExpr {
    override def apply(v1: Event): Action = new Action{}
  }

  implicit class ActionExprImplicit(actionExpr: ActionExpr){
    def and(actionExpr: ActionExpr): ActionExpr = {
      event => new Action({
        this.actionExpr(event).perform()
        actionExpr(event).perform()
      })
    }
  }

}


