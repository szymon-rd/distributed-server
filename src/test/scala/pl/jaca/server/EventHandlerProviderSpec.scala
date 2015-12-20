package pl.jaca.server

import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}

/**
 * @author Jaca777
 *         Created 2015-12-17 at 20
 */
class EventHandlerProviderSpec extends WordSpecLike with Matchers {

  val properConfig1 = ConfigFactory.load("pl/jaca/server/conf1.conf")
  val wrongConfig1 = ConfigFactory.load("pl/jaca/server/conf2.conf")
  val wrongConfig2 = ConfigFactory.load("pl/jaca/server/conf3.conf")
  val wrongConfig3 = ConfigFactory.load("pl/jaca/server/conf4.conf")

  "EventHandlerProvider" must {
    "load handlers from config" in {

    }
  }

}
object EventHandlerProviderSpec {

}
