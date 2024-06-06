package at.mategka.sda.io;

import at.mategka.sda.GraphExtensions;
import at.mategka.sda.util.Vector2;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CsvGraphParser {

    public SimpleGraph<String, DefaultEdge> parse(String source) {
        var graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        source.lines()
                .map(String::strip)
                .filter(Predicate.not(String::isEmpty))
                .map(it -> it.split("[,>]", 3))
                .filter(it -> it.length >= 2)
                .forEach(parts -> {
                    String i = parts[0];
                    String j = parts[1];
                    Graphs.addEdgeWithVertices(graph, i, j);
                });
        return graph;
    }

    public <V, E> String encode(SimpleGraph<V, E> graph) {
        var edgeVectorSet = GraphExtensions.edgeVectorSet(graph);
        var connectedVertices = edgeVectorSet.stream()
                .flatMap(Vector2::stream)
                .collect(Collectors.toUnmodifiableSet());
        String edges = edgeVectorSet.stream()
                .map(Vector2.formatted("%s,%s,\n"))
                .collect(Collectors.joining());
        String isolatedVertices = graph.vertexSet().stream()
                .filter(Predicate.not(connectedVertices::contains))
                .map("%s,,\n"::formatted)
                .collect(Collectors.joining());
        return edges + isolatedVertices;
    }

}
