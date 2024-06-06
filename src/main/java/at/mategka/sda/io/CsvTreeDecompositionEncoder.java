package at.mategka.sda.io;

import at.mategka.sda.Bag;
import at.mategka.sda.GraphExtensions;
import at.mategka.sda.util.Vector2;
import org.jgrapht.graph.SimpleGraph;

import java.util.Objects;
import java.util.stream.Collectors;

public class CsvTreeDecompositionEncoder {

    public <V, E> String encode(SimpleGraph<Bag<V>, E> decomposition) {
        String edges = GraphExtensions.edgeVectorSet(decomposition).stream()
                .map(Vector2.mapping(Bag::getName))
                .map(Vector2.formatted("%s,%s,\n"))
                .collect(Collectors.joining());
        String bags = decomposition.vertexSet().stream()
                .map(b -> "%s,,%s\n".formatted(b.getName(), bagToSemicolonList(b)))
                .collect(Collectors.joining());
        return edges + bags;
    }

    private String bagToSemicolonList(Bag<?> bag) {
        return bag.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(";"));
    }

}
