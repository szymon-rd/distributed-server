package pl.jaca.server

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.service.Service

/**
 * @author Jaca777
 *         Created 2015-12-17 at 20
 */
class ServiceProviderSpec extends WordSpecLike with Matchers {

  val properConfig1 = ConfigFactory.load("pl/jaca/server/conf1.conf")
  val wrongConfig1 = ConfigFactory.load("pl/jaca/server/conf2.conf")
  val wrongConfig2 = ConfigFactory.load("pl/jaca/server/conf3.conf")
  val wrongConfig3 = ConfigFactory.load("pl/jaca/server/conf4.conf")

  "EventHandlerProvider" must {
    "load services from config" in {

    }
  }



}
object ServiceProviderSpec {
  class ServiceA extends Service {
    override def receive: Receive = ???
  }
  class ServiceB extends Service {
    override def receive: Receive = ???
  }
  class NotAService {

  }
  class ParametrizedService(a: Int) extends Service {
    override def receive: Actor.Receive = ???
  }
  abstract class AbstractService extends Service
}
