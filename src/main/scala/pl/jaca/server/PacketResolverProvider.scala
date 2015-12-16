package pl.jaca.server

import com.typesafe.config.Config
import pl.jaca.server.PacketResolverProvider.resolversPath
import pl.jaca.server.proxy.server.PacketResolver


/**
 * @author Jaca777
 *         Created 2015-12-16 at 16
 */
private[server] class PacketResolverProvider(config: Config) {
  private val resolversEntry = config.getStringList(resolversPath).toArray.map(_.asInstanceOf[String])
  private val resolvers = resolversEntry.map {
    className =>
      val classLoader = this.getClass.getClassLoader
      val clazz = classLoader.loadClass(className)
      if(!clazz.getConstructors.exists(_.getParameterCount == 0))
        throw new ServerInitializationException("Resolver " + className + " doesn't contain parameterless constructor.")
      else clazz.newInstance().asInstanceOf[PacketResolver]
  }
  
  def getResolvers: Array[PacketResolver] = resolvers
}
object PacketResolverProvider {
  val resolversPath = "server-app.context.resolvers"
}