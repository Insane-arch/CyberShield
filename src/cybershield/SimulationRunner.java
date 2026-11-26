package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimulationRunner {

    private static int parseNumber(
            String text, String name, int minimum, int maximum) {

        final int value;

        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    name + " must be a whole number.", exception);
        }

        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between "
                    + minimum + " and " + maximum + ".");
        }

        return value;
    }

    private static int parseScenario(String defence) {
        switch (defence) {
            case "none":
                return NetworkFactory.NO_DEFENCE;
            case "isolate":
                return NetworkFactory.ISOLATION;
            case "protect":
                return NetworkFactory.PROTECTION;
            default:
                throw new IllegalArgumentException(
                        "Defence must be none, isolate or protect.");
        }
    }

    private static Network createNetwork(
            String topology, int size, int scenario) {

        switch (topology) {
            case "chain":
                return NetworkFactory.createChainScenario(size, scenario);
            case "ring":
                return NetworkFactory.createRingScenario(size, scenario);
            case "star":
                return NetworkFactory.createStarScenario(size, scenario);
            default:
                throw new IllegalArgumentException(
                        "Topology must be chain, ring or star.");
        }
    }

    private static void verifyStates(
            Network sequential, Network parallel, int step) {

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = parallel.getComputer(id);

            if (expected.getId() != actual.getId()
                    || expected.isCompromised() != actual.isCompromised()
                    || expected.isIsolated() != actual.isIsolated()
                    || expected.isProtected() != actual.isProtected()) {
                throw new AssertionError(
                        "State mismatch at step " + step
                        + ", Computer " + id);
            }
        }
    }

    private static SimulationHistory.HistoryEntry createEntry(
            int step, int newlyCompromised,
            int total, int size, int previousTotal) {

        if (newlyCompromised < 0
                || total < 0
                || total > size
                || (step == 0 && newlyCompromised != 0)
                || (step > 0 && total - previousTotal != newlyCompromised)) {
            throw new AssertionError(
                    "Invalid history counts at step " + step);
        }

        return new SimulationHistory.HistoryEntry(
                step, newlyCompromised, total, size - total);
    }

    private static void printUsage() {
        System.out.println(
                "Usage: java -cp out cybershield.SimulationRunner"
                + " <topology> <computers> <steps> <defence> <workers>"
                + " [output-directory]");
        System.out.println("Topology: chain | ring | star");
        System.out.println("Defence: none | isolate | protect");
        System.out.println(
                "Example: java -cp out cybershield.SimulationRunner"
                + " ring 7 6 protect 3");
    }

    public static void main(String[] args)
            throws InterruptedException, IOException {

        if (args.length == 1 && args[0].equals("--help")) {
            printUsage();
            return;
        }

        if (args.length < 5 || args.length > 6) {
            printUsage();
            throw new IllegalArgumentException(
                    "Provide five configuration arguments"
                    + " and an optional output directory.");
        }

        String topology = args[0].toLowerCase(Locale.ROOT);
        int size = parseNumber(args[1], "Computers", 3, 1000);
        int steps = parseNumber(args[2], "Steps", 0, 1000);
        String defence = args[3].toLowerCase(Locale.ROOT);
        int scenario = parseScenario(defence);
        int workers = parseNumber(
                args[4], "Workers", 1, Math.min(size, 32));

        String runName = topology + "-n" + size
                + "-s" + steps + "-" + defence + "-w" + workers;

        Path outputDirectory = args.length == 6
                ? Path.of(args[5])
                : Path.of("out", "history", runName);

        Network sequentialNetwork =
                createNetwork(topology, size, scenario);
        Network parallelNetwork =
                createNetwork(topology, size, scenario);

        SequentialSimulator sequential =
                new SequentialSimulator(sequentialNetwork);
        DataParallelSimulator parallel =
                new DataParallelSimulator(parallelNetwork, workers);

        List<SimulationHistory.HistoryEntry> sequentialHistory =
                new ArrayList<>();
        List<SimulationHistory.HistoryEntry> parallelHistory =
                new ArrayList<>();

        int previousSequential = sequential.getCompromisedCount();
        int previousParallel = parallel.getCompromisedCount();

        for (int step = 0; step <= steps; step++) {
            int sequentialNew = step == 0 ? 0 : sequential.runStep();
            int parallelNew = step == 0 ? 0 : parallel.runStep();

            int sequentialTotal = sequential.getCompromisedCount();
            int parallelTotal = parallel.getCompromisedCount();

            SimulationHistory.HistoryEntry sequentialEntry =
                    createEntry(step, sequentialNew, sequentialTotal,
                            size, previousSequential);

            SimulationHistory.HistoryEntry parallelEntry =
                    createEntry(step, parallelNew, parallelTotal,
                            size, previousParallel);

            verifyStates(sequentialNetwork, parallelNetwork, step);

            if (!sequentialEntry.equals(parallelEntry)) {
                throw new AssertionError(
                        "History mismatch at step " + step);
            }

            sequentialHistory.add(sequentialEntry);
            parallelHistory.add(parallelEntry);

            previousSequential = sequentialTotal;
            previousParallel = parallelTotal;
        }

        System.out.println("CyberShield - Configurable Simulation");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nTopology: " + topology);
        System.out.println("Computers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Defence: " + defence);
        System.out.println("Workers: " + workers);
        System.out.println("Initially compromised: Computer 1");

        if (scenario == NetworkFactory.ISOLATION) {
            System.out.println("Isolation target: Computer 1");
        } else if (scenario == NetworkFactory.PROTECTION) {
            System.out.println(
                    "Protection target: Computer " + (size / 2 + 1));
        }

        System.out.printf(
                "%n%6s %10s %14s %10s%n",
                "Step", "New", "Compromised", "Healthy");

        for (SimulationHistory.HistoryEntry entry : sequentialHistory) {
            System.out.printf(
                    "%6d %10d %14d %10d%n",
                    entry.step(), entry.newlyCompromised(),
                    entry.totalCompromised(), entry.healthy());
        }

        System.out.println(
                "\nAll sequential and parallel states and histories match.");

        Path sequentialCsv =
                outputDirectory.resolve("sequential-history.csv");
        Path parallelCsv =
                outputDirectory.resolve("parallel-history.csv");

        HistoryCsvExporter.write(sequentialCsv, sequentialHistory);
        HistoryCsvExporter.write(parallelCsv, parallelHistory);

        System.out.println("\nCSV files exported:");
        System.out.println(sequentialCsv.toAbsolutePath().normalize());
        System.out.println(parallelCsv.toAbsolutePath().normalize());
    }
}