package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

import java.util.Locale;

public class PooledSpeedupBenchmark {
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 6;

    private record RunResult(
            long elapsedNanos,
            int newlyCompromised,
            Network network) {
    }

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

    private static RunResult measure(
            boolean pooledMode,
            int size,
            int steps,
            int workers) throws InterruptedException {

        // Build identical input outside the timed region.
        Network network = NetworkFactory.createChainScenario(
                size, NetworkFactory.NO_DEFENCE);

        int newlyCompromised = 0;
        long start = System.nanoTime();

        if (pooledMode) {
            try (PooledDataParallelSimulator simulator =
                    new PooledDataParallelSimulator(network, workers)) {

                for (int step = 0; step < steps; step++) {
                    newlyCompromised += simulator.runStep();
                }
            }
        } else {
            TargetSequentialSimulator simulator =
                    new TargetSequentialSimulator(network);

            for (int step = 0; step < steps; step++) {
                newlyCompromised += simulator.runStep();
            }
        }

        long elapsed = System.nanoTime() - start;

        return new RunResult(elapsed, newlyCompromised, network);
    }

    private static void verify(
            RunResult result, int size, int steps) {

        int expectedTotal = Math.min(size, steps + 1);

        if (result.newlyCompromised() != expectedTotal - 1) {
            throw new AssertionError(
                    "Accumulated new-compromise count is incorrect.");
        }

        int actualTotal = 0;

        for (int id = 1; id <= size; id++) {
            ComputerNode computer = result.network().getComputer(id);
            boolean expectedCompromised = id <= expectedTotal;

            if (computer.getId() != id
                    || computer.isCompromised() != expectedCompromised
                    || computer.isIsolated()
                    || computer.isProtected()) {

                throw new AssertionError(
                        "Incorrect final state for Computer " + id);
            }

            if (computer.isCompromised()) {
                actualTotal++;
            }
        }

        if (actualTotal != expectedTotal) {
            throw new AssertionError("Incorrect final total.");
        }
    }

    private static double averageMillis(long[] times) {
        double total = 0;

        for (long time : times) {
            total += time;
        }

        return total / times.length / 1_000_000.0;
    }

    public static void main(String[] args)
            throws InterruptedException {

        if (args.length != 0 && args.length != 3) {
            throw new IllegalArgumentException(
                    "Usage: PooledSpeedupBenchmark"
                    + " [computers steps workers]");
        }

        int size = 500;
        int steps = 499;
        int workers = 3;

        if (args.length == 3) {
            size = parseNumber(args[0], "Computers", 3, 1000);
            steps = parseNumber(args[1], "Steps", 1, 1000);
            workers = parseNumber(
                    args[2], "Workers", 1, Math.min(size, 16));
        }

        System.out.println("CyberShield - Pooled Speedup Benchmark");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nTopology: Chain");
        System.out.println("Defence: None");
        System.out.println("Computers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Pool workers: " + workers);
        System.out.println("Warm-up runs per mode: " + WARMUP_RUNS);
        System.out.println("Measured runs per mode: " + MEASURED_RUNS);
        System.out.println("Java version: "
                + System.getProperty("java.version"));
        System.out.println("OS: "
                + System.getProperty("os.name") + " "
                + System.getProperty("os.version"));
        System.out.println("Available JVM processors: "
                + Runtime.getRuntime().availableProcessors());

        System.out.println("\nWarming up both modes...");

        for (int run = 0; run < WARMUP_RUNS; run++) {
            boolean pooledFirst = run % 2 == 0;

            RunResult first =
                    measure(pooledFirst, size, steps, workers);
            verify(first, size, steps);

            RunResult second =
                    measure(!pooledFirst, size, steps, workers);
            verify(second, size, steps);
        }

        long[] sequentialTimes = new long[MEASURED_RUNS];
        long[] pooledTimes = new long[MEASURED_RUNS];

        System.out.printf(
                "%n%4s %-12s %15s %15s%n",
                "Run", "First mode", "Sequential ms", "Pooled ms");

        for (int run = 0; run < MEASURED_RUNS; run++) {
            boolean pooledFirst = run % 2 == 0;

            RunResult first =
                    measure(pooledFirst, size, steps, workers);
            verify(first, size, steps);

            RunResult second =
                    measure(!pooledFirst, size, steps, workers);
            verify(second, size, steps);

            RunResult sequential = pooledFirst ? second : first;
            RunResult pooled = pooledFirst ? first : second;

            sequentialTimes[run] = sequential.elapsedNanos();
            pooledTimes[run] = pooled.elapsedNanos();

            System.out.printf(
                    Locale.ROOT,
                    "%4d %-12s %15.3f %15.3f%n",
                    run + 1,
                    pooledFirst ? "Pooled" : "Sequential",
                    sequentialTimes[run] / 1_000_000.0,
                    pooledTimes[run] / 1_000_000.0);
        }

        double sequentialAverage = averageMillis(sequentialTimes);
        double pooledAverage = averageMillis(pooledTimes);

        System.out.println(
                "\nAll warm-up and measured results passed verification.");

        System.out.printf(
                Locale.ROOT,
                "Average sequential time: %.3f ms%n", sequentialAverage);
        System.out.printf(
                Locale.ROOT,
                "Average pooled time:     %.3f ms%n", pooledAverage);

        if (pooledAverage > 0) {
            double speedup = sequentialAverage / pooledAverage;
            double efficiency = speedup / workers * 100.0;

            System.out.printf(
                    Locale.ROOT, "Speedup: %.2fx%n", speedup);
            System.out.printf(
                    Locale.ROOT, "Efficiency: %.2f%%%n", efficiency);
        }

        System.out.println(
                "\nTiming includes simulator setup, execution and cleanup.");
        System.out.println(
                "Network construction, verification and printing are excluded.");
        System.out.println(
                "Efficiency is speedup divided by workers, not CPU utilization.");
    }
}