package pl.jaca.server

import akka.actor._
import akka.testkit.{TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.server.ServiceProviderSpec._
import pl.jaca.server.providers.{ServiceDependencyResolver, ServiceDependencyResolver$}
import pl.jaca.server.service.Service

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Jaca777
  *         Created 2015-12-17 at 20
  */
class ServiceProviderSpec extends TestKit(ActorSystem("ServiceProviderSpec")) with WordSpecLike with Matchers {

  implicit val ec = Implicits.global

  val log = new DummyLoggingAdapter

  val properConfig1 = ConfigFactory.load("server/conf1.conf")
  val wrongConfig1 = ConfigFactory.load("server/conf2.conf")
  val wrongConfig2 = ConfigFactory.load("server/conf3.conf")
  val wrongConfig3 = ConfigFactory.load("server/conf4.conf")
  val wrongConfig4 = ConfigFactory.load("server/conf5.conf")

  System.setProperty("app.config", "server/distrConf.conf")

  var actors: List[String] = List.empty

  def createTestActor(p: Props, name: String) = {
    actors ::= name
    TestActorRef(p)
  }

  "ServiceProvider" must {
    "load services from config and inject dependencies" in {
      val serviceProvider = new ServiceDependencyResolver(properConfig1, createTestActor, log)

      val optionA = serviceProvider.getService("serviceA")
      optionA.isDefined should be(true)
      val serviceA = optionA.get.asInstanceOf[TestActorRef[Service]].underlyingActor
      serviceA shouldBe a[ServiceA]
      val castedA = serviceA.asInstanceOf[ServiceA]
      castedA.service1 shouldNot be(null)
      castedA.service1.asInstanceOf[TestActorRef[Service]].underlyingActor shouldBe a[ServiceB]

      val optionF = serviceProvider.getService("serviceG")
      optionF.isDefined should be(true)
      val serviceF = optionF.get.asInstanceOf[TestActorRef[Service]].underlyingActor
      serviceF shouldBe a[ServiceG]

      val optionUnc = serviceProvider.getService("uncB")
      optionUnc.isDefined should be(true)
      val uncB = optionUnc.get.asInstanceOf[TestActorRef[Service]].underlyingActor
      uncB shouldBe a[UnconnectedServiceB]
    }
    "Create only one actor of each service" in {
      actors = List.empty
      val serviceProvider = new ServiceDependencyResolver(properConfig1, createTestActor, log)
      within(100 millis) {
        actors should contain theSameElementsAs (List("serviceA", "serviceB", "serviceC", "serviceD",
          "serviceE", "serviceF", "serviceG", "uncA", "uncB"))
      }
    }
    "throw exception when class is not type of service" in {
      intercept[ServerConfigException] {
        new ServiceDependencyResolver(wrongConfig1, createTestActor, log)
      }.getMessage should be("pl.jaca.server.ServiceProviderSpec$NotAService is not type of Service.")
    }
    "throw exception when class is abstract" in {
      intercept[ServerConfigException] {
        new ServiceDependencyResolver(wrongConfig2, createTestActor, log)
      }.getMessage should be("Service pl.jaca.server.ServiceProviderSpec$AbstractService is an abstract class.")
    }
    "throw exception when class constructor has not injectable params." in {
      intercept[ServerConfigException] {
        new ServiceDependencyResolver(wrongConfig3, createTestActor, log)
      }.getMessage should be("Service pl.jaca.server.ServiceProviderSpec$NonInjectableService constructor has not injectable parameters.")
    }
    "detect cyclic dependencies" in {
      intercept[ServerConfigException] {
        new ServiceDependencyResolver(wrongConfig4, createTestActor, log)
      }.getMessage should be("Cycle found: cyclicC -> cyclicA -> cyclicB -> cyclicC")
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

  class ServiceA(@Inject(serviceName = "serviceB") val service1: ActorRef, @Inject(serviceName = "serviceC") val service2: ActorRef, @Inject(serviceName = "serviceE") service: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceB extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceC(@Inject(serviceName = "serviceD") service1: ActorRef, @Inject(serviceName = "serviceE") service2: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceD(@Inject(serviceName = "serviceB") val service1: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceE(@Inject(serviceName = "serviceB") val service1: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceF(@Inject(serviceName = "serviceG") service: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class ServiceG extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class CyclicA(@Inject(serviceName = "cyclicB") cyclicB: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class CyclicB(@Inject(serviceName = "cyclicC") cyclicC: ActorRef) extends Service {
    override def receive: Actor.Receive = {
      case _ =>
    }
  }

  class CyclicC(@Inject(serviceName = "cyclicA") cyclicA: ActorRef) extends Service {
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
