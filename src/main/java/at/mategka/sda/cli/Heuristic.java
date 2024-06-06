package at.mategka.sda.cli;

import at.mategka.sda.elimination.*;

public enum Heuristic {
    MIN_FILL("minfill", MinFillHeuristic::new),
    MIN_DEGREE("mindeg", MinDegreeHeuristic::new),
    MAX_CARDINALITY("maxcard", MaxCardinalityHeuristic::new),
    ;

    private final String value;
    private final EliminationHeuristicFactory<String> factory;

    Heuristic(String value, EliminationHeuristicFactory<String> factory) {
        this.value = value;
        this.factory = factory;
    }

    public String value() {
        return value;
    }

    public EliminationHeuristicFactory<String> factory() {
        return factory;
    }

    @Override
    public String toString() {
        return value;
    }
}
