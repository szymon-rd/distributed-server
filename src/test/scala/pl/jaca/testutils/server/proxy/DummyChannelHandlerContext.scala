package pl.jaca.testutils.server.proxy

import java.net.SocketAddress

import io.netty.buffer.ByteBufAllocator
import io.netty.channel._
import io.netty.util.concurrent.EventExecutor
import io.netty.util.{Attribute, AttributeKey}

/**
 * @author Jaca777
 *         Created 2015-12-04 at 17
 */
class DummyChannelHandlerContext extends ChannelHandlerContext {
  override def fireChannelUnregistered(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def voidPromise(): ChannelPromise = throw new UnsupportedOperationException

  override def writeAndFlush(msg: scala.Any, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def writeAndFlush(msg: scala.Any): ChannelFuture = throw new UnsupportedOperationException

  override def fireChannelInactive(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def disconnect(): ChannelFuture = throw new UnsupportedOperationException

  override def disconnect(promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def newProgressivePromise(): ChannelProgressivePromise = throw new UnsupportedOperationException

  override def fireChannelActive(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def fireChannelRegistered(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def fireChannelReadComplete(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def handler(): ChannelHandler = throw new UnsupportedOperationException

  override def executor(): EventExecutor = throw new UnsupportedOperationException

  override def alloc(): ByteBufAllocator = throw new UnsupportedOperationException

  override def flush(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def newFailedFuture(cause: Throwable): ChannelFuture = throw new UnsupportedOperationException

  override def name(): String = throw new UnsupportedOperationException

  override def write(msg: scala.Any): ChannelFuture = throw new UnsupportedOperationException

  override def write(msg: scala.Any, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def fireChannelWritabilityChanged(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def fireUserEventTriggered(event: scala.Any): ChannelHandlerContext = throw new UnsupportedOperationException

  override def close(): ChannelFuture = throw new UnsupportedOperationException

  override def close(promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def deregister(): ChannelFuture = throw new UnsupportedOperationException

  override def deregister(promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def read(): ChannelHandlerContext = throw new UnsupportedOperationException

  override def newPromise(): ChannelPromise = throw new UnsupportedOperationException

  override def channel(): Channel = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress): ChannelFuture = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress, localAddress: SocketAddress): ChannelFuture = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def connect(remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def pipeline(): ChannelPipeline = throw new UnsupportedOperationException

  override def fireExceptionCaught(cause: Throwable): ChannelHandlerContext = throw new UnsupportedOperationException

  override def bind(localAddress: SocketAddress): ChannelFuture = throw new UnsupportedOperationException

  override def bind(localAddress: SocketAddress, promise: ChannelPromise): ChannelFuture = throw new UnsupportedOperationException

  override def newSucceededFuture(): ChannelFuture = throw new UnsupportedOperationException

  override def isRemoved: Boolean = throw new UnsupportedOperationException

  override def fireChannelRead(msg: scala.Any): ChannelHandlerContext = throw new UnsupportedOperationException

  override def hasAttr[T](key: AttributeKey[T]): Boolean = throw new UnsupportedOperationException

  override def attr[T](key: AttributeKey[T]): Attribute[T] = throw new UnsupportedOperationException
}
