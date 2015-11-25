package pl.jaca.server.proxy.eventhandling

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.jaca.server.proxy.server.Event


/**
 * @author Jaca777
 *         Created 2015-11-20 at 21
 */
abstract class EventActor extends Actor with ActorLogging {

  type EventHandler[T <: Event] = PartialFunction[Event, (Event => Action)]
  type ActionExpr = Event => Action

  def receive: Receive = reacting(PartialFunction.empty)

  def reacting(handler: EventHandler[Event]): Receive = {
    case e: Event =>
      handler(e)(e).perform()
  }

  val unhandled: EventHandler[Event] = {
    case e => Action {
      //Do nothing
    }
  }

  class AsyncEventStream {
    def emit(event: Event) = self ! event

    def react(handler: EventHandler[Event]) = context become reacting(handler orElse unhandled)
  }

  object AsyncEventStream {
    def apply() = {
      new AsyncEventStream
    }
  }

  class Action(action: => Unit) {
    def perform() = action
  }

  object Action {
    def apply(action: => Unit): ActionExpr = _ => new Action(action)
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


