package at.mategka.sda.cli;

import at.mategka.sda.io.CsvGraphParser;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "gen",
        description = "Outputs a series of G(n, p) graphs in CSV format to a specified directory.",
        mixinStandardHelpOptions = true
)
public class GenerationCommand implements Callable<Integer> {

    @CommandLine.Parameters(
            index = "0",
            description = "The path to the (empty) output directory to place the generated graphs in."
    )
    private Path outputDirectory;

    @CommandLine.Option(
            names = {"-n"},
            required = true,
            arity = "1..*",
            description = "The options for the number of vertices in the output graph."
    )
    private int[] n;

    @CommandLine.Option(
            names = {"-p"},
            required = true,
            arity = "1..*",
            description = "The options for the probability of an edge being included in the output graph."
    )
    private double[] p;

    @CommandLine.Option(
            names = {"-c", "--count"},
            required = true,
            description = "The number of graphs to generate per combination."
    )
    private int c;

    @CommandLine.Option(
            names = {"--start"},
            description = "The graph number to start with (great for resuming).",
            defaultValue = "1"
    )
    private int start;

    @Override
    public Integer call() throws IOException {
        var outputFile = outputDirectory.toFile();
        if (outputFile.exists()) {
            if (!outputFile.isDirectory()) {
                System.err.println(outputDirectory + " is not a directory.");
                return 1;
            }
            var fileList = outputFile.list();
            assert fileList != null;
            if (fileList.length > 0) {
                System.err.println(outputDirectory + " is not empty.");
                return 1;
            }
        } else {
            Files.createDirectories(outputDirectory);
        }
        var maxDigitsN = Arrays.stream(n)
                .map(it -> String.valueOf(it).length())
                .max()
                .orElseThrow();
        var digitsC = String.valueOf(start + c - 1).length();
        String filenameFormat = "graph_n%%0%dd_p%%3s_%%0%dd.csv".formatted(maxDigitsN, digitsC);
        var parser = new CsvGraphParser();
        var edgeSupplier = SupplierUtil.DEFAULT_EDGE_SUPPLIER;
        for (int nn : n) {
            for (double pp : p) {
                var pps = String.valueOf(pp);
                pps = pps.substring(2, Math.min(pps.length(), 5));
                pps += "0".repeat(3 - pps.length());
                var generator = new GnpRandomGraphGenerator<String, DefaultEdge>(nn, pp);
                for (var i = start; i < start + c; i++) {
                    var filename = filenameFormat.formatted(nn, pps, i);
                    var filePath = outputDirectory.resolve(filename);
                    var vertexNameSupplier = SupplierUtil.createStringSupplier();
                    var target = new SimpleGraph<>(vertexNameSupplier, edgeSupplier, false);
                    generator.generateGraph(target);
                    var encodedGraph = parser.encode(target);
                    Files.writeString(filePath, encodedGraph, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Created: " + filePath);
                }
            }
        }
        return 0;
    }

}
