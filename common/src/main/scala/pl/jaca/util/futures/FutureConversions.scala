package pl.jaca.util.futures

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Jaca777
 *         Created 2015-12-23 at 18
 */
object FutureConversions {
  /**
   * Converts list of futures to future of lists.
    * Returned future completes when all futures in given list are completed.
   */
  def all[T](fs: List[Future[T]])(implicit executor: ExecutionContext): Future[List[T]] =
    fs.foldRight(Future(Nil:List[T]))((future, fs2) =>
      for {
        x <- future
        xs <- fs2
      } yield x :: xs)
}
