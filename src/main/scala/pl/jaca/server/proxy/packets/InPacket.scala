package pl.jaca.server.proxy.packets

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import pl.jaca.server.proxy.Connection

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 */
abstract class InPacket(id: Short, length: Short, msg: Array[Byte], val sender: Connection) extends Packet(id, length, msg)
