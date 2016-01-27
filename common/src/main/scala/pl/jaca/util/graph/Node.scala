package pl.jaca.util.graph

/**
  * @author Jaca777
  *         Created 2015-12-23 at 11
  */
case class Node[V](value: V, connections: Node[V]*) {

  def collect[A](f: (Seq[A], V) => A): A = {
    def collect(toVisit: Node[V]): A = {
      val s = toVisit.connections.map(collect)
      f(s, toVisit.value)
    }
    collect(this)
  }

  def checkForCycle() = {
    def checkForCycle(toVisit: Node[V], visited: List[V]): Unit = {
      if (visited.contains(toVisit.value)) {
        val path = s"${visited.mkString(" -> ")} -> ${toVisit.value}"
        throw new GraphException("Cycle found: " + path)
      } else {
        val path = visited :+ toVisit.value
        toVisit.connections.foreach(leaf => checkForCycle(leaf, path))
      }
    }
    checkForCycle(this, List.empty)
  }

  def accept(visitor: NodeVisitor[V]): Unit = {
    for (node <- connections) node.accept(visitor)
    visitor.visitNode(this)
  }
}
