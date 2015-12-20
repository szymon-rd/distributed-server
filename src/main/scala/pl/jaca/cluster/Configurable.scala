package pl.jaca.cluster

import akka.actor.Actor
import com.typesafe.config.{ConfigFactory, Config}

/**
 * @author Jaca777
 *         Created 2015-10-02 at 23
 */
trait Configurable extends Actor {
  val systemConfig = context.system.settings.config
  val appConfig = ConfigFactory.load(System.getenv("-Dapp.config"))

  implicit class Configuration(config: Config) {
    def stringAt(value: String): Option[String] = {
      val absolutePath = s"$value"
      if (config.hasPath(absolutePath)) Some(config.getString(absolutePath)) else None
    }

    def intAt(value: String): Option[Int] = {
      val absolutePath = s"$value"
      if (config.hasPath(absolutePath)) Some(config.getInt(absolutePath)) else None
    }

    def stringsAt(value: String): Option[Array[String]] = ???

  }

  def requireConfig(cond: Boolean, message: String) =
    if (!cond) throw new ConfigurationException(message)


}

class ConfigurationException(s: String) extends RuntimeException
