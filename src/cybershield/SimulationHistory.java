package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimulationHistory {

    public record HistoryEntry(
            int step,
            int newlyCompromised,
            int totalCompromised,
            int healthy) {
    }

    private static void verifyMatchingStates(
            Network sequential, Network parallel, int step) {

        if (sequential.getSize() != parallel.getSize()) {
            throw new AssertionError("Network sizes differ.");
        }

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = parallel.getComputer(id);

            boolean matches =
                    expected.getId() == actual.getId()
                    && expected.isCompromised() == actual.isCompromised()
                    && expected.isIsolated() == actual.isIsolated()
                    && expected.isProtected() == actual.isProtected();

            if (!matches) {
                throw new AssertionError(
                        "State mismatch at step " + step
                        + ", Computer " + id);
            }
        }
    }

    private static void verifyHistory(
            List<HistoryEntry> history, int size, int steps) {

        if (history.size() != steps + 1) {
            throw new AssertionError("History has missing entries.");
        }

        int previousTotal = history.get(0).totalCompromised();

        for (int index = 0; index < history.size(); index++) {
            HistoryEntry entry = history.get(index);

            if (entry.step() != index) {
                throw new AssertionError("History steps are out of order.");
            }

            if (entry.totalCompromised() + entry.healthy() != size) {
                throw new AssertionError(
                        "Population count mismatch at step " + index);
            }

            if (entry.newlyCompromised() < 0
                    || entry.healthy() < 0
                    || entry.totalCompromised() < previousTotal) {
                throw new AssertionError(
                        "Invalid history counts at step " + index);
            }

            if (index > 0
                    && entry.totalCompromised() - previousTotal
                    != entry.newlyCompromised()) {
                throw new AssertionError(
                        "New-compromise count mismatch at step " + index);
            }

            int expectedTotal = Math.min(size, index + 1);
            int expectedNew =
                    index > 0 && index < size ? 1 : 0;

            if (entry.totalCompromised() != expectedTotal
                    || entry.newlyCompromised() != expectedNew) {
                throw new AssertionError(
                        "Unexpected chain history at step " + index);
            }

            previousTotal = entry.totalCompromised();
        }
    }

    public static void main(String[] args)
            throws InterruptedException, IOException {

        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Usage: SimulationHistory [output-directory]");
        }

        Path outputDirectory = args.length == 1
                ? Path.of(args[0])
                : Path.of("out", "history");

        int size = 7;
        int steps = 8;
        int workers = 3;

        Network sequentialNetwork =
                NetworkFactory.createChainScenario(
                        size, NetworkFactory.NO_DEFENCE);

        Network parallelNetwork =
                NetworkFactory.createChainScenario(
                        size, NetworkFactory.NO_DEFENCE);

        SequentialSimulator sequential =
                new SequentialSimulator(sequentialNetwork);

        DataParallelSimulator parallel =
                new DataParallelSimulator(parallelNetwork, workers);

        List<HistoryEntry> sequentialHistory = new ArrayList<>();
        List<HistoryEntry> parallelHistory = new ArrayList<>();

        verifyMatchingStates(sequentialNetwork, parallelNetwork, 0);

        int sequentialInitial = sequential.getCompromisedCount();
        int parallelInitial = parallel.getCompromisedCount();

        sequentialHistory.add(new HistoryEntry(
                0, 0, sequentialInitial, size - sequentialInitial));

        parallelHistory.add(new HistoryEntry(
                0, 0, parallelInitial, size - parallelInitial));

        for (int step = 1; step <= steps; step++) {
            int sequentialNew = sequential.runStep();
            int parallelNew = parallel.runStep();

            int sequentialTotal = sequential.getCompromisedCount();
            int parallelTotal = parallel.getCompromisedCount();

            sequentialHistory.add(new HistoryEntry(
                    step, sequentialNew,
                    sequentialTotal, size - sequentialTotal));

            parallelHistory.add(new HistoryEntry(
                    step, parallelNew,
                    parallelTotal, size - parallelTotal));

            verifyMatchingStates(
                    sequentialNetwork, parallelNetwork, step);

            if (!sequentialHistory.get(step).equals(
                    parallelHistory.get(step))) {
                throw new AssertionError(
                        "History mismatch at step " + step);
            }
        }

        verifyHistory(sequentialHistory, size, steps);
        verifyHistory(parallelHistory, size, steps);

        if (!sequentialHistory.equals(parallelHistory)) {
            throw new AssertionError("Execution histories differ.");
        }

        System.out.println("CyberShield - Simulation History");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nTopology: Chain");
        System.out.println("Scenario: No defence");
        System.out.println("Computers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Parallel workers: " + workers);

        System.out.printf(
                "%n%6s %10s %14s %10s %10s%n",
                "Step", "New", "Compromised", "Healthy", "Match");

        for (HistoryEntry entry : sequentialHistory) {
            System.out.printf(
                    "%6d %10d %14d %10d %10s%n",
                    entry.step(),
                    entry.newlyCompromised(),
                    entry.totalCompromised(),
                    entry.healthy(),
                    "YES");
        }

        System.out.println(
                "\nSequential and parallel histories match at every step.");
        System.out.println("History consistency checks passed.");

        Path sequentialCsv =
                outputDirectory.resolve("sequential-history.csv");

        Path parallelCsv =
                outputDirectory.resolve("parallel-history.csv");

        // Export only after all simulation checks pass.
        HistoryCsvExporter.write(sequentialCsv, sequentialHistory);
        HistoryCsvExporter.write(parallelCsv, parallelHistory);

        System.out.println("\nCSV files exported:");
        System.out.println(sequentialCsv.toAbsolutePath().normalize());
        System.out.println(parallelCsv.toAbsolutePath().normalize());
    }
}