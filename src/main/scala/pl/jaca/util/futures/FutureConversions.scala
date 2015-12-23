package pl.jaca.util.futures

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Jaca777
 *         Created 2015-12-23 at 18
 */
object FutureConversions {
  def all[T](fs: List[Future[T]])(implicit executor: ExecutionContext): Future[List[T]] =
    fs.foldRight(Future(Nil:List[T]))((f, fs2) =>
      for {
        x <- f
        xs <- fs2
      } yield x :: xs)
}
