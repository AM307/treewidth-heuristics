package at.mategka.sda.elimination;

import at.mategka.sda.GraphExtensions;
import at.mategka.sda.elimination.result.*;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public interface EliminationHeuristic<V> {

    EliminationResult<V> next(SimpleGraph<V, ?> graph);

    default void eliminate(SimpleGraph<V, ?> graph, V vertex) {
        GraphExtensions.eliminateVertex(graph, vertex);
    }

    default List<V> eliminationOrder(SimpleGraph<V, ?> graph) {
        var remainingGraph = GraphExtensions.shallowCopy(graph);
        Deque<V> result = new ArrayDeque<>();
        while (true) {
            var nextResult = next(remainingGraph);
            if (nextResult instanceof VertexAppendResult<V> vertexAppendResult) {
                result.add(vertexAppendResult.vertex());
                eliminate(remainingGraph, vertexAppendResult.vertex());
            }
            if (nextResult instanceof VertexPrependResult<V> vertexPrependResult) {
                result.addFirst(vertexPrependResult.vertex());
                eliminate(remainingGraph, vertexPrependResult.vertex());
            }
            if (nextResult instanceof FinalAppendResult<V> finalAppendResult) {
                result.addAll(finalAppendResult.vertices());
                break;
            }
            if (nextResult instanceof EmptyResult<V>) {
                break;
            }
        }
        return result.stream().toList();
    }

    default int treewidth(SimpleGraph<V, ?> graph) {
        var remainingGraph = GraphExtensions.shallowCopy(graph);
        int result = 0;
        while (true) {
            var nextResult = next(remainingGraph);
            if (nextResult instanceof FinalAppendResult<V>) {
                result = Math.max(result, remainingGraph.vertexSet().size() - 1);
            }
            if (!(nextResult instanceof VertexResult<V> vertexResult)) {
                break;
            }
            result = Math.max(result, vertexResult.degree());
            eliminate(remainingGraph, vertexResult.vertex());
        }
        return result;
    }

    static <V> int treewidth(SimpleGraph<V, ?> graph, List<V> eliminationOrder) {
        var remainingGraph = GraphExtensions.shallowCopy(graph);
        return eliminationOrder.stream()
                .mapToInt(vertex -> {
                    var neighbors = Graphs.neighborListOf(remainingGraph, vertex);
                    var value = neighbors.size();
                    GraphExtensions.eliminateVertex(remainingGraph, vertex, neighbors);
                    return value;
                })
                .max()
                .orElse(0);
    }

}
