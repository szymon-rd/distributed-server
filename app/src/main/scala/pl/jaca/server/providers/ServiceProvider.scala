package pl.jaca.server.providers

import java.lang.reflect.{Constructor, Modifier, Parameter}
import java.util.Map.Entry

import akka.actor.{ActorRef, Props}
import com.typesafe.config.{Config, ConfigObject, ConfigValue}
import pl.jaca.server.providers.ServiceProvider._
import pl.jaca.server.service.Service
import pl.jaca.server.{DI, ServerConfigException}
import pl.jaca.util.futures.FutureConversions
import pl.jaca.util.graph.{DependencyGraph, GraphException}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}


/**
 * @author Jaca777
 *         Created 2015-12-16 at 16
 */
private[server] class ServiceProvider(config: Config, actorFactory: (Props => Future[ActorRef]))(implicit executionContext: ExecutionContext) {

  /**
   * Type of node value of the service dependency graph.
   */
  private case class GraphElem(name: String, constr: Constructor[Service]) {
    override def toString = name
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
  private val services: Map[String, Future[ActorRef]] = {
    val elements = (for {
      elem <- serviceClasses
      clazz = elem._2
      name = elem._1
      constructor = getConstructor(clazz)
    } yield GraphElem(name, constructor)).toSeq
    val graph = createGraph(elements)
    graph.reduceEachRoot[Seq[(String, Future[ActorRef])]](createService).flatten.toMap
  }

  /**
   * Creates services dependency graph.
   */
  private def createGraph(elems: Seq[GraphElem]): DependencyGraph[GraphElem] = {
    def findConstructor(constructors: Seq[Constructor[Service]], diName: String) =
      constructors.find(constr => serviceClasses(diName) == constr.getDeclaringClass).get
    val constructors = elems.map(_.constr)
    val connections = for {
      elem <- elems
      name = elem.name
      constr = elem.constr
      param <- constr.getParameters
      diName = getServiceName(param)
      diConstr = findConstructor(constructors, diName)
    } yield (GraphElem(name, constr), GraphElem(diName, diConstr))
    try {
      val connectionsGraph = connections.foldLeft(z = new DependencyGraph[GraphElem]()) {
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
      param.getAnnotations.exists(_.isInstanceOf[DI])
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
   * Graph collector. Calls actorFactory and accumulates futures of actor refs.
   */
  private def createService(servicesAcc: Seq[Seq[(String, Future[ActorRef])]], elem: GraphElem): Seq[(String, Future[ActorRef])] = {
    val constructor = elem.constr
    val diNames = constructor.getParameters.map(getServiceName)
    val services = servicesAcc.flatten
    val diServices = diNames.map(services.toMap).toList
    val future = FutureConversions.all(diServices)
    val propsFuture = future.map(params => Props(constructor.newInstance(params: _*)))
    val instance = for {
      props <- propsFuture
      actor <- actorFactory(props)
    } yield actor
    val name = elem.name
    services :+(name, instance)
  }

  /**
   * Resolves name of service given in DI annotation.
   */
  private def getServiceName(param: Parameter): String = {
    val annotation = param.getAnnotation(classOf[DI])
    annotation.serviceName()
  }

  /**
   *
   * @param name
   * @return
   */
  def getService(name: String): Option[Future[ActorRef]] = {
    services.get(name)
  }
}

object ServiceProvider {
  val servicesPath = "server-app.context.services"
}
