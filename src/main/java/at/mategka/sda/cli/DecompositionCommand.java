package at.mategka.sda.cli;

import at.mategka.sda.TreeDecomposition;
import at.mategka.sda.io.CsvGraphParser;
import at.mategka.sda.io.CsvTreeDecompositionEncoder;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "decomp",
        description = "Outputs a tree decomposition using a specified elimination ordering to STDOUT.",
        mixinStandardHelpOptions = true
)
public class DecompositionCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The path to the input graph in CSV format.")
    private Path graphPath;

    @CommandLine.Parameters(
            index = "1",
            description = "The path to the input elimination ordering in space-separated list format or - to use STDIN."
    )
    private String orderingPath;

    @CommandLine.Option(
            names = {"-y", "--verify"},
            description = "Whether to verify that the generated graph is a valid tree decomposition of the input graph."
    )
    private boolean verify;

    @Override
    public Integer call() throws IOException {
        if (!graphPath.toFile().isFile()) {
            System.err.println(graphPath + " is not a file.");
            return 1;
        }

        String orderingContents;
        if ("-".equals(orderingPath)) {
            StringBuilder input = new StringBuilder();
            try (InputStreamReader isr = new InputStreamReader(System.in);
                 BufferedReader reader = new BufferedReader(isr)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    input.append(line).append(System.lineSeparator());
                }
            }
            orderingContents = input.toString();
        } else {
            Path actualOrderingPath = Path.of(orderingPath);
            if (!actualOrderingPath.toFile().isFile()) {
                System.err.println(orderingPath + " is not a file.");
                return 1;
            }
            orderingContents = Files.readString(actualOrderingPath);
        }
        var ordering = Arrays.asList(orderingContents.strip().split("\\s+"));

        var graphContents = Files.readString(graphPath);
        var parser = new CsvGraphParser();
        var graph = parser.parse(graphContents);

        var decomposition = TreeDecomposition.fromOrdering(ordering, graph);
        if (verify) {
            try {
                TreeDecomposition.verify(graph, decomposition);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                return 1;
            }
        }
        var encoder = new CsvTreeDecompositionEncoder();
        System.out.println(encoder.encode(decomposition));
        return 0;
    }

}
