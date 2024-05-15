package at.mategka.sda;

import at.mategka.sda.elimination.EliminationHeuristicFactory;
import at.mategka.sda.elimination.MaxCardinalityHeuristic;
import at.mategka.sda.elimination.MinDegreeHeuristic;
import at.mategka.sda.elimination.MinFillHeuristic;
import at.mategka.sda.heuristics.EliminationHeuristic;
import at.mategka.sda.io.CsvGraphParser;
import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class Main {

    private static final String SMALL_GRAPH = """
            1,2
            1,3
            1,4
            2,3
            2,5
            3,4
            3,5
            3,6
            4,6
            5,6
            5,7
            6,7
            6,8
            7,8
            7,9
            8,9
            8,10
            9,10
            """;

    private static final String LARGE_GRAPH = """
            1,2
            1,3
            2,3
            2,4
            3,4
            3,5
            4,5
            4,6
            5,6
            5,7
            6,7
            6,8
            7,8
            7,9
            8,9
            8,10
            9,10
            9,11
            10,11
            10,12
            11,12
            11,13
            12,13
            12,14
            13,14
            13,15
            14,15
            14,16
            15,16
            15,17
            16,17
            16,18
            17,18
            17,19
            18,19
            18,20
            19,20
            19,21
            20,21
            20,22
            21,22
            21,23
            22,23
            22,24
            23,24
            23,25
            24,25
            24,26
            25,26
            25,27
            26,27
            26,28
            27,28
            27,29
            28,29
            28,30
            29,30
            29,31
            30,31
            30,32
            31,32
            31,33
            32,33
            32,34
            33,34
            33,35
            34,35
            34,36
            35,36
            35,37
            36,37
            36,38
            37,38
            37,39
            38,39
            38,40
            39,40
            39,41
            40,41
            40,42
            41,42
            41,43
            42,43
            42,44
            43,44
            43,45
            44,45
            44,46
            45,46
            45,47
            46,47
            46,48
            47,48
            47,49
            48,49
            48,50
            49,50
            """;

    public static void main(String[] args) {
        var graph = new CsvGraphParser().parse(LARGE_GRAPH);
        List<Pair<EliminationHeuristicFactory<String>, EliminationHeuristic>> pairs = List.of(
                Pair.of(MinDegreeHeuristic::new, new at.mategka.sda.heuristics.MinDegreeHeuristic()),
                Pair.of(MinFillHeuristic::new, new at.mategka.sda.heuristics.MinFillHeuristic()),
                Pair.of(MaxCardinalityHeuristic::new, new at.mategka.sda.heuristics.MaxCardinalityHeuristic())
        );
        for (var pair : pairs) {
            var t1 = System.nanoTime();
            var order1 = pair.getFirst().eliminationOrder(graph);
            var t2 = System.nanoTime();
            var order2 = pair.getSecond().compute(GraphExtensions.shallowCopy(graph));
            var t3 = System.nanoTime();
            System.out.println(" Width: " + pair.getFirst().treewidth(graph));
            System.out.println("Order 1: " + order1);
            System.out.println(" Time 1: " + (t2 - t1) / 1000000);
            System.out.println("Order 2: " + order2);
            System.out.println(" Time 2: " + (t3 - t2) / 1000000);
            System.out.println("  Ident: " + IntStream.range(0, graph.vertexSet().size()).allMatch(i -> Objects.equals(order1.get(i), order2.get(i))));
        }
    }
}
