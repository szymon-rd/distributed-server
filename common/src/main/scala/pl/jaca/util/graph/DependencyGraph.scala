package pl.jaca.util.graph

/**
 * @author Jaca777
 *         Created 2015-12-23 at 11
 */
class DependencyGraph[A](val rootNodes: Node[A]*) {

  /**
   * Adds edge to graph.
   * @param from
   * @param to
   * @return
   */
  def addEdge(from: A, to: A): DependencyGraph[A] = {
    if(from == to) throw new GraphException(s"Cyclic dependency found: $from -> $to")
    val fromNode = resolveNode(from)
    val toNode = resolveNode(to)
    if (fromNode.isEmpty && toNode.isEmpty) addEdge(from, new Node[A](to))
    else if (fromNode.isEmpty) addEdge(from, toNode.get)
    else if (toNode.isEmpty) addEdge(fromNode.get, to)
    else addEdge(fromNode.get, toNode.get)
  }

  def add(e: A): DependencyGraph[A] = {
    val roots = rootNodes :+ new Node[A](e)
    new DependencyGraph[A](roots: _*)
  }

  private def addEdge(from: A, to: Node[A]): DependencyGraph[A] = {
    val newNode = new Node(from, to)
    val roots = rootNodes :+ newNode
    new DependencyGraph[A](roots: _*)
  }

  private def addEdge(from: Node[A], to: A): DependencyGraph[A] = {
    val newNode = new Node[A](to)
    val newLeafs = from.leaves :+ newNode
    val updatedNode = new Node(from.value, newLeafs: _*)
    update(from, updatedNode)
  }

  private def addEdge(from: Node[A], to: Node[A]): DependencyGraph[A] = {
    val leafs = from.leaves :+ to
    val updatedNode = new Node[A](from.value, leafs: _*)
    updatedNode.checkForCycle()
    update(from, updatedNode)
  }

  /**
   * Finds node with element @from in graph.
   * @param from
   * @return
   */
  def resolveNode(from: A): Option[Node[A]] = {
    def resolveAcc(node: Node[A]): Option[Node[A]] = {
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

  /**
   * Replaces @node with @newNode in graph.
   * @param node
   * @param newNode
   * @return
   */
  def update(node: Node[A], newNode: Node[A]): DependencyGraph[A] = {
    def updateAcc(currNode: Node[A]): Node[A] = {
      if (currNode == node) newNode
      else {
        val value = currNode.value
        val newLeafs = currNode.leaves.map(updateAcc)
        new Node[A](value, newLeafs: _*)
      }
    }
    new DependencyGraph[A](rootNodes.map(updateAcc): _*)
  }

  def collect[B](zero: A)(f: (Seq[B], A) => B): B = f(rootNodes.map(_.collect(f)), zero)
  def collectEachRoot[B](f: (Seq[B], A) => B): Seq[B] = rootNodes.map(_.collect(f))

}

