package pl.jaca.testutils

import io.netty.channel.ChannelId

/**
 * @author Jaca777
 *         Created 2015-11-27 at 22
 */
class MockNettyChannelId(id: Int) extends ChannelId{
  override def asLongText(): String = id.toString

  override def asShortText(): String = id.toString

  override def compareTo(o: ChannelId): Int = throw new UnsupportedOperationException
}
