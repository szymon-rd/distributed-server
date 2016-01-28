package pl.jaca.util.graph

/**
  * @author Jaca777
  *         Created 2015-12-23 at 11
  */
case class DirectedGraph[+A](val rootNodes: Node[A]*) {


  /**
    * Creates new graph with an additional edge.
    *
    * @param from
    * @param to
    * @return Created graph.
    */
  def addEdge[B >: A](from: B, to: B): DirectedGraph[B] = {
    if (from == to) throw new GraphException(s"Cycle found: $from -> $to")
    val fromNode = resolveNode(from)
    val toNode = resolveNode(to)
    if (fromNode.isEmpty && toNode.isEmpty) addEdge(from, new Node[B](to))
    else if (fromNode.isEmpty) addEdge(from, toNode.get)
    else if (toNode.isEmpty) addEdge(fromNode.get, to)
    else addEdge(fromNode.get, toNode.get)
  }


  /**
    * Creates new graph with additional node.
    * @param e new element
    * @return Created graph.
    */
  def addNode[B >: A](e: B): DirectedGraph[B] = {
    val roots: Seq[Node[_ >: A <: B]] = rootNodes :+ new Node[B](e)
    new DirectedGraph[B](roots: _*)
  }

  def addNodes[B >: A](es: Seq[B]): DirectedGraph[B] = {
    es.foldLeft[DirectedGraph[B]](this) {
      (graph, elem) => graph.addNode(elem)
    }
  }

  private def addEdge[B >: A](from: B, to: Node[B]): DirectedGraph[B] = {
    val newNode = new Node[B](from, to)
    val roots = rootNodes :+ newNode
    new DirectedGraph[B](roots: _*)
  }

  private def addEdge[B >: A](from: Node[B], to: B): DirectedGraph[B] = {
    val newNode = new Node[B](to)
    val newLeafs = from.connections :+ newNode
    val updatedNode = new Node(from.value, newLeafs: _*)
    update(from, updatedNode)
  }

  private def addEdge[B >: A](from: Node[B], to: Node[B]): DirectedGraph[B] = {
    val leafs = from.connections :+ to
    val updatedNode = new Node[B](from.value, leafs: _*)
    updatedNode.checkForCycle()
    update(from, updatedNode)
  }


  /**
    * Finds node with value @from in graph.
    *
    * @return
    */
  def resolveNode[B >: A](value: B): Option[Node[B]] = {
    def resolveAcc(node: Node[A]): Option[Node[A]] = {
      if (node.value != value) {
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
  def update[B >: A](node: Node[B], newNode: Node[B]): DirectedGraph[B] = {
    def updateAcc(currNode: Node[A]): Node[B] = {
      if (currNode == node) newNode
      else {
        val value = currNode.value
        val newLeafs = currNode.connections.map(updateAcc)
        new Node[B](value, newLeafs: _*)
      }
    }
    new DirectedGraph[B](rootNodes.map(updateAcc): _*)
  }

  def accept[B >: A](visitor: BottomToTopGraphVisitor[B]): Unit =
    for (root <- rootNodes) root.accept(visitor)

  def accept[B >: A](visitor: TopToBottomGraphVisitor[B]): Unit =
    for (root <- rootNodes) root.accept(visitor)

}

object DirectedGraph {
  def fromConnections[A](connections: Seq[(A, A)]) = connections.foldLeft(z = new DirectedGraph[A]()) {
    (graph, conn) => graph.addEdge(conn._1, conn._2)
  }
}

