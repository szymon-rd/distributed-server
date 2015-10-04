package pl.jaca.server

import pl.jaca.server.chat.Chat
import pl.jaca.server.cluster.Application
import pl.jaca.server.cluster.Application.Launch
import pl.jaca.server.cluster.distribution.Distribution

/**
 * @author Jaca777
 *         Created 2015-06-15 at 21
 */
class GameClusterRoot extends Application with Distribution {

  override def receive: Receive = {
    case Launch => {
      context.distribute(new Chat())
    }
  }

}
