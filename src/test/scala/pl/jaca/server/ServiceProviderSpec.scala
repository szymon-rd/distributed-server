package pl.jaca.server

import akka.actor._
import akka.testkit.{TestKit, TestActorRef}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.ServiceProviderSpec._
import pl.jaca.server.providers.ServiceProvider
import pl.jaca.server.service.Service
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * @author Jaca777
 *         Created 2015-12-17 at 20
 */
class ServiceProviderSpec extends TestKit(ActorSystem("ServiceProviderSpec")) with WordSpecLike with Matchers {

  val properConfig1 = ConfigFactory.load("pl/jaca/server/conf1.conf")
  val wrongConfig1 = ConfigFactory.load("pl/jaca/server/conf2.conf")
  val wrongConfig2 = ConfigFactory.load("pl/jaca/server/conf3.conf")
  val wrongConfig3 = ConfigFactory.load("pl/jaca/server/conf4.conf")
  val wrongConfig4 = ConfigFactory.load("pl/jaca/server/conf5.conf")


  def createTestActor(p: Props) = Future(TestActorRef(p))

  "ServiceProvider" must {
    "load services from config and inject dependencies" in {
      val serviceProvider = new ServiceProvider(properConfig1, createTestActor)

      val optionA = serviceProvider.getService("serviceA")
      optionA.isDefined should be(true)
      val serviceA = Await.result(optionA.get, 200 millis).asInstanceOf[TestActorRef[Service]].underlyingActor
      serviceA shouldBe a[ServiceA]
      val castedA = serviceA.asInstanceOf[ServiceA]
      castedA.service1 shouldNot be(null)
      castedA.service1.asInstanceOf[TestActorRef[Service]].underlyingActor shouldBe a[ServiceB]

      val optionF = serviceProvider.getService("serviceG")
      optionF.isDefined should be(true)
      val serviceF = Await.result(optionF.get, 200 millis).asInstanceOf[TestActorRef[Service]].underlyingActor
      serviceF shouldBe a[ServiceG]

      val optionUnc = serviceProvider.getService("uncB")
      optionUnc.isDefined should be(true)
      val uncB = Await.result(optionUnc.get, 200 millis).asInstanceOf[TestActorRef[Service]].underlyingActor
      uncB shouldBe a[UnconnectedServiceB]
    }
    "throw exception when class is not type of service" in {
      intercept[ServerConfigException] {
      new ServiceProvider(wrongConfig1, createTestActor)
      }.getMessage should be("pl.jaca.server.ServiceProviderSpec$NotAService is not type of Service.")
    }
    "throw exception when class is abstract" in {
      intercept[ServerConfigException] {
      new ServiceProvider(wrongConfig2, createTestActor)
      }.getMessage should be("Service pl.jaca.server.ServiceProviderSpec$AbstractService is an abstract class.")
    }
    "throw exception when class constructor has not injectable params." in {
      intercept[ServerConfigException] {
      new ServiceProvider(wrongConfig3, createTestActor)
      }.getMessage should be("Service pl.jaca.server.ServiceProviderSpec$NonInjectableService constructor has not injectable parameters.")
    }
    "detect cyclic dependencies" in {
      intercept[ServerConfigException] {
      new ServiceProvider(wrongConfig4, createTestActor)
      }.getMessage should be("Cyclic dependency found: cyclicC -> cyclicA -> cyclicB -> cyclicC")
    }
  }


}

object ServiceProviderSpec {

  class NotAService

  abstract class AbstractService extends Service

  class NonInjectableService(service: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceA(@DI(serviceName = "serviceB") val service1: ActorRef, @DI(serviceName = "serviceC") val service2: ActorRef, @DI(serviceName = "serviceE") service: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceB extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceC(@DI(serviceName = "serviceD") service1: ActorRef, @DI(serviceName = "serviceE") service2: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceD extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceE extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceF(@DI(serviceName = "serviceG") service: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceG extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class CyclicA(@DI(serviceName = "cyclicB") cyclicB: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class CyclicB(@DI(serviceName = "cyclicC") cyclicC: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class CyclicC(@DI(serviceName = "cyclicA") cyclicA: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class UnconnectedServiceA extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class UnconnectedServiceB extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }


}
