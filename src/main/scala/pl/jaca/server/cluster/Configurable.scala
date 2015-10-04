package pl.jaca.server.cluster

import akka.actor.Actor
import com.typesafe.config.Config

/**
 * @author Jaca777
 *         Created 2015-10-02 at 23
 */
trait Configurable extends Actor {
  val config = context.system.settings.config
  implicit val defaultPath = ""
  implicit class Configuration(config: Config){
    def get(value: String)(implicit path: String) = config.getString(s"$path.$value")
  }
}
