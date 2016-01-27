package pl.jaca.server.providers

import java.lang.reflect.{Parameter, Constructor}

import akka.actor.ActorRef
import pl.jaca.server.{ServerConfigException, Inject}
import pl.jaca.server.service.Service

/**
  * @author Jaca777
  *         Created 2016-01-27 at 15
  */
case class ServiceProvider(name: String, constr: Constructor[Service]) {

  override def toString = name

  /**
    * Creates new service actor.
    */
  def createFrom(dependencies: Array[ActorRef]): Service = {
    constr.newInstance(dependencies: _*)
  }

  /**
    *
    * @return names of dependencies
    */
  def dependencies: Array[String] = {
    val params = constr.getParameters
    params.map(getServiceName)
  }

  /**
    * @return name of service given in Inject annotation.
    */
  private def getServiceName(param: Parameter): String = {
    val annotation = param.getAnnotation(classOf[Inject])
    annotation.serviceName()
  }

}

object ServiceProvider {
  def apply(name: String, clazz: Class[_ <: Service]): ServiceProvider = ServiceProvider(name, getConstructor(clazz))

  /**
    * Resolves constructor
    */
  private def getConstructor(clazz: Class[_ <: Service]): Constructor[Service] = {
    def isActorRef(param: Parameter): Boolean = {
      param.getType.isAssignableFrom(classOf[ActorRef])
    }

    def isInjectDefined(param: Parameter): Boolean = {
      param.getAnnotations.exists(_.isInstanceOf[Inject])
    }

    def isInjectable(c: Constructor[_]): Boolean = {
      c.getParameters.forall(param => isActorRef(param) && isInjectDefined(param))
    }
    val constructors = clazz.getConstructors
    val constructor = constructors.find(isInjectable)
    if (constructor.isEmpty)
      throw new ServerConfigException(s"Service ${clazz.getName} constructor has not injectable parameters.")
    else constructor.get.asInstanceOf[Constructor[Service]]
  }
}