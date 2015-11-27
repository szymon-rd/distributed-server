package pl.jaca.testutils

import java.net.{InetSocketAddress, SocketAddress}

import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel.Unsafe
import io.netty.channel._
import io.netty.util.{Attribute, AttributeKey}

/**
 * @author Jaca777
 *         Created 2015-11-27 at 22
 */
class MockNettyChannel(port: Int, nid: Int) extends Channel{

  val address = new InetSocketAddress("dummy", port)
  val nettyId = new MockNettyChannelId(nid)

  override def voidPromise(): ChannelPromise = throw new UnsupportedOperationException

  override def eventLoop(): EventLoop = throw new UnsupportedOperationException

  override def isRegistered: Boolean = throw new UnsupportedOperationException

  override def writeAndFlush(msg: scala.Any, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def writeAndFlush(msg: scala.Any): ChannelFuture = throw new UnsupportedOperationException

  override def unsafe(): Unsafe = throw new UnsupportedOperationException

  override def config(): ChannelConfig = throw new UnsupportedOperationException

  override def disconnect(): ChannelFuture = throw new UnsupportedOperationException

  override def disconnect(promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def newProgressivePromise(): ChannelProgressivePromise = throw new UnsupportedOperationException

  override def metadata(): ChannelMetadata = throw new UnsupportedOperationException

  override def alloc(): ByteBufAllocator = throw new UnsupportedOperationException

  override def closeFuture(): ChannelFuture = throw new UnsupportedOperationException

  override def remoteAddress(): SocketAddress = address

  override def isActive: Boolean = throw new UnsupportedOperationException

  override def flush(): Channel = throw new UnsupportedOperationException

  override def newFailedFuture(cause: Throwable): ChannelFuture = throw new UnsupportedOperationException

  override def write(msg: scala.Any): ChannelFuture = throw new UnsupportedOperationException

  override def write(msg: scala.Any, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def localAddress(): SocketAddress = address

  override def isOpen: Boolean = throw new UnsupportedOperationException

  override def isWritable: Boolean = throw new UnsupportedOperationException

  override def close(): ChannelFuture = throw new UnsupportedOperationException

  override def close(promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def deregister(): ChannelFuture = throw new UnsupportedOperationException

  override def deregister(promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def read(): Channel = throw new UnsupportedOperationException

  override def newPromise(): ChannelPromise = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress): ChannelFuture = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress, localAddress: SocketAddress): ChannelFuture = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def pipeline(): ChannelPipeline = throw new UnsupportedOperationException

  override def bind(localAddress: SocketAddress): ChannelFuture = throw new UnsupportedOperationException

  override def bind(localAddress: SocketAddress, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def newSucceededFuture(): ChannelFuture = throw new UnsupportedOperationException

  override def id(): ChannelId = nettyId

  override def parent(): Channel = throw new UnsupportedOperationException

  override def compareTo(o: Channel): Int = throw new UnsupportedOperationException

  override def hasAttr[T](key: AttributeKey[T]): Boolean = throw new UnsupportedOperationException

  override def attr[T](key: AttributeKey[T]): Attribute[T] = throw new UnsupportedOperationException
}
