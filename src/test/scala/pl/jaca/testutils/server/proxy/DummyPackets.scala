package pl.jaca.testutils.server.proxy

import pl.jaca.server.proxy.Connection
import pl.jaca.server.proxy.packets.{InPacket, OutPacket}

/**
 * @author Jaca777
 *         Created 2015-12-04 at 17
 */
object DummyPackets {

  class DummyInPacket(id: Short) extends InPacket(id, -1, Array.empty, Connection.NoConnection){
    def this() = this(-1)
  }

  class DummyOutPacket(id: Short) extends OutPacket(id, -1, Array.empty) {
    def this() = this(-1)
  }

}
