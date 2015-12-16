package pl.jaca.server.cluster

import akka.actor.Actor
import com.typesafe.config.Config

/**
 * @author Jaca777
 *         Created 2015-10-02 at 23
 */
trait Configurable extends Actor {
  val config = context.system.settings.config
  implicit val defaultPath = "server-cluster"

  implicit class Configuration(config: Config) {
    def stringAt(value: String)(implicit path: String): Option[String] = {
      val absolutePath = s"$path.$value"
      if (config.hasPath(absolutePath)) Some(config.getString(absolutePath)) else None
    }

    def intAt(value: String)(implicit path: String): Option[Int] = {
      val absolutePath = s"$path.$value"
      if (config.hasPath(absolutePath)) Some(config.getInt(absolutePath)) else None
    }

    def stringsAt(value: String)(implicit path: String): Option[Array[String]] = ???

  }

  def requireConfig(cond: Boolean, message: String) =
    if (!cond) throw new ConfigurationException(message)


}

class ConfigurationException(s: String) extends RuntimeException
