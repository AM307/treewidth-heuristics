package at.mategka.sda.cli;

import at.mategka.sda.TreeDecomposition;
import at.mategka.sda.elimination.EliminationHeuristic;
import at.mategka.sda.elimination.MaxCardinalityHeuristic;
import at.mategka.sda.elimination.MinDegreeHeuristic;
import at.mategka.sda.elimination.MinFillHeuristic;
import at.mategka.sda.io.CsvGraphParser;
import org.jgrapht.graph.SimpleGraph;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

@CommandLine.Command(
        name = "validate",
        description = "Validates the correctness of the given heuristic on a given instance set.",
        mixinStandardHelpOptions = true
)
public class ValidationCommand implements Callable<Integer> {

    private static final Pattern N_PATTERN = Pattern.compile("[\\W_]n(\\d+)");
    private static final Pattern P_PATTERN = Pattern.compile("[\\W_]p(\\d+)");

    @CommandLine.Parameters(
            index = "0",
            description = "The path to the directory the (generated) input graphs are in."
    )
    private Path inputDirectory;

    @CommandLine.Option(
            names = {"-p", "--prefix"},
            required = true,
            description = "Which graphs to process (e.g., \"graph_n0010_p250_\").",
            defaultValue = ""
    )
    private String prefix;

    @Override
    public Integer call() throws IOException {
        var inputFile = inputDirectory.toFile();
        if (!inputFile.isDirectory()) {
            System.err.println(inputDirectory + " is not a directory.");
            return 1;
        }
        var fileList = inputFile.list();
        assert fileList != null;
        if (fileList.length == 0) {
            System.err.println(inputDirectory + " is empty.");
            return 1;
        }
        var relevantFileList = Arrays.stream(fileList)
                .filter(f -> f.startsWith(prefix))
                .filter(f -> f.endsWith(".csv"))
                .sorted()
                .toList();
        if (relevantFileList.isEmpty()) {
            System.err.println(inputDirectory + " contains no graphs matching \"%s\".".formatted(prefix));
        }

        System.out.println("n,p,e,i,mind,minf,maxc");
        var parser = new CsvGraphParser();

        for (var fileName : relevantFileList) {
            var graph = parser.parse(Files.readString(inputDirectory.resolve(fileName)));
            var nMatcher = N_PATTERN.matcher(fileName);
            var n = nMatcher.find() ? Integer.parseInt(nMatcher.group(1)) : -1;
            var pMatcher = P_PATTERN.matcher(fileName);
            var p = pMatcher.find() ? Double.parseDouble(pMatcher.group(1)) / 1000 : -1;
            var i = fileName.substring(prefix.length(), fileName.length() - 4);
            var e = graph.edgeSet().size();
            System.out.printf("%d,%.3f,%d,%s,", n, p, e, i);

            var minDegree = new MinDegreeHeuristic<>(graph);
            var mindResult = validate(minDegree, graph);
            System.out.printf("%d,", mindResult);
            var minFill = new MinFillHeuristic<>(graph);
            var minfResult = validate(minFill, graph);
            System.out.printf("%d,", minfResult);
            var maxCardinality = new MaxCardinalityHeuristic<>(graph);
            var maxcResult = validate(maxCardinality, graph);
            System.out.println(maxcResult);
        }
        return 0;
    }

    private <V> int validate(EliminationHeuristic<V> heuristic, SimpleGraph<V, ?> graph) {
        var ordering = heuristic.eliminationOrder(graph);
        var orderingWidth = EliminationHeuristic.treewidth(graph, ordering);
        var decompWidth = getTreeDecompositionWidth(graph, ordering);
        return (orderingWidth == decompWidth) ? 1 : 0;
    }

    private <V> int getTreeDecompositionWidth(SimpleGraph<V, ?> graph, List<V> eliminationOrdering) {
        var decomp = TreeDecomposition.fromOrdering(eliminationOrdering, graph);
        try {
            TreeDecomposition.verify(graph, decomp);
        } catch (IllegalArgumentException _e) {
            return -1;
        }
        return TreeDecomposition.treewidth(decomp);
    }

}
