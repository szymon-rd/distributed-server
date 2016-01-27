package pl.jaca.server.providers

import java.lang.reflect.{Constructor, Modifier}
import java.util.Map.Entry

import akka.actor.{ActorRef, Props}
import akka.event.LoggingAdapter
import com.typesafe.config.{Config, ConfigObject, ConfigValue}
import pl.jaca.server.ServerConfigException
import pl.jaca.server.providers.ServiceDependencyResolver._
import pl.jaca.server.service.Service
import pl.jaca.util.graph.{DirectedGraph, GraphException}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext


/**
  * @author Jaca777
  *         Created 2015-12-16 at 16
  */
private[server] class ServiceDependencyResolver(config: Config, actorFactory: ((Props, String) => ActorRef), log: LoggingAdapter)(implicit executionContext: ExecutionContext) {

  /**
    * Maps service name to service class.
    */
  private val serviceClasses: Map[String, Class[_ <: Service]] = {
    val list: Iterable[ConfigObject] = config.getObjectList(servicesPath).asScala
    (for {
      item: ConfigObject <- list
      entry: Entry[String, ConfigValue] <- item.entrySet().asScala
      name = entry.getKey
      className = entry.getValue.unwrapped().toString
    } yield name -> resolveClass(className)).toMap
  }

  private def resolveClass(className: String): Class[_ <: Service] = {
    val classLoader = this.getClass.getClassLoader
    try {
      val clazz = classLoader.loadClass(className)
      if (!classOf[Service].isAssignableFrom(clazz))
        throw new ServerConfigException(className + " is not type of Service.")
      if (Modifier.isAbstract(clazz.getModifiers))
        throw new ServerConfigException("Service " + className + " is an abstract class.")
      clazz.asInstanceOf[Class[_ <: Service]]
    } catch {
      case c: ClassNotFoundException =>
        val e = new ServerConfigException("Service class not found.")
        e.initCause(c)
        throw e
    }
  }

  /**+
    * Maps service name to service instance.
    */
  private val services: Map[String, ActorRef] = {
    val elements = (for {
      elem <- serviceClasses
    } yield ServiceProvider(elem._1, elem._2)).toSeq
    val graph = createGraph(elements)
    val visitor = new ServiceNodeVisitor(actorFactory)
    graph.accept(visitor)
    visitor.services
  }

  /**
    * Creates services dependency graph.
    */
  private def createGraph(elems: Seq[ServiceProvider]): DirectedGraph[ServiceProvider] = {
    def findConstructor(constructors: Seq[Constructor[Service]], diName: String) =
      constructors.find(constr => serviceClasses(diName) == constr.getDeclaringClass).get
    val constructors = elems.map(_.constr)
    val connections = for {
      service <- elems
      dependencyName <- service.dependencies
    } yield {
      val injServiceConstr = findConstructor(constructors, dependencyName)
      service -> ServiceProvider(dependencyName, injServiceConstr)
    }
    try {
      val connectionsGraph = DirectedGraph.fromConnections(connections)
      val unconnected = elems.filter(_.constr.getParameterCount == 0)
      connectionsGraph addNodes unconnected
    } catch {
      case g: GraphException => throw new ServerConfigException(g.getMessage)
    }
  }




  /**
    *
    * @param name
    * @return
    */
  def getService(name: String): Option[ActorRef] = {
    services.get(name)
  }
}

object ServiceDependencyResolver {
  val servicesPath = "server-app.context.services"
}
