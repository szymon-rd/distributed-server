package pl.jaca.cluster

import com.typesafe.config.{Config, ConfigFactory}

/**
 * @author Jaca777
 *         Created 2015-10-02 at 23
 */
trait Configurable {
  val appConfig = ConfigFactory.load(System.getProperty("app.config"))


  implicit class Configuration(config: Config) {
    def stringAt(value: String): Option[String] = {
      if (config.hasPath(value)) Some(config.getString(value)) else None
    }

    def intAt(value: String): Option[Int] = {
      if (config.hasPath(value)) Some(config.getInt(value)) else None
    }

    def stringsAt(value: String): Option[Array[String]] = ???

  }

  def requireConfig(cond: Boolean, message: String) =
    if (!cond) throw new ConfigurationException(message)


}

class ConfigurationException(s: String) extends RuntimeException
