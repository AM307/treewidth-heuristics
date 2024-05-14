package at.mategka.sda.elimination;

import org.jgrapht.graph.SimpleGraph;

@FunctionalInterface
public interface EliminationHeuristicFactory<T extends EliminationHeuristic> {

    T newInstance(SimpleGraph<?, ?> graph);

}
