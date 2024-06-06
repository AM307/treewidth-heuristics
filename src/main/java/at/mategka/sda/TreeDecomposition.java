package at.mategka.sda;

import at.mategka.sda.elimination.EliminationHeuristic;
import at.mategka.sda.elimination.result.EmptyResult;
import at.mategka.sda.elimination.result.FinalAppendResult;
import at.mategka.sda.elimination.result.VertexAppendResult;
import at.mategka.sda.elimination.result.VertexPrependResult;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.function.Supplier;

public final class TreeDecomposition {

    private TreeDecomposition() {}

    public static <V> SimpleGraph<Bag<V>, ?> fromHeuristic(EliminationHeuristic<V> heuristic, SimpleGraph<V, ?> graph) {
        var remainingGraph = GraphExtensions.shallowCopy(graph);
        Deque<V> vertexStack = new ArrayDeque<>();
        Deque<List<V>> neighborsStack = new ArrayDeque<>();
        while (true) {
            var nextResult = heuristic.next(remainingGraph);
            if (nextResult instanceof VertexAppendResult<V> vertexAppendResult) {
                var vertex = vertexAppendResult.vertex();
                vertexStack.addLast(vertex);
                neighborsStack.addLast(Graphs.neighborListOf(remainingGraph, vertex));
                heuristic.eliminate(remainingGraph, vertex);
            }
            if (nextResult instanceof VertexPrependResult<V> vertexPrependResult) {
                var vertex = vertexPrependResult.vertex();
                vertexStack.addFirst(vertex);
                neighborsStack.addFirst(Graphs.neighborListOf(remainingGraph, vertex));
                heuristic.eliminate(remainingGraph, vertex);
            }
            if (nextResult instanceof FinalAppendResult<V> finalAppendResult) {
                for (var vertex : finalAppendResult.vertices()) {
                    vertexStack.addLast(vertex);
                    neighborsStack.addLast(Graphs.neighborListOf(remainingGraph, vertex));
                    heuristic.eliminate(remainingGraph, vertex);
                }
                break;
            }
            if (nextResult instanceof EmptyResult<V>) {
                break;
            }
        }
        return compute(vertexStack, neighborsStack);
    }

    public static <V> SimpleGraph<Bag<V>, ?> fromOrdering(List<V> eliminationOrdering, SimpleGraph<V, ?> graph) {
        var remainingGraph = GraphExtensions.shallowCopy(graph);
        Deque<V> vertexStack = new ArrayDeque<>();
        Deque<List<V>> neighborsStack = new ArrayDeque<>();
        for (V vertex : eliminationOrdering) {
            vertexStack.addLast(vertex);
            var neighbors = Graphs.neighborListOf(remainingGraph, vertex);
            neighborsStack.addLast(neighbors);
            GraphExtensions.eliminateVertex(remainingGraph, vertex, neighbors);
        }
        return compute(vertexStack, neighborsStack);
    }

    private static <V> SimpleGraph<Bag<V>, ?> compute(Deque<V> vertexStack, Deque<List<V>> neighborsStack) {
        Supplier<String> nameSupplier = SupplierUtil.createStringSupplier();
        var decomposition = new SimpleGraph<Bag<V>, DefaultEdge>(DefaultEdge.class);
        Bag<V> rootBag = null;

        while (!vertexStack.isEmpty()) {
            var vertex = vertexStack.removeLast();
            var neighbors = neighborsStack.removeLast();
            var oldBag = decomposition.vertexSet().stream()
                    .filter(bag -> bag.containsAll(neighbors))
                    .findFirst()
                    .orElse(rootBag);
            Bag<V> newBag = new Bag<>("N" + nameSupplier.get(), neighbors);
            newBag.add(vertex);
            decomposition.addVertex(newBag);
            if (rootBag == null) {
                rootBag = newBag;
            } else {
                decomposition.addEdge(oldBag, newBag);
            }
        }
        return decomposition;
    }

