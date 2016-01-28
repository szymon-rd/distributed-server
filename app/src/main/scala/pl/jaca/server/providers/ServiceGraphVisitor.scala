package pl.jaca.server.providers

import akka.actor.{ActorRef, Props}
import pl.jaca.util.graph.{Node, BottomToTopGraphVisitor}

/**
  * @author Jaca777
  *         Created 2016-01-27 at 15
  */
private class ServiceGraphVisitor(actorFactory: ((Props, String) => ActorRef)) extends BottomToTopGraphVisitor[ServiceProvider] {
  var services = Map[String, ActorRef]()

  override def visitNode(node: Node[ServiceProvider]): Unit = {
    val service = node.value
    if (!services.isDefinedAt(service.name)) {
      val ref = get(service)
      services += (service.name -> ref)
    }
  }

  def get(service: ServiceProvider) = {
    val dependencies = resolveDependencies(service)
    actorFactory(Props(service.createFrom(dependencies)), service.name)
  }

  private def resolveDependencies(service: ServiceProvider): Array[ActorRef] = {
    val dependencies = service.dependencies.map(services.get)
    dependencies.map {
      case Some(s) => s
      case None =>
        sys.error(s"Unable to resolve dependency for service $service")
    }
  }
}

