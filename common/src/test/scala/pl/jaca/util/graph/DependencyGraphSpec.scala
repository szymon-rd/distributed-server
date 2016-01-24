package pl.jaca.util.graph

import org.scalatest.{Matchers, WordSpecLike}
import pl.jaca.util.testing.TypeMatchers

/**
 * @author Jaca777
 *         Created 2015-12-23 at 12
 */
class DependencyGraphSpec extends WordSpecLike with Matchers with TypeMatchers {
  "DependencyGraph" should {
    "add two new nodes" in {
      val graph = new DirectedDependencyGraph[Int]()
      val newGraph = graph.addEdge(1, 2)
      val rootNode = newGraph.rootNodes(0)
      rootNode.value should be(1)
      rootNode.connections(0).value should be(2)
    }
    "connect node to existing one" in {
      val root = Node(1, Node(2, Node(4), Node(5, Node(6))), Node(3))
      val graph = new DirectedDependencyGraph[Int](root)
      val newGraph = graph.addEdge(7, 5)
      val newRoot = newGraph.rootNodes(1)
      newRoot should be(Node(7, Node(5, Node(6))))
    }
    "connect existing node to new one" in {
      val root = Node(1, Node(2), Node(3, Node(4), Node(5)))
      val graph = new DirectedDependencyGraph[Int](root)
      val newGraph = graph.addEdge(4, 7)
      val newRoot = newGraph.rootNodes(0)
      newRoot should be(Node(1, Node(2), Node(3, Node(4, Node(7)), Node(5))))
    }
    "connect existing node to existing node in connected graph" in {
      val root = Node(1, Node(2), Node(3, Node(4), Node(5)))
      val graph = new DirectedDependencyGraph[Int](root)
      val newGraph = graph.addEdge(1, 3)
      val newRoot = newGraph.rootNodes(0)
      newRoot should be(Node(1, Node(2), Node(3, Node(4), Node(5)), Node(3, Node(4), Node(5))))
    }
    "connect existing node to existing node in disconnected graphs" in {
      val root1 = Node(1, Node(2), Node(3, Node(4), Node(5)))
      val root2 = Node(6, Node(7), Node(8))
      val graph = new DirectedDependencyGraph[Int](Array(root1, root2): _*)
      val newGraph = graph.addEdge(6, 3)
      val newRoot = newGraph.rootNodes(1)
      newRoot should be(Node(6, Node(7), Node(8), Node(3, Node(4), Node(5))))
    }
    "detect cyclic dependency 1" in {
      val graph = new DirectedDependencyGraph[Int]
      intercept[GraphException] {
        val newGraph = graph.addEdge(1, 2)
        newGraph.addEdge(2, 1)
      }.getMessage should be (s"Cyclic dependency found: 2 -> 1 -> 2")
    }
    "detect cyclic dependency 2" in {
      val root = Node(1, Node(2), Node(3, Node(4), Node(5)))
      val graph = new DirectedDependencyGraph(root)
      intercept[GraphException] {
        graph.addEdge(4, 1)
      }.getMessage should be (s"Cyclic dependency found: 4 -> 1 -> 3 -> 4")
    }
    "detect cyclic dependency 3" in {
      val root1 = Node(1, Node(2), Node(3, Node(4), Node(5)))
      val root2 = Node(6, Node(7), Node(8, Node(9)))
      val graph = new DirectedDependencyGraph[Int](Array(root1, root2): _*)
      intercept[GraphException] {
        val newGraph = graph.addEdge(5, 6)
        newGraph.addEdge(9, 1)
      }.getMessage should be (s"Cyclic dependency found: 9 -> 1 -> 3 -> 5 -> 6 -> 8 -> 9")
    }
    "detect cyclic dependency 4" in {
      val graph = new DirectedDependencyGraph[Int]
      intercept[GraphException] {
        val newGraph = graph.addEdge(1, 1)
      }.getMessage should be (s"Cyclic dependency found: 1 -> 1")
    }
  }
}
