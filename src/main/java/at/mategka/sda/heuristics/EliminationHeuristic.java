package at.mategka.sda.heuristics;

import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;

public interface EliminationHeuristic {

    <V> List<V> compute(SimpleGraph<V, ?> graph);

    static <V> void eliminate(SimpleGraph<V, ?> graph, V vertex) {
        var neighbors = Graphs.neighborListOf(graph, vertex);
        graph.removeVertex(vertex);
        for (int i = 0; i < neighbors.size(); i++) {
            var u = neighbors.get(i);
            for (int j = i + 1; j < neighbors.size(); j++) {
                var v = neighbors.get(j);
                if (!graph.containsEdge(u, v)) {
                    graph.addEdge(u, v);
                }
            }
        }
    }

}
