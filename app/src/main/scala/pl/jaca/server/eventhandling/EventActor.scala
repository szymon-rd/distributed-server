package pl.jaca.server.eventhandling

import akka.actor.{Actor, ActorLogging}
import pl.jaca.server.networking.Event
import pl.jaca.server.networking.ServerEvent.SessionEvent
import pl.jaca.server.packets.InPacket


/**
 * @author Jaca777
 *         Created 2015-11-20 at 21
 */
abstract class EventActor extends Actor with ActorLogging {
  implicit val executionContext = context.dispatcher

  type EventHandler = PartialFunction[Event, Unit]

  def receive: Receive = reacting(PartialFunction.empty)

  private def reacting(handler: EventHandler): Receive = {
    case e: Event if handler.isDefinedAt(e) =>
      e.getAndHandle()
      handler(e)
    case AddHandler(h: EventHandler) =>
      context.become(reacting(h orElse handler orElse unhandled))
  }

  private val unhandled: EventHandler = {
    case _ => //do nothing
  }

  private case class AddHandler(handler: EventHandler)

  private[eventhandling] class AsyncEventStream {
    /**
     * Adds new event handler with the highest priority. It's called as first.
     */
    def react(handler: EventHandler) = self ! AddHandler(handler)

    /**
     * Returns event stream containing only InPacket events.
     */
    def packets: PacketAsyncStream.type = PacketAsyncStream

    /**
     * Returns event stream containing only SessionEvents.
     */
    def sessionEvents: SessionEventAsyncStream.type = SessionEventAsyncStream
  }

  object AsyncEventStream {
    def apply() = {
      new AsyncEventStream()
    }
  }

  type PacketHandler = PartialFunction[InPacket, Unit]

  private[eventhandling] object PacketAsyncStream {
    def react(handler: PacketHandler) = self ! AddHandler {
      case i: InPacket => (handler orElse unhandled)(i)
    }
  }

  type SessionEventHandler = PartialFunction[SessionEvent, Unit]
  
  private[eventhandling] object SessionEventAsyncStream {
    def react(handler: SessionEventHandler) = self ! AddHandler {
      case c: SessionEvent => (handler orElse unhandled)(c)
    }
  }


}

object EventActor {

  case class EventHandled(event: Event)

}


