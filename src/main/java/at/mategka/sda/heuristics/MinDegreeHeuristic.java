package at.mategka.sda.heuristics;

import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.FibonacciHeap;

import java.util.*;

public class MinDegreeHeuristic implements EliminationHeuristic {

    @Override
    public <V> List<V> compute(SimpleGraph<V, ?> graph) {
        if (graph.vertexSet().isEmpty()) {
            return List.of();
        }
        AddressableHeap<Integer, V> heap = new FibonacciHeap<>();
        graph.vertexSet().forEach(v -> heap.insert(graph.degreeOf(v), v));
        Set<V> outdated = new HashSet<>();
        List<V> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            var next = heap.deleteMin();
            var v = next.getValue();
            if (heap.isEmpty()) {
                result.add(v);
                graph.removeVertex(v);
                break;
            }
            if (outdated.remove(v)) {
                heap.insert(graph.degreeOf(v), v);
                continue;
            }
            if (next.getKey() == graph.vertexSet().size() - 1) {
                var remainingVertices = new ArrayList<>(graph.vertexSet());
                result.addAll(remainingVertices);
                graph.removeAllVertices(remainingVertices);
                break;
            }
            result.add(v);
            outdated.addAll(Graphs.neighborListOf(graph, v));
            EliminationHeuristic.eliminate(graph, v);
        }
        return result;
    }

}
