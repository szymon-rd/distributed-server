package pl.jaca.util.graph

/**
  * @author Jaca777
  *         Created 2016-01-24 at 13
  */
abstract class NodeVisitor[A] {
  def visitNode(node: Node[A])
}
