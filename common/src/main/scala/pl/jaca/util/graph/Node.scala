package pl.jaca.util.graph

/**
  * @author Jaca777
  *         Created 2015-12-23 at 11
  */
case class Node[+V](value: V, connections: Node[V]*) {

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

  def accept[A >: V](visitor: TopToBottomGraphVisitor[A]): Unit = { //IntelliJ reports type mismatch. Compiles.
    visitor.visitNode(this)
    for (node <- connections) node.accept(visitor)
  }

  def accept[A >: V](visitor: BottomToTopGraphVisitor[A]): Unit = {
    for (node <- connections) node.accept(visitor)
    visitor.visitNode(this)
  }

}
