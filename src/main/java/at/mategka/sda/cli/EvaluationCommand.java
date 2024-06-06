package at.mategka.sda.cli;

import at.mategka.sda.elimination.EliminationHeuristic;
import at.mategka.sda.elimination.MaxCardinalityHeuristic;
import at.mategka.sda.elimination.MinDegreeHeuristic;
import at.mategka.sda.elimination.MinFillHeuristic;
import at.mategka.sda.io.CsvGraphParser;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Pattern;

@CommandLine.Command(
        name = "eval",
        description = "Evaluates all three treewidth heuristics on a given set of graphs.",
        mixinStandardHelpOptions = true
)
public class EvaluationCommand implements Callable<Integer> {

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
        var valuesMind = new ArrayList<Integer>();
        var valuesMinf = new ArrayList<Integer>();
        var valuesMaxc = new ArrayList<Integer>();

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
            var mind = minDegree.treewidth(graph);
            valuesMind.add(mind);
            System.out.printf("%d,", mind);
            var minFill = new MinFillHeuristic<>(graph);
            var minf = minFill.treewidth(graph);
            valuesMinf.add(minf);
            System.out.printf("%d,", minf);
            var maxCardinality = new MaxCardinalityHeuristic<>(graph);
            var maxc = maxCardinality.treewidth(graph);
            valuesMaxc.add(maxc);
            System.out.println(maxc);
        }
        var meanMind = valuesMind.stream().mapToInt(i -> i).average().orElseThrow();
        var meanMinf = valuesMinf.stream().mapToInt(i -> i).average().orElseThrow();
        var meanMaxc = valuesMaxc.stream().mapToInt(i -> i).average().orElseThrow();
        System.out.printf("-1,-1,-1,mean,%.3f,%.3f,%.3f%n", meanMind, meanMinf, meanMaxc);

        valuesMind.sort(Integer::compareTo);
        valuesMinf.sort(Integer::compareTo);
        valuesMaxc.sort(Integer::compareTo);
        var half = relevantFileList.size() / 2;
        var medianMind = (double) valuesMind.get(half);
        var medianMinf = (double) valuesMinf.get(half);
        var medianMaxc = (double) valuesMaxc.get(half);
        if (relevantFileList.size() % 2 == 0) {
            medianMind = (medianMind + valuesMind.get(half + 1)) / 2;
            medianMinf = (medianMinf + valuesMinf.get(half + 1)) / 2;
            medianMaxc = (medianMaxc + valuesMaxc.get(half + 1)) / 2;
        }
        System.out.printf("-1,-1,-1,median,%.3f,%.3f,%.3f%n", medianMind, medianMinf, medianMaxc);
        return 0;
    }

}
