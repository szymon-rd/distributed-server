package pl.jaca.util.graph

/**
  * @author Jaca777
  *         Created 2016-01-28 at 14
  */
abstract class GraphVisitor[A] {
  def visitNode(node: Node[A])
}
