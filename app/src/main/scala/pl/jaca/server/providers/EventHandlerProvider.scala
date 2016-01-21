package pl.jaca.server.providers

import java.lang.reflect.{Constructor, Modifier}
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.Config
import pl.jaca.server.eventhandling.EventActor
import pl.jaca.server.providers.EventHandlerProvider._
import pl.jaca.server.{Inject, ServerConfigException}
import pl.jaca.util.futures.FutureConversions

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Jaca777
  *         Created 2015-12-16 at 16
  */
private[server] class EventHandlerProvider(config: Config, services: ServiceProvider, handlerFactory: ((Props, String) => ActorRef))(implicit executor: ExecutionContext) {
  private val handlersEntry = config.getStringList(handlersPath).toArray.map(_.asInstanceOf[String])

  private val actorsFactories = handlersEntry.map {
    className =>
      try {
        createFactory(className)
      } catch {
        case c: ClassNotFoundException =>
          val exception = new ServerConfigException("Event handler class not found")
          exception.initCause(c)
          throw exception
      }
  }

  /**
    * Creates EventActor factory (of type Future[() => EventActor]). Resolves class using its classloader.
    */
  private def createFactory(className: String): Future[() => EventActor] = {
    val classLoader: ClassLoader = this.getClass.getClassLoader
    val clazz = classLoader.loadClass(className)
    checkClass(clazz)
    val constructor = clazz.getConstructors.find(_.getParameterTypes
      .forall(classOf[ActorRef].isAssignableFrom))
    if (constructor.isDefined) newFactory(constructor.get)
    else throw new ServerConfigException("Event handler " + className + " has no constructor with injectable parameters.")
  }

  /**
    * Checks whether event handler class has allowed format, i.e. is subtype of EventActor and is not an abstract class.
    */
  private def checkClass(clazz: Class[_]): Unit = {
    if (!classOf[Actor].isAssignableFrom(clazz))
      throw new ServerConfigException(s"${clazz.getName} is not type of EventActor.")
    if (Modifier.isAbstract(clazz.getModifiers))
      throw new ServerConfigException(s"Event handler ${clazz.getName} is an abstract class.")
  }

  /**
    * Creates new instance of EventActor factory with given constructor.
    */
  private def newFactory(constructor: Constructor[_]): Future[(() => EventActor)] = {
    val names = getServicesNames(constructor)
    val servicesFuture = resolveServices(names)
    servicesFuture.map(services => () => constructor.newInstance(services: _*).asInstanceOf[EventActor])
  }

  /**
    * Returns service names from constructor parameters annotations.
    */
  private def getServicesNames(constructor: Constructor[_]): Array[String] = {
    val params = constructor.getParameters
    val annotations = params.map(_.getAnnotation(classOf[Inject]))
    annotations.map(_.serviceName())
  }

  /**
    * Resolves services with given names.
    */
  private def resolveServices(names: Array[String]): Future[List[ActorRef]] = {
    val options = names.map(name => (name, services.getService(name)))
    val unknownServices = options.filter(_._2.isEmpty).map(_._1)
    if (unknownServices.isEmpty) FutureConversions.all(options.map(_._2.get).toList)
    else reportUnresolvedServices(unknownServices)
  }

  private def reportUnresolvedServices(unknownServices: Array[String]) = {
    throw new ServerConfigException("Unresolved services: " + unknownServices.mkString(", "))
  }


  /**
    * @return Event actors loaded from config as lazy values.
    */
  def getEventActors: Future[List[ActorRef]] = {
   FutureConversions.all(actorsFactories.toList).map(createHandlers)
  }

  private val handlerCounter = new AtomicInteger()

  /**
    * Creates a handler.
    */
  private def createHandlers(list: List[() => EventActor]) = list.map(f => handlerFactory(Props(f()),
    s"handler-${handlerCounter.getAndIncrement()}"))
}

object EventHandlerProvider {
  val handlersPath = "server-app.context.handlers"
}
