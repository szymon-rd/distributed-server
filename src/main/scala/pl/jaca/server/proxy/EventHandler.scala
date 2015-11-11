package pl.jaca.server.proxy

import akka.actor.{ActorRef, Actor}
import akka.util.Timeout
import akka.pattern._
import scala.concurrent.Future
import scala.concurrent.duration._
import pl.jaca.server.proxy.packets.InPacket
import pl.jaca.server.proxy.server.Server.{GetEventObservable, REventObservable}
import pl.jaca.server.proxy.server._
import rx.lang.scala.Observable

/**
 * @author Jaca777
 *         Created 2015-11-11 at 01
 */
trait EventHandler extends Actor { //TODO Do something with Futures.
  type EventStream = Observable[Event]
  type PacketStream[T <: InPacket] = Observable[T]
  
  implicit class EventStreamImplicit(eventStream: EventStream) {
    def packets = eventStream.filter(_.isInstanceOf[PacketReceived]).map(_.asInstanceOf[PacketReceived]).map(_.inPacket)
    def connectionEvents = eventStream.filter(_.isInstanceOf[ConnectionEvent])
    def activeConnectionStream = eventStream.filter(_.isInstanceOf[ConnectionActive]).map(_.asInstanceOf[ConnectionActive]).map(_.connection)
    def inactiveConnectionStream = eventStream.filter(_.isInstanceOf[ConnectionInactive]).map(_.asInstanceOf[ConnectionInactive]).map(_.connection)
  }

  implicit class PacketStreamImplicit[T <: InPacket](observable: Observable[T]) {
    def packets[R <: T]: PacketStream[R] = observable.filter(_.isInstanceOf[R]).map(_.asInstanceOf[R])
    def packetsFrom(connectionsFilter: (Connection => Boolean)) = observable.filter(packet => connectionsFilter(packet.sender))
  }

  implicit class PacketSenderImplicit(connection: Connection){
    def packets(implicit packetStream: PacketStream) = packetStream.map((packet: InPacket) => packet.sender == connection)
    def packets(implicit eventStream: EventStream) = eventStream.packets.map((packet: InPacket) => packet.sender == connection)
  }

  implicit val askTimeout = Timeout(2 seconds)
  object EventStream {
    def apply(serverRef: ActorRef): Future[EventStream] = (serverRef ? GetEventObservable).mapTo[REventObservable].map(_.stream)
  }
  object PacketStream {
    def apply(serverRef: ActorRef): Future[PacketStream[InPacket]] = (serverRef ? GetEventObservable).mapTo[REventObservable].map(_.stream).map(_.packets)
  }
}

