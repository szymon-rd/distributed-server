package pl.jaca.util.graph

/**
  * @author Jaca777
  *         Created 2015-12-23 at 11
  */
case class DirectedGraph[A](val rootNodes: Node[A]*) {


  /**
    * Creates new graph with an additional edge.
    *
    * @param from
    * @param to
    * @return Created graph.
    */
  def addEdge(from: A, to: A): DirectedGraph[A] = {
    if (from == to) throw new GraphException(s"Cycle found: $from -> $to")
    val fromNode = resolveNode(from)
    val toNode = resolveNode(to)
    if (fromNode.isEmpty && toNode.isEmpty) addEdge(from, new Node[A](to))
    else if (fromNode.isEmpty) addEdge(from, toNode.get)
    else if (toNode.isEmpty) addEdge(fromNode.get, to)
    else addEdge(fromNode.get, toNode.get)
  }

  /**
   * Creates new graph with additional node.
   * @param e new element
   * @return Created graph.
   */
  def addNode(e: A): DirectedGraph[A] = {
    val roots = rootNodes :+ new Node[A](e)
    new DirectedGraph[A](roots: _*)
  }

  def addNodes(es: Seq[A]): DirectedGraph[A] = es.foldLeft(this) {
    (graph, elem) => graph.addNode(elem)
  }

  private def addEdge(from: A, to: Node[A]): DirectedGraph[A] = {
    val newNode = new Node(from, to)
    val roots = rootNodes :+ newNode
    new DirectedGraph[A](roots: _*)
  }

  private def addEdge(from: Node[A], to: A): DirectedGraph[A] = {
    val newNode = new Node[A](to)
    val newLeafs = from.connections :+ newNode
    val updatedNode = new Node(from.value, newLeafs: _*)
    update(from, updatedNode)
  }

  private def addEdge(from: Node[A], to: Node[A]): DirectedGraph[A] = {
    val leafs = from.connections :+ to
    val updatedNode = new Node[A](from.value, leafs: _*)
    updatedNode.checkForCycle()
    update(from, updatedNode)
  }


  /**
    * Finds node with value @from in graph.
    *
    * @param from
    * @return
    */
  def resolveNode(from: A): Option[Node[A]] = {
    def resolveAcc(node: Node[A]): Option[Node[A]] = {
      if (node.value != from) {
        val leafs = node.connections
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
    * Creates new graph with replaced @node with @newNode.
    *
    * @param node
    * @param newNode
    * @return Created graph.
    */
  def update(node: Node[A], newNode: Node[A]): DirectedGraph[A] = {
    def updateAcc(currNode: Node[A]): Node[A] = {
      if (currNode == node) newNode
      else {
        val value = currNode.value
        val newLeafs = currNode.connections.map(updateAcc)
        new Node[A](value, newLeafs: _*)
      }
    }
    new DirectedGraph[A](rootNodes.map(updateAcc): _*)
  }

  def accept(visitor: NodeVisitor[A]): Unit =
    for (root <- rootNodes) root.accept(visitor)

}

object DirectedGraph {
  def fromConnections[A](connections: Seq[(A, A)]) = connections.foldLeft(z = new DirectedGraph[A]()) {
    (graph, conn) => graph.addEdge(conn._1, conn._2)
  }
}

