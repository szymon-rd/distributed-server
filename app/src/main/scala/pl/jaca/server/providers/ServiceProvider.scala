package pl.jaca.server.providers

import java.lang.reflect.{Constructor, Modifier, Parameter}
import java.util.Map.Entry

import akka.actor.{ActorRef, Props}
import akka.event.LoggingAdapter
import com.typesafe.config.{Config, ConfigObject, ConfigValue}
import pl.jaca.server.providers.ServiceProvider._
import pl.jaca.server.service.Service
import pl.jaca.server.{Inject, ServerConfigException}
import pl.jaca.util.graph.{Node, NodeVisitor, DirectedDependencyGraph, GraphException}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext


/**
  * @author Jaca777
  *         Created 2015-12-16 at 16
  */
private[server] class ServiceProvider(config: Config, actorFactory: ((Props, String) => ActorRef), log: LoggingAdapter)(implicit executionContext: ExecutionContext) {

  /**
    * Type of node value of the service dependency graph.
    */
  private case class GraphElem(name: String, constr: Constructor[Service]) {
    override def toString = name
  }

  private class ServiceNodeVisitor extends NodeVisitor[GraphElem] {
    var services = Map[String, ActorRef]()

    override def visitNode(node: Node[GraphElem]): Unit = {
      val elem = node.value
      if(!services.isDefinedAt(elem.name)) {
        val ref = createActor(elem.constr, elem.name)
        services += (elem.name -> ref)
      }
    }

    private def createActor(constr: Constructor[Service], serviceName: String): ActorRef = {
      val dependencies = resolveDependencies(constr, serviceName)
      actorFactory(Props(constr.newInstance(dependencies: _*)), serviceName)
    }

    private def resolveDependencies(constr: Constructor[Service], serviceName: String): Array[ActorRef] = {
      val params = constr.getParameters
      val dependenciesNames = params.map(getServiceName)
      val dependencies = dependenciesNames.map(services.get)
      dependencies.map {
        case Some(service) => service
        case None =>
          sys.error(s"Unable to resolve dependency for service $serviceName")
      }
    }
  }

  /**
    * Maps service name to service class.
    */
  private val serviceClasses: Map[String, Class[_]] = {
    val list: Iterable[ConfigObject] = config.getObjectList(servicesPath).asScala
    (for {
      item: ConfigObject <- list
      entry: Entry[String, ConfigValue] <- item.entrySet().asScala
      name = entry.getKey
      className = entry.getValue.unwrapped().toString
    } yield name -> resolveClass(className)).toMap
  }

  private def resolveClass(className: String): Class[_] = {
    val classLoader = this.getClass.getClassLoader
    try {
      val clazz = classLoader.loadClass(className)
      if (!classOf[Service].isAssignableFrom(clazz))
        throw new ServerConfigException(className + " is not type of Service.")
      if (Modifier.isAbstract(clazz.getModifiers))
        throw new ServerConfigException("Service " + className + " is an abstract class.")
      clazz
    } catch {
      case c: ClassNotFoundException =>
        val e = new ServerConfigException("Service class not found.")
        e.initCause(c)
        throw e
    }
  }

  /**
    * Maps service name to service instance.
    */
  private val services: Map[String, ActorRef] = {
    val elements = (for {
      elem <- serviceClasses
      clazz = elem._2
      name = elem._1
      constructor = getConstructor(clazz)
    } yield GraphElem(name, constructor)).toSeq
    val graph = createGraph(elements)
    val visitor = new ServiceNodeVisitor
    graph.accept(visitor)
    visitor.services
  }

  /**
    * Creates services dependency graph.
    */
  private def createGraph(elems: Seq[GraphElem]): DirectedDependencyGraph[GraphElem] = {
    def findConstructor(constructors: Seq[Constructor[Service]], diName: String) =
      constructors.find(constr => serviceClasses(diName) == constr.getDeclaringClass).get
    val constructors = elems.map(_.constr)
    val connections = for {
      elem <- elems
      param <- elem.constr.getParameters
      injServiceName = getServiceName(param)
      injServiceConstr = findConstructor(constructors, injServiceName)
    } yield (GraphElem(elem.name, elem.constr), GraphElem(injServiceName, injServiceConstr))
    try {
      val connectionsGraph = connections.foldLeft(z = new DirectedDependencyGraph[GraphElem]()) {
        (graph, conn) => graph.addEdge(conn._1, conn._2)
      }
      val unconnected = elems.filter(_.constr.getParameterCount == 0)
      val fullGraph = unconnected.foldLeft(connectionsGraph) {
        (graph, elem) => graph.add(elem)
      }
      fullGraph
    } catch {
      case g: GraphException => throw new ServerConfigException(g.getMessage)
    }
  }

  /**
    * Resolves constructor
    */
  private def getConstructor(clazz: Class[_]) = {
    def isActorRef(param: Parameter): Boolean = {
      param.getType.isAssignableFrom(classOf[ActorRef])
    }

    def isDIDefined(param: Parameter): Boolean = {
      param.getAnnotations.exists(_.isInstanceOf[Inject])
    }

    def isInjectable(c: Constructor[_]): Boolean = {
      c.getParameters.forall(param => isActorRef(param) && isDIDefined(param))
    }
    val constructors = clazz.getConstructors
    val constructor = constructors.find(isInjectable)
    if (constructor.isEmpty)
      throw new ServerConfigException(s"Service ${clazz.getName} constructor has not injectable parameters.")
    else constructor.get.asInstanceOf[Constructor[Service]]
  }

  /**
    * Resolves name of service given in Inject annotation.
    */
  private def getServiceName(param: Parameter): String = {
    val annotation = param.getAnnotation(classOf[Inject])
    annotation.serviceName()
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

object ServiceProvider {
  val servicesPath = "server-app.context.services"
}
