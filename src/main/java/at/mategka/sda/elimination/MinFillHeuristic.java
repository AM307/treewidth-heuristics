package at.mategka.sda.elimination;

import at.mategka.sda.GraphExtensions;
import at.mategka.sda.elimination.result.EliminationResult;
import at.mategka.sda.elimination.result.EmptyResult;
import at.mategka.sda.elimination.result.FinalAppendResult;
import at.mategka.sda.elimination.result.VertexAppendResult;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

public class MinFillHeuristic<V> implements EliminationHeuristic<V> {

    private final Map<V, Set<V>> neighborsMap = new HashMap<>();
    private final Map<V, Integer> fillInEdges = new HashMap<>();

    public MinFillHeuristic(final SimpleGraph<V, ?> _graph) {
        // Empty
    }

    @Override
    public EliminationResult<V> next(final SimpleGraph<V, ?> graph) {
        if (graph.vertexSet().isEmpty()) {
            return new EmptyResult<>();
        }

        int minFillEdges = Integer.MAX_VALUE;
        VertexAppendResult<V> minFillVertex = null;

        for (var v : graph.vertexSet()) {
            var n = getNeighbors(graph, v);
            var d = n.size();
            if (d == graph.vertexSet().size() - 1) {
                return new FinalAppendResult<>(new HashSet<>(graph.vertexSet()));
            }
            int fillEdges = getFillEdgeCount(graph, v);
            if (fillEdges < minFillEdges) {
                minFillEdges = fillEdges;
                minFillVertex = new VertexAppendResult<>(v, d);
                if (fillEdges == 0) break;
            }
        }
        return minFillVertex;
    }

    private int getFillEdgeCount(final SimpleGraph<V, ?> graph, V vertex) {
        if (fillInEdges.containsKey(vertex)) {
            return fillInEdges.get(vertex);
        }
        int fillEdges = 0;
        var neighborList = new ArrayList<>(getNeighbors(graph, vertex));
        for (int i = 0; i < neighborList.size(); i++) {
            for (int j = i + 1; j < neighborList.size(); j++) {
                V ni = neighborList.get(i);
                V nj = neighborList.get(j);
                if (!getNeighbors(graph, ni).contains(nj)) {
                    fillEdges++;
                }
            }
        }
        fillInEdges.put(vertex, fillEdges);
        return fillEdges;
    }

    private Set<V> getNeighbors(final SimpleGraph<V, ?> graph, V vertex) {
        return neighborsMap.computeIfAbsent(vertex, w -> Graphs.neighborSetOf(graph, w));
    }

    @Override
    public void eliminate(SimpleGraph<V, ?> graph, V vertex) {
        var neighborsSet = neighborsMap.get(vertex);
        var neighborsList = new ArrayList<>(neighborsSet);
        neighborsList
                .forEach(v -> {
                    fillInEdges.remove(v);
                    var n = neighborsMap.get(v);
                    n.remove(vertex);
                    n.addAll(neighborsList);
                    n.remove(v);
                    n.forEach(fillInEdges::remove);
                });
        neighborsMap.remove(vertex);
        GraphExtensions.eliminateVertex(graph, vertex, neighborsList);
    }
}
