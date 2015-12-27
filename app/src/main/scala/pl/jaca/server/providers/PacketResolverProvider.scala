package pl.jaca.server.providers

import java.lang.reflect.Modifier

import com.typesafe.config.Config
import pl.jaca.server.ServerConfigException
import pl.jaca.server.networking.PacketResolver
import pl.jaca.server.providers.PacketResolverProvider._


/**
 * @author Jaca777
 *         Created 2015-12-16 at 16
 */
private[server] class PacketResolverProvider(config: Config) {
  private val resolversEntry = config.getStringList(resolversPath).toArray.map(_.asInstanceOf[String])
  private val resolvers = resolversEntry.map(createResolver)


  def createResolver(className: String) = {
    val clazz: Class[_] = getResolverClass(className)
    clazz.newInstance().asInstanceOf[PacketResolver]
  }

  private def getResolverClass(className: String): Class[PacketResolver] = {
    try {
      val classLoader = this.getClass.getClassLoader
      val clazz = classLoader.loadClass(className)
      if (!classOf[PacketResolver].isAssignableFrom(clazz)) //(checking order makes difference)
        throw new ServerConfigException(className + " is not type of PacketResolver.")
      if (Modifier.isAbstract(clazz.getModifiers))
        throw new ServerConfigException("Resolver " + className + " is an abstract class.")
      if (!clazz.getConstructors.exists(_.getParameterCount == 0))
        throw new ServerConfigException("Resolver " + className + " has no parameterless constructor defined.")
      clazz.asInstanceOf[Class[PacketResolver]]
    } catch {
      case c: ClassNotFoundException =>
        val e = new ServerConfigException("Resolver class not found.")
        e.initCause(c)
        throw e
    }
  }

  private val resolver = mergeResolvers(resolvers)

  def mergeResolvers(resolvers: Array[PacketResolver]) = resolvers.reduceLeft(_ and _)

  def getResolver: PacketResolver = resolver
}

object PacketResolverProvider {
  val resolversPath = "server-app.context.resolvers"
}