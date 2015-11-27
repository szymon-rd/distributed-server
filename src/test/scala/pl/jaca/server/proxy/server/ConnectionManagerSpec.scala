package pl.jaca.server.proxy.server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.testutils.MockNettyChannel

/**
 * @author Jaca777
 *         Created 2015-11-27 at 21
 */
class ConnectionManagerSpec extends TestKit(ActorSystem("ConnectionManagerSpec")) with ImplicitSender with WordSpecLike with Matchers {


  "ConnectionManager" must {
    val proxyProbe = TestProbe()
    val serverProbe = TestProbe()

    val testChannel1 = new MockNettyChannel(1, 2)
    val testChannel2 = new MockNettyChannel(3, 4)

    "collect available connections" in {
      val connectionManager = new ConnectionManager(_ => proxyProbe.ref, serverProbe.ref)

      connectionManager.createConnection(testChannel1)
      connectionManager.createConnection(testChannel2)

      connectionManager.getAllConnections.map(_.port) should be(Set(1, 3))
    }

    "remove unavailable connections" in {
      val connectionManager = new ConnectionManager(_ => proxyProbe.ref, serverProbe.ref)

      connectionManager.createConnection(testChannel1)
      connectionManager.createConnection(testChannel2)
      connectionManager.removeConnection(testChannel1)
      connectionManager.getAllConnections.map(_.port) should be(Set(3))
    }
  }
}
