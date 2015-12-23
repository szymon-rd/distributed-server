package pl.jaca.util.graph

/**
 * @author Jaca777
 *         Created 2015-12-23 at 11
 */
case class Node[T](value: T, leaves: Node[T]*) {

  def collect[R](f: (Seq[R], T) => R): R = {
    def collect(toVisit: Node[T]): R = {
      val s = toVisit.leaves.map(collect)
      f(s, toVisit.value)
    }
    collect(this)
  }

  def checkForCycle() = {
    def checkForCycle(toVisit: Node[T], visited: List[T]): Unit = {
      if(visited.contains(toVisit.value)){
        val path = s"${visited.mkString(" -> ")} -> ${toVisit.value}"
        throw new GraphException("Cyclic dependency found: " + path)
      } else {
        val path = visited :+ toVisit.value
        toVisit.leaves.foreach(leaf => checkForCycle(leaf, path))
      }
    }
    checkForCycle(this, List.empty)
  }
}
