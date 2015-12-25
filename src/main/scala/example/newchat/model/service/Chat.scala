package example.newchat.model.service

import java.nio.charset.Charset

import pl.jaca.cluster.distribution.{AbsoluteLoad, Load}
import pl.jaca.server.service.Service

/**
 * @author Jaca777
 *         Created 2015-12-17 at 18
 */
class Chat extends Service {
  override def receive: Receive = ???

  override def getLoad: Load = AbsoluteLoad(2.0f)
}
object Chat {
  val charset = Charset.forName("UTF-16")
}
