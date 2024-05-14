package at.mategka.sda.elimination;

import at.mategka.sda.GraphExtensions;
import at.mategka.sda.elimination.result.EliminationResult;
import at.mategka.sda.elimination.result.VertexResult;
import org.jgrapht.graph.SimpleGraph;

public interface EliminationHeuristic<V> {

    EliminationResult<V> next(SimpleGraph<V, ?> graph);

    default void eliminate(SimpleGraph<V, ?> graph, V vertex) {
        GraphExtensions.eliminateVertex(graph, vertex);
    }

    default int treewidth(SimpleGraph<V, ?> graph) {
        var remainingGraph = GraphExtensions.shallowCopy(graph);
        int result = 0;
        while (true) {
            var nextResult = next(remainingGraph);
            if (!(nextResult instanceof VertexResult<V> vertexResult)) {
                break;
            }
            result = Math.max(result, vertexResult.degree());
            eliminate(remainingGraph, vertexResult.vertex());
        }
        return result;
    }

}
