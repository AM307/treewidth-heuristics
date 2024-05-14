package at.mategka.sda.heuristics;

import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.FibonacciHeap;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MaxCardinalityHeuristic implements EliminationHeuristic {

    @Override
    public <V> List<V> compute(SimpleGraph<V, ?> graph) {
        if (graph.vertexSet().isEmpty()) {
            return List.of();
        }
        Deque<V> result = new ArrayDeque<>();

        V first = graph.vertexSet().stream().findAny().orElseThrow();
        if (graph.vertexSet().size() == 1) {
            graph.removeVertex(first);
            return List.of(first);
        }
        result.add(first);
        Map<V, Integer> originalDegrees = graph.vertexSet().stream()
                .collect(Collectors.toMap(Function.identity(), graph::degreeOf, (a, b) -> a, HashMap::new));
        graph.removeVertex(first);
        originalDegrees.remove(first);

        AddressableHeap<Integer, V> heap = new FibonacciHeap<>();
        graph.vertexSet().forEach(v -> heap.insert(graph.degreeOf(v) - originalDegrees.get(v), v));
        Set<V> outdated = new HashSet<>();
        while (!heap.isEmpty()) {
            var next = heap.deleteMin();
            var v = next.getValue();
            if (heap.isEmpty()) {
                result.addFirst(v);
                graph.removeVertex(v);
                break;
            }
            if (outdated.remove(v)) {
                heap.insert(graph.degreeOf(v) - originalDegrees.get(v), v);
                continue;
            }
            result.addFirst(v);
            outdated.addAll(Graphs.neighborListOf(graph, v));
            graph.removeVertex(v);
            originalDegrees.remove(v);
        }
        return result.stream().toList();
    }

}
