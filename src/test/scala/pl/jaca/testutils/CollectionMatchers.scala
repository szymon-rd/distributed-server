package pl.jaca.testutils

import org.scalatest.matchers.{MatchResult, Matcher}

/**
 * @author Jaca777
 *         Created 2015-09-12 at 18
 */
trait CollectionMatchers {

  implicit class IterableContainsImplicit[T](iterable: Iterable[T]) {
    def containsPattern(pattern: T => Boolean): Boolean = iterable match {
      case seq: Seq[T] => seq.exists(pattern)
      case set: Set[T] => set.exists(pattern)
      case map: Map[_, T] => map.values.exists(pattern) //Do not change to 'contains' (it becomes recursive then)
    }

    def contains(elem: T): Boolean = iterable.containsPattern(_ == elem)
  }

  class ContainAllMatcher[T](elements: Iterable[T]) extends Matcher[Iterable[T]] {
    override def apply(left: Iterable[T]): MatchResult = MatchResult(
      elements.forall(left.contains),
      "Set doesn't contain all the given elements",
      "Set contains all the given elements"
    )
  }

  def containAll[T](elements: T*): ContainAllMatcher[T] = new ContainAllMatcher[T](elements)

  class ContainOnlyMatcher[T](elements: Iterable[T]) extends Matcher[Iterable[T]] {
    override def apply(left: Iterable[T]): MatchResult = MatchResult(
      left.forall(elements.contains) && elements.forall(left.contains),
      "Set doesn't contain only the given elements",
      "Set contains only the given elements"
    )
  }

  def containOnly[T](elements: T*): ContainAllMatcher[T] = new ContainAllMatcher[T](elements)


}
