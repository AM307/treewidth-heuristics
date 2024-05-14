package at.mategka.sda.elimination;

import at.mategka.sda.elimination.result.EliminationResult;
import at.mategka.sda.elimination.result.EmptyResult;
import at.mategka.sda.elimination.result.FinalAppendResult;
import at.mategka.sda.elimination.result.VertexAppendResult;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.FibonacciHeap;

import java.util.function.Predicate;

public class MinFillHeuristic<V> implements EliminationHeuristic<V> {

    private final AddressableHeap<Integer, V> heap = new FibonacciHeap<>();

    public MinFillHeuristic(final SimpleGraph<V, ?> graph) {
        graph.vertexSet().forEach(v -> heap.insert(graph.degreeOf(v), v));
    }

    @Override
    public EliminationResult<V> next(final SimpleGraph<V, ?> graph) {
        if (graph.vertexSet().isEmpty()) {
            return new EmptyResult<>();
        }

        var first = heap.deleteMin();
        var firstVertex = first.getValue();
        var firstDegree = first.getKey();
        if (firstDegree == graph.vertexSet().size() - 1 || heap.isEmpty()) {
            return new FinalAppendResult<>(graph.vertexSet());
        }

        long minFillEdges = getFillEdgeCount(graph, firstVertex);
        var minFillVertex = new VertexAppendResult<>(firstVertex, firstDegree);
        if (minFillEdges == 0) {
            return minFillVertex;
        }

        while (!heap.isEmpty()) {
            var next = heap.deleteMin();
            var v = next.getValue();
            var d = next.getKey();
            long fillEdges = getFillEdgeCount(graph, v);
            if (fillEdges < minFillEdges) {
                minFillEdges = fillEdges;
                minFillVertex = new VertexAppendResult<>(v, d);
                if (fillEdges == 0) break;
            }
        }
        return minFillVertex;
    }

    private long getFillEdgeCount(final SimpleGraph<V, ?> graph, V vertex) {
        final var neighbors = Graphs.neighborSetOf(graph, vertex);
        return neighbors.stream()
                .mapToLong(v -> {
                    var vn = Graphs.neighborSetOf(graph, v);
                    return neighbors.stream().filter(Predicate.not(vn::contains)).count() - 1;
                })
                .sum();
    }

}
