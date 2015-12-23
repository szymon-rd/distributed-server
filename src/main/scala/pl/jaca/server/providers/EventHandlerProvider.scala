package pl.jaca.server.providers

import java.lang.reflect.Constructor

import com.typesafe.config.Config
import pl.jaca.server.eventhandling.EventActor
import pl.jaca.server.providers.EventHandlerProvider._
import pl.jaca.server.service.Service
import pl.jaca.server.ServerConfigException

/**
 * @author Jaca777
 *         Created 2015-12-16 at 16
 */
private[server] class EventHandlerProvider(config: Config, services: ServiceProvider) {
  private val handlersEntry = config.getStringList(handlersPath).toArray.map(_.asInstanceOf[String])

  private val actorsProps = handlersEntry.map {
    className =>
      val classLoader = this.getClass.getClassLoader
      val clazz = classLoader.loadClass(className)
      val constructor = clazz.getConstructors.find(_.getParameterTypes
        .forall(classOf[Service].isAssignableFrom))
      if (constructor.isDefined) newFactory(constructor.get)
      else throw new ServerConfigException("EventHandler " + className + " doesn't contain constructor with injectable types.")
  }

  /**
   * Creates new instance of EventActor factory with given constructor.
   */
  private def newFactory(constructor: Constructor[_]): (() => EventActor) = {
    val services = resolveServices(constructor.getParameterTypes)
    () => constructor.newInstance(services).asInstanceOf[EventActor]
  }

  /**
   * Finds services of given types.
   * @return List of services
   * @throws ServerConfigException when service is not found in config.
   */
  private def resolveServices(classes: Array[Class[_]]) = {
    val options = classes.map(c => (c, services.getService(c.getName)))
    val unknownServices = options.filter(_._2.isEmpty).map(_._1)
    if (unknownServices.isEmpty) options.map(_._2.get)
    else reportUnresolvedServices(unknownServices)

  }

  private def reportUnresolvedServices(unknownServices: Array[Class[_]]) = {
    throw new ServerConfigException("Unresolved services: " + unknownServices.map(_.getSimpleName).mkString(", "))
  }


  /**
   * @return Event actors loaded from config as lazy values.
   */
  def getActorFactories: Array[(() => EventActor)] = actorsProps
}

object EventHandlerProvider {
  val handlersPath = "server-app.context.handlers"
}
