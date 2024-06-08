# treewidth-heuristics

This Markdown file contains usage and build information. For evaluation results, including the required plots, see [EVALUATION.md](EVALUATION.md).

## Usage

### Usage Prerequisites

- Java 17 or higher (tested with Java 17.0.7)

### General usage

```sh
java -jar treewidth.jar --help
```

### 1. Generating G(n,p) Graphs

Generates a series of $G(n,p)$ graphs.

The output graphs will be in the prescribed CSV format and be named `graph_n<n>_p<p>_<i>.csv` where `<n>` is the $n$ parameter value, `<p>` is the $p$ parameter value as a whole number (strictly speaking $\lfloor 1000p \rfloor$, i.e., $0.6789 \to 678$) and `<i>` is the ascending instance number.

```sh
java -jar treewidth.jar gen <outputDir> -n <n> [<n> ...] -p <p> [<p> ...] -c <count> [--help]
```

- `<outputDir>` - A path to a directory where the generated graphs will be stored. Must be either non-existent or empty.
- `<n>` - Values of $n$ to use. Can be repeated to specify multiple values.
- `<p>` - Values of $p$ to use. Can be repeated to specify multiple values. Use the English format, e.g., `0.123`.
- `<count>` - The number of graphs to generate for each $(n, p)$ combination.

Example:

```sh
java -jar treewidth.jar gen graphs -n 10 100 1000 -p 0.25 0.5 0.75 -c 100
```

### 2. Generating Elimination Orderings

Generates an elimination ordering for a given graph.

The input graph must be in the prescribed CSV format. The output ordering is printed to STDOUT as a space-separated list of vertex labels.

```sh
java -jar treewidth.jar order <graphPath> -r <heuristic> [--help]
```

- `<graphPath>` - A path to an input graph.
- `<heuristic>` - The heuristic to use. Must be one of the following:
  - `mindeg` - Minimum-Degree heuristic
  - `minfill` - Minimum-Fill-In-Edges heuristic
  - `maxcard` - Maximum Cardinality Search heuristic

Example:

```sh
java -jar treewidth.jar order graphs/graph_n0100_p750_001.csv -r mindeg
```

### 3. Generating Tree Decompositions

Generates a tree decomposition for a given graph and an elimination ordering of its vertices.

The input graph must be in the prescribed CSV format and the elimination ordering must be a valid ordering for the input graph and in space-separated list format. The output decomposition will be in the prescribed CSV bag-based format and printed to STDOUT.

```sh
java -jar treewidth.jar decomp <graphPath> <orderingPath> [-y] [--help]
```

- `<graphPath>` - A path to an input graph.
- `<orderingPath>` - A path to an input elimination ordering. You can use `-` to read from STDIN instead.
- `-y` - Enable verification. The produced tree decomposition will be tested for validity (tree, vertices covered, edges covered, connected subtrees) and no decomposition will be output if it is not valid (an error message will be printed to STDERR instead).

This command can be chained with [the `order` command](#2-generating-elimination-orderings) if an elimination ordering has not been computed yet:

```sh
java -jar treewidth.jar order <graphPath> -r <heuristic> | java -jar treewidth.jar decomp <graphPath> -
```

Example:

```sh
java -jar treewidth.jar order graphs/graph_n0100_p750_001.csv -r mindeg | java -jar treewidth.jar decomp graphs/graph_n0100_p750_001.csv -
```

### 4. Generating Evaluation Data

Generates a list of treewidths as computed by the three treewidth heuristics (min-degree, min-fill, max-cardinality) for a given set of graphs.

The input graphs must be in the prescribed CSV format and have filenames similar to the one generated by [the `gen` command](#1-generating-gnp-graphs), i.e., `graph_n<n>_p<p>_<i>.csv`. The output consists of a CSV-format treewidth report for each input graph as well as the mean and median treewidth for each heuristic on the given graph set and will be printed to STDOUT.

```sh
java -jar treewidth.jar eval <inputDir> -p <prefix> [--help]
```

- `<inputDir>` - A path to a directory to retrieve the graph instances from.
- `<prefix>` - The prefix for the graph instances to use. Will be stripped from filenames to determine the instance names. Required, so use a string like `"graph_n0100_p750_"`.

Example:

```sh
java -jar treewidth.jar eval graphs -p graph_n0100_p750_ | tee results/graph_n0100_p750.csv
```

## Development

### Build Prerequisites

- Java 17 or higher
- Maven 3.2.5 or higher

Tested with:

```text
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 17.0.7, vendor: Private Build, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "4.4.0-22621-microsoft", arch: "amd64", family: "unix"
```

### Build Instructions

```sh
mvn clean package
```

The resulting JAR can be found as:

```text
target/treewidth-heuristics-1.0-SNAPSHOT.jar
```

You can then proceed to use this JAR as with the pre-packaged version as outlined under [Usage](#usage).
