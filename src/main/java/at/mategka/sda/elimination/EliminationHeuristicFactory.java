package at.mategka.sda.elimination;

import org.jgrapht.graph.SimpleGraph;

import java.util.List;

@FunctionalInterface
public interface EliminationHeuristicFactory<V> {

    EliminationHeuristic<V> newInstance(SimpleGraph<V, ?> graph);

    default List<V> eliminationOrder(SimpleGraph<V, ?> graph) {
        return newInstance(graph).eliminationOrder(graph);
    }

    default int treewidth(SimpleGraph<V, ?> graph) {
        return newInstance(graph).treewidth(graph);
    }

}
