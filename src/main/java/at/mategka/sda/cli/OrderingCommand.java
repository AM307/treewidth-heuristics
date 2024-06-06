package at.mategka.sda.cli;

import at.mategka.sda.io.CsvGraphParser;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "order",
        description = "Outputs a heuristically-generated elimination ordering to STDOUT.",
        mixinStandardHelpOptions = true
)
public class OrderingCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The path to the input graph in CSV format.")
    private Path graphPath;

    @CommandLine.Option(names = {"-r", "--heuristic"}, required = true, description = "The heuristic to use. [${COMPLETION-CANDIDATES}]")
    private Heuristic heuristic;

    @Override
    public Integer call() throws IOException {
        if (!graphPath.toFile().isFile()) {
            System.err.println(graphPath + " is not a file.");
            return 1;
        }
        var contents = Files.readString(graphPath);
        var parser = new CsvGraphParser();
        var graph = parser.parse(contents);
        var eliminationOrdering = heuristic.factory().eliminationOrder(graph);
        System.out.println(String.join(" ", eliminationOrdering));
        return 0;
    }
}
