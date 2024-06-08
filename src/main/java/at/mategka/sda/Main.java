package at.mategka.sda;

import at.mategka.sda.cli.*;
import at.mategka.sda.elimination.*;
import at.mategka.sda.io.CsvGraphParser;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "treewidth.jar",
        subcommands = {
                CommandLine.HelpCommand.class,
                OrderingCommand.class,
                DecompositionCommand.class,
                GenerationCommand.class,
                EvaluationCommand.class,
                ValidationCommand.class
        },
        mixinStandardHelpOptions = true
)
public class Main implements Callable<Integer> {

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

    public static void main(String[] args) throws IOException {
        var cmd = new CommandLine(new Main());
        if (args.length == 0) {
            cmd.usage(System.out);
            System.exit(1);
        } else {
            int exitCode = cmd.execute(args);
            System.exit(exitCode);
        }
        var graph = new CsvGraphParser().parse(Files.readString(Paths.get("graphs/graph_n0100_p750_002.csv")));
        var result = new MinDegreeHeuristic<>(graph).eliminationOrder(graph);
        System.out.println(EliminationHeuristic.treewidth(graph, result));
        var decomp = TreeDecomposition.fromHeuristic(new MinDegreeHeuristic<>(graph), graph);
        TreeDecomposition.verify(graph, decomp);
        System.out.println(TreeDecomposition.treewidth(decomp));
        GnpRandomGraphGenerator<String, DefaultEdge> generator = new GnpRandomGraphGenerator<>(10, 0.5);
        SimpleGraph<String, DefaultEdge> g = new SimpleGraph<>(SupplierUtil.createStringSupplier(), SupplierUtil.createSupplier(DefaultEdge.class), false);
        generator.generateGraph(g);
        System.out.println("Done");
        //var result2 = new BruteForceAlgorithm<>(graph).eliminationOrder(graph);
        //System.out.println(EliminationHeuristic.treewidth(graph, result2));
    }

    @Override
    public Integer call() {
        return 0;
    }

}
