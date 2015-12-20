package pl.jaca.server.networking

import akka.actor.ActorSystem
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import Server.EventOccurred
import ServerEvent.{ConnectionInactive, ConnectionActive}
import pl.jaca.testutils.AkkaTools
import pl.jaca.testutils.server.proxy.DummyNettyChannel

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Jaca777
 *         Created 2015-11-27 at 21
 */
class ConnectionManagerSpec extends TestKit(ActorSystem("ConnectionManagerSpec")) with ImplicitSender with WordSpecLike with Matchers with AkkaTools {

  "ConnectionManager" must {


    val testChannel1 = new DummyNettyChannel(1, 2)
    val testChannel2 = new DummyNettyChannel(3, 4)

    "collect available connections" in {
      val proxy = new TestProbe(system, "proxy")
      val server = new TestProbe(system, "server")
      val connectionManager = new ConnectionManager(_ => proxy.ref, server.ref)

      connectionManager.createConnection(testChannel1)
      connectionManager.createConnection(testChannel2)

      connectionManager.getAllConnections.map(_.port) should be(Set(1, 3))
    }

    "remove unavailable connections" in {
      val proxy = new TestProbe(system, "proxy")
      val server = new TestProbe(system, "server")
      val connectionManager = new ConnectionManager(_ => proxy.ref, server.ref)

      connectionManager.createConnection(testChannel1)
      connectionManager.createConnection(testChannel2)
      connectionManager.removeConnection(testChannel1)

      connectionManager.getAllConnections.map(_.port) should be(Set(3))
    }

    "report event of connection creation" in {
      val proxy = new TestProbe(system, "proxy")
      val server = new TestProbe(system, "server")
      val connectionManager = new ConnectionManager(_ => proxy.ref, server.ref)
      connectionManager.createConnection(testChannel1)
      val connection = connectionManager.getAllConnections.head
      within(100 millis) {
        server.expectMsg(EventOccurred(ConnectionActive(connection)))
        server.ignoreAll
      }
    }

    "report event of connection removal" in {
      val proxy = new TestProbe(system, "proxy")
      val server = new TestProbe(system, "server")
      val connectionManager = new ConnectionManager(_ => proxy.ref, server.ref)
      connectionManager.createConnection(testChannel1)
      val connection = connectionManager.getAllConnections.head
      connectionManager.removeConnection(testChannel1)

      within(100 millis) {
        server.expectMsg(EventOccurred(ConnectionActive(connection)))
        server.expectMsg(EventOccurred(ConnectionInactive(connection)))
        server.ignoreAll
      }
    }
  }
}
