package pl.jaca.util.testing

import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}

/**
 * @author Jaca777
 *         Created 2015-11-27 at 17
 */
trait TypeMatchers {
  def anInstanceOf[T](implicit manifest: Manifest[T]) = {
    val clazz = manifest.runtimeClass.asInstanceOf[Class[T]]
    new BePropertyMatcher[AnyRef] {
      def apply(left: AnyRef) = BePropertyMatchResult(
        clazz.isAssignableFrom(left.getClass),
        "an instance of " + clazz.getName)
    }
  }
}
