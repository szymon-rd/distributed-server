package pl.jaca.server

import com.typesafe.config.Config
import pl.jaca.server.ServiceProvider._
import pl.jaca.server.proxy.service.Service


/**
 * @author Jaca777
 *         Created 2015-12-16 at 16
 */
private[server] class ServiceProvider(config: Config) {
  private val servicesEntry = config.getStringList(servicesPath).toArray.map(_.asInstanceOf[String])
  private val services = servicesEntry.map {
    className =>
      val classLoader = this.getClass.getClassLoader
      val clazz = classLoader.loadClass(className)
      if(!clazz.getConstructors.exists(_.getParameterCount == 0))
        throw new ServerInitializationException("Service " + className + " doesn't contain parameterless constructor.")
      else clazz.newInstance().asInstanceOf[Service]
  }

  def getServices: Array[Service] = services
}
object ServiceProvider {
  val servicesPath = "server-app.context.services"
}
