package pl.jaca.server.proxy.packets

import io.netty.buffer.ByteBuf

/**
 * @author Jaca777
 *         Created 2015-06-13 at 13
 */
abstract class Packet(val id: Short, val length: Short, val msg: Array[Byte]) extends Serializable
