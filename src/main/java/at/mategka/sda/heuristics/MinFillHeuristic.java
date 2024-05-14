package at.mategka.sda.heuristics;

import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.FibonacciHeap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MinFillHeuristic implements EliminationHeuristic {

    @Override
    public <V> List<V> compute(SimpleGraph<V, ?> graph) {
        if (graph.vertexSet().isEmpty()) {
            return List.of();
        }
        List<V> result = new ArrayList<>();
        outer:
        while (!graph.vertexSet().isEmpty()) {
            AddressableHeap<Integer, V> heap = new FibonacciHeap<>();
            graph.vertexSet().forEach(v -> heap.insert(graph.degreeOf(v), v));
            long minFillEdges = Long.MAX_VALUE;
            V minFillVertex = null;
            while (!heap.isEmpty()) {
                var next = heap.deleteMin();
                var v = next.getValue();
                if (next.getKey() == graph.vertexSet().size() - 1 || heap.isEmpty()) {
                    var remainingVertices = new ArrayList<>(graph.vertexSet());
                    result.addAll(remainingVertices);
                    graph.removeAllVertices(remainingVertices);
                    break outer;
                }
                long fillEdges = getFillEdgeCount(graph, v);
                if (fillEdges < minFillEdges) {
                    minFillEdges = fillEdges;
                    minFillVertex = v;
                    if (fillEdges == 0) break;
                }
            }
            EliminationHeuristic.eliminate(graph, minFillVertex);
            result.add(minFillVertex);
        }
        return result;
    }

    private <V> long getFillEdgeCount(final SimpleGraph<V, ?> graph, V vertex) {
        final var neighbors = Graphs.neighborSetOf(graph, vertex);
        return neighbors.stream()
                .mapToLong(v -> {
                    var vn = Graphs.neighborSetOf(graph, v);
                    return neighbors.stream().filter(Predicate.not(vn::contains)).count() - 1;
                })
                .sum();
    }

}
