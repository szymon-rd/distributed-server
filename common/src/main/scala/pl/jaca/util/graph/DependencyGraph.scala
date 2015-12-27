package pl.jaca.util.graph

/**
 * @author Jaca777
 *         Created 2015-12-23 at 11
 */
class DependencyGraph[T](val rootNodes: Node[T]*) {

  def addEdge(from: T, to: T): DependencyGraph[T] = {
    if(from == to) throw new GraphException(s"Cyclic dependency found: $from -> $to")
    val fromNode = resolveNode(from)
    val toNode = resolveNode(to)
    if (fromNode.isEmpty && toNode.isEmpty) addEdge(from, new Node[T](to))
    else if (fromNode.isEmpty) addEdge(from, toNode.get)
    else if (toNode.isEmpty) addEdge(fromNode.get, to)
    else addEdge(fromNode.get, toNode.get)
  }

  def add(e: T): DependencyGraph[T] = {
    val roots = rootNodes :+ new Node[T](e)
    new DependencyGraph[T](roots: _*)
  }

  private def addEdge(from: T, to: Node[T]): DependencyGraph[T] = {
    val newNode = new Node(from, to)
    val roots = rootNodes :+ newNode
    new DependencyGraph[T](roots: _*)
  }

  private def addEdge(from: Node[T], to: T): DependencyGraph[T] = {
    val newNode = new Node[T](to)
    val newLeafs = from.leaves :+ newNode
    val updatedNode = new Node(from.value, newLeafs: _*)
    update(from, updatedNode)
  }

  private def addEdge(from: Node[T], to: Node[T]): DependencyGraph[T] = {
    val leafs = from.leaves :+ to
    val updatedNode = new Node[T](from.value, leafs: _*)
    updatedNode.checkForCycle()
    update(from, updatedNode)
  }

  def resolveNode(from: T): Option[Node[T]] = {
    def resolveAcc(node: Node[T]): Option[Node[T]] = {
      if (node.value != from) {
        val leafs = node.leaves
        val resolved = leafs.map(resolveAcc)
        val found = resolved.find(_.isDefined)
        found.map(_.get)
      }
      else Some(node)
    }
    val resolved = rootNodes.map(resolveAcc)
    val found = resolved.find(_.isDefined)
    found.map(_.get)
  }

  def update(node: Node[T], newNode: Node[T]): DependencyGraph[T] = {
    def updateAcc(currNode: Node[T]): Node[T] = {
      if (currNode == node) newNode
      else {
        val value = currNode.value
        val newLeafs = currNode.leaves.map(updateAcc)
        new Node[T](value, newLeafs: _*)
      }
    }
    new DependencyGraph[T](rootNodes.map(updateAcc): _*)
  }

  def collect[R](zero: T)(f: (Seq[R], T) => R): R = f(rootNodes.map(_.collect(f)), zero)
  def collectEachRoot[R](f: (Seq[R], T) => R): Seq[R] = rootNodes.map(_.collect(f))

}

