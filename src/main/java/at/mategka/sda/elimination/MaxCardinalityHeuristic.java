package at.mategka.sda.elimination;

import at.mategka.sda.elimination.result.EliminationResult;
import at.mategka.sda.elimination.result.EmptyResult;
import at.mategka.sda.elimination.result.VertexPrependResult;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.FibonacciHeap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MaxCardinalityHeuristic<V> implements EliminationHeuristic<V> {

    private final Map<V, Integer> originalDegrees;
    private final AddressableHeap<Integer, V> heap = new FibonacciHeap<>();
    private final Set<V> outdated = new HashSet<>();

    public MaxCardinalityHeuristic(SimpleGraph<V, ?> graph) {
        originalDegrees = graph.vertexSet().stream()
                .collect(Collectors.toMap(Function.identity(), graph::degreeOf, (a, b) -> a, HashMap::new));
        graph.vertexSet().forEach(v -> heap.insert(graph.degreeOf(v) - originalDegrees.get(v), v));
    }

    @Override
    public EliminationResult<V> next(SimpleGraph<V, ?> graph) {
        if (graph.vertexSet().isEmpty()) {
            return new EmptyResult<>();
        }
        while (!heap.isEmpty()) {
            var next = heap.deleteMin();
            var v = next.getValue();
            var f = next.getKey();
            if (outdated.remove(v)) {
                heap.insert(graph.degreeOf(v) - originalDegrees.get(v), v);
                continue;
            }
            return new VertexPrependResult<>(v, f + originalDegrees.get(v));
        }
        return new EmptyResult<>();
    }

    @Override
    public void eliminate(SimpleGraph<V, ?> graph, V vertex) {
        outdated.addAll(Graphs.neighborListOf(graph, vertex));
        originalDegrees.remove(vertex);
        graph.removeVertex(vertex);
    }

    @Override
    public int treewidth(SimpleGraph<V, ?> graph) {
        var ordering = eliminationOrder(graph);
        return EliminationHeuristic.treewidth(graph, ordering);
    }

}
