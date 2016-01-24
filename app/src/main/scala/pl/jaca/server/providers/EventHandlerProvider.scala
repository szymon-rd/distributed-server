package pl.jaca.server.providers

import java.lang.reflect.{Constructor, Modifier}
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.Config
import pl.jaca.server.eventhandling.EventActor
import pl.jaca.server.providers.EventHandlerProvider._
import pl.jaca.server.{Inject, ServerConfigException}

import scala.concurrent.ExecutionContext

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
  private def createFactory(className: String): () => EventActor = {
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
  private def newFactory(constructor: Constructor[_]): (() => EventActor) = {
    val names = getServicesNames(constructor)
    val services = resolveServices(names)
    () => constructor.newInstance(services: _*).asInstanceOf[EventActor]
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
  private def resolveServices(names: Array[String]): List[ActorRef] = {
    val options = names.map(name => (name, services.getService(name)))
    val unknownServices = options.filter(_._2.isEmpty).map(_._1)
    if (unknownServices.isEmpty) options.map(_._2.get).toList
    else reportUnresolvedServices(unknownServices)
  }

  private def reportUnresolvedServices(unknownServices: Array[String]) = {
    throw new ServerConfigException("Unresolved services: " + unknownServices.mkString(", "))
  }


  /**
    * @return Event actors loaded from config as lazy values.
    */
  lazy val eventActors: List[ActorRef] = {
   createHandlers(actorsFactories.toList)
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
