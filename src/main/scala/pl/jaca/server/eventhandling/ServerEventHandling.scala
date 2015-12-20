package pl.jaca.server.eventhandling

import pl.jaca.server.networking.ServerEvent.InPacketEvent
import pl.jaca.server.networking.ServerStateEvent

/**
 * @author Jaca777
 *         Created 2015-12-11 at 18
 */
trait ServerEventHandling extends EventActor {

  class PacketAsyncStream extends AsyncEventStream {
    override def react(handler: EventHandler) = super.react({
      case i: InPacketEvent => handler.orElse(unhandled)(i)
    })

  }

  class ServerEventAsyncStream extends AsyncEventStream {
    override def react(handler: EventHandler) = super.react({
      case i: ServerStateEvent => handler.orElse(unhandled)(i)
    })

  }

  implicit class ServerAsyncEventStream(asyncEventStream: AsyncEventStream) {
    def packets: AsyncEventStream = new PacketAsyncStream

    def serverEvents: AsyncEventStream = new ServerEventAsyncStream
  }

}
