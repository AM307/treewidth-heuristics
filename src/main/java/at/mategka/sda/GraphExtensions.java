package at.mategka.sda;

import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.TypeUtil;

import java.util.List;

public final class GraphExtensions {

    private GraphExtensions() {}

    public static <V, E, T extends AbstractBaseGraph<? extends V, ? extends E>> T shallowCopy(T graph) {
        return TypeUtil.uncheckedCast(graph.clone());
    }

    public static <V> void eliminateVertex(SimpleGraph<V, ?> graph, V vertex) {
        eliminateVertex(graph, vertex, Graphs.neighborListOf(graph, vertex));
    }

    public static <V> void eliminateVertex(SimpleGraph<V, ?> graph, V vertex, List<V> neighbors) {
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
