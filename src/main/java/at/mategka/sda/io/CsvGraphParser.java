package at.mategka.sda.io;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.function.Predicate;

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

}