    public static <V> int treewidth(SimpleGraph<Bag<V>, ?> decomposition) {
        return decomposition.vertexSet().stream()
                .mapToInt(Bag::size)
                .max()
                .orElse(1)
                - 1;
    }

    public static <V, E> void verify(SimpleGraph<V, E> graph, SimpleGraph<Bag<V>, ?> decomposition) {
        assertTree(decomposition);
        assertVerticesCovered(graph, decomposition);
        assertEdgesCovered(graph, decomposition);
        assertConnectedSubtrees(graph, decomposition);
    }

    private static <V, E> void assertConnectedSubtrees(SimpleGraph<V, E> graph, SimpleGraph<Bag<V>, ?> decomposition) {
        for (V vertex : graph.vertexSet()) {
            Set<Bag<V>> bagsContainingVertex = new HashSet<>();
            for (Bag<V> bag : decomposition.vertexSet()) {
                if (bag.contains(vertex)) {
                    bagsContainingVertex.add(bag);
                }
            }
            var inducedSubgraph = new SimpleGraph<Set<V>, DefaultEdge>(DefaultEdge.class);
            Graphs.addAllVertices(inducedSubgraph, bagsContainingVertex);
            for (Bag<V> bag1 : bagsContainingVertex) {
                for (Bag<V> bag2 : bagsContainingVertex) {
                    if (decomposition.containsEdge(bag1, bag2)) {
                        inducedSubgraph.addEdge(bag1, bag2);
                    }
                }
            }
            var connectivityInspector = new ConnectivityInspector<>(inducedSubgraph);
            if (!connectivityInspector.isConnected()) {
                throw new IllegalArgumentException("Does not satisfy subtree criterion for vertex %s".formatted(vertex));
            }
        }
    }

    private static <V, E> void assertEdgesCovered(SimpleGraph<V, E> graph, SimpleGraph<Bag<V>, ?> decomposition) {
        for (E edge : graph.edgeSet()) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            boolean edgeCovered = false;
            for (Bag<V> bag : decomposition.vertexSet()) {
                if (bag.contains(source) && bag.contains(target)) {
                    edgeCovered = true;
                    break;
                }
            }
            if (!edgeCovered) {
                throw new IllegalArgumentException("Does not cover edge %s-%s".formatted(source, target));
            }
        }
    }

    private static <V> void assertVerticesCovered(SimpleGraph<V, ?> graph, SimpleGraph<Bag<V>, ?> decomposition) {
        Set<V> originalVertices = graph.vertexSet();
        Set<V> coveredVertices = new HashSet<>();
        for (Bag<V> bag : decomposition.vertexSet()) {
            coveredVertices.addAll(bag);
        }
        if (!coveredVertices.containsAll(originalVertices)) {
            throw new IllegalArgumentException("Does not cover all vertices");
        }
    }

    private static <V> void assertTree(SimpleGraph<Bag<V>, ?> decomposition) {
        Set<Bag<V>> visited = new HashSet<>();
        Stack<Bag<V>> stack = new Stack<>();
        Stack<Bag<V>> parentStack = new Stack<>();
        for (Bag<V> vertex : decomposition.vertexSet()) {
            if (!visited.contains(vertex)) {
                stack.push(vertex);
                parentStack.push(null);
                while (!stack.isEmpty()) {
                    Bag<V> current = stack.pop();
                    Bag<V> parent = parentStack.pop();
                    if (!visited.add(current)) {
                        continue;
                    }
                    for (Bag<V> neighbor : Graphs.neighborListOf(decomposition, current)) {
                        if (neighbor.equals(parent)) {
                            continue;
                        }
                        if (visited.contains(neighbor)) {
                            throw new IllegalArgumentException("Not a tree");
                        }
                        stack.push(neighbor);
                        parentStack.push(current);
                    }
                }
            }
        }
    }

}
