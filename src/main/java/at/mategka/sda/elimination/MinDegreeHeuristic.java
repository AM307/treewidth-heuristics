package at.mategka.sda.elimination;

import at.mategka.sda.GraphExtensions;
import at.mategka.sda.elimination.result.EliminationResult;
import at.mategka.sda.elimination.result.EmptyResult;
import at.mategka.sda.elimination.result.FinalAppendResult;
import at.mategka.sda.elimination.result.VertexAppendResult;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.FibonacciHeap;

import java.util.HashSet;
import java.util.Set;

public class MinDegreeHeuristic<V> implements EliminationHeuristic<V> {

    private final AddressableHeap<Integer, V> heap = new FibonacciHeap<>();
    private final Set<V> outdated = new HashSet<>();

    public MinDegreeHeuristic(SimpleGraph<V, ?> graph) {
        graph.vertexSet().forEach(v -> heap.insert(graph.degreeOf(v), v));
    }

    @Override
    public EliminationResult<V> next(SimpleGraph<V, ?> graph) {
        if (graph.vertexSet().isEmpty()) {
            return new EmptyResult<>();
        }
        while (!heap.isEmpty()) {
            var next = heap.deleteMin();
            var v = next.getValue();
            if (outdated.remove(v)) {
                heap.insert(graph.degreeOf(v), v);
                continue;
            }
            var d = next.getKey();
            if (d == graph.vertexSet().size() - 1 && !heap.isEmpty()) {
                heap.clear();
                return new FinalAppendResult<>(graph.vertexSet());
            }
            return new VertexAppendResult<>(v, d);
        }
        return new EmptyResult<>();
    }

    @Override
    public void eliminate(SimpleGraph<V, ?> graph, V vertex) {
        var neighbors = Graphs.neighborListOf(graph, vertex);
        outdated.addAll(neighbors);
        GraphExtensions.eliminateVertex(graph, vertex, neighbors);
    }

}
