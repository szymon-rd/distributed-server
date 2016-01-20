package pl.jaca.cluster

import com.typesafe.config.{Config, ConfigFactory}

/**
 * @author Jaca777
 *         Created 2015-10-02 at 23
 */
trait Configurable {
  lazy val appConfig = ConfigFactory.load(System.getProperty("app.config"))

  implicit class Configuration(config: Config) {
    def stringAt(path: String): Option[String] = {
      if (config.hasPath(path)) Some(config.getString(path)) else None
    }

    def intAt(path: String): Option[Int] = {
      if (config.hasPath(path)) Some(config.getInt(path)) else None
    }
    

    def boolAt(path: String): Option[Boolean] = {
      if (config.hasPath(path)) Some(config.getBoolean(path)) else None
    }
  }

  def requireConfig(cond: Boolean, message: String) =
    if (!cond) throw new ConfigurationException(message)


}

class ConfigurationException(s: String) extends RuntimeException
