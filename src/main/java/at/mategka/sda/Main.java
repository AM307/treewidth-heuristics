package at.mategka.sda;

import at.mategka.sda.cli.*;
import picocli.CommandLine;

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

    public static void main(String[] args) {
        var cmd = new CommandLine(new Main());
        if (args.length == 0) {
            cmd.usage(System.out);
            System.exit(1);
        } else {
            int exitCode = cmd.execute(args);
            System.exit(exitCode);
        }
    }

    @Override
    public Integer call() {
        return 0;
    }

}
