package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

import java.util.Locale;

public class ThreadReuseBenchmark {
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 6;

    private record RunResult(
            long elapsedNanos,
            int newlyCompromisedTotal,
            Network network) {
    }

    private static int parseNumber(
            String value, String name, int minimum, int maximum) {

        final int parsed;

        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    name + " must be a whole number.", exception);
        }

        if (parsed < minimum || parsed > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between "
                    + minimum + " and " + maximum + ".");
        }

        return parsed;
    }

    private static RunResult measure(
            boolean pooledMode,
            int size,
            int steps,
            int workers) throws InterruptedException {

        // Identical fresh input; network construction is not timed.
        Network network = NetworkFactory.createChainScenario(
                size, NetworkFactory.NO_DEFENCE);

        int newlyCompromisedTotal = 0;

        long start = System.nanoTime();

        if (pooledMode) {
            // Includes executor setup, lazy thread creation and closure.
            try (PooledDataParallelSimulator simulator =
                    new PooledDataParallelSimulator(network, workers)) {

                for (int step = 0; step < steps; step++) {
                    newlyCompromisedTotal += simulator.runStep();
                }
            }
        } else {
            // Includes simulator setup and per-step thread creation/join.
            DataParallelSimulator simulator =
                    new DataParallelSimulator(network, workers);

            for (int step = 0; step < steps; step++) {
                newlyCompromisedTotal += simulator.runStep();
            }
        }

        long elapsed = System.nanoTime() - start;

        return new RunResult(elapsed, newlyCompromisedTotal, network);
    }

    private static Network createBaseline(int size, int steps) {
        Network network = NetworkFactory.createChainScenario(
                size, NetworkFactory.NO_DEFENCE);

        SequentialSimulator simulator = new SequentialSimulator(network);

        for (int step = 0; step < steps; step++) {
            simulator.runStep();
        }

        return network;
    }

    private static void verify(
            RunResult result, Network baseline, int steps) {

        int size = baseline.getSize();
        int expectedTotal = Math.min(size, steps + 1);

        if (result.newlyCompromisedTotal() != expectedTotal - 1) {
            throw new AssertionError(
                    "Incorrect accumulated new-compromise count.");
        }

        int actualTotal = 0;

        for (int id = 1; id <= size; id++) {
            ComputerNode expected = baseline.getComputer(id);
            ComputerNode actual = result.network().getComputer(id);

            if (actual.isCompromised()) {
                actualTotal++;
            }

            // Independent expectation for a chain starting at Computer 1.
            boolean expectedCompromised = id <= expectedTotal;

            if (expected.isCompromised() != expectedCompromised
                    || actual.isCompromised() != expectedCompromised
                    || expected.isIsolated() != actual.isIsolated()
                    || expected.isProtected() != actual.isProtected()
                    || actual.isIsolated()
                    || actual.isProtected()) {

                throw new AssertionError(
                        "Incorrect final state for Computer " + id);
            }
        }

        if (actualTotal != expectedTotal) {
            throw new AssertionError("Incorrect final compromised total.");
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
                    "Usage: ThreadReuseBenchmark [computers steps workers]");
        }

        int size = 100;
        int steps = 99;
        int workers = 3;

        if (args.length == 3) {
            size = parseNumber(args[0], "Computers", 3, 1000);
            steps = parseNumber(args[1], "Steps", 1, 1000);
            workers = parseNumber(
                    args[2], "Workers", 1, Math.min(size, 16));
        }

        System.out.println("CyberShield - Thread Reuse Benchmark");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nTopology: Chain");
        System.out.println("Defence: None");
        System.out.println("Computers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Workers per mode: " + workers);
        System.out.println("Warm-up runs per mode: " + WARMUP_RUNS);
        System.out.println("Measured runs per mode: " + MEASURED_RUNS);

        Network baseline = createBaseline(size, steps);

        System.out.println("\nWarming up both modes...");

        for (int run = 0; run < WARMUP_RUNS; run++) {
            boolean pooledFirst = run % 2 == 0;

            RunResult first =
                    measure(pooledFirst, size, steps, workers);
            verify(first, baseline, steps);

            RunResult second =
                    measure(!pooledFirst, size, steps, workers);
            verify(second, baseline, steps);
        }

        long[] perStepTimes = new long[MEASURED_RUNS];
        long[] pooledTimes = new long[MEASURED_RUNS];

        System.out.printf(
                "%n%4s %-12s %14s %14s%n",
                "Run", "First mode", "Per-step ms", "Pooled ms");

        for (int run = 0; run < MEASURED_RUNS; run++) {
            boolean pooledFirst = run % 2 == 0;

            RunResult first =
                    measure(pooledFirst, size, steps, workers);
            verify(first, baseline, steps);

            RunResult second =
                    measure(!pooledFirst, size, steps, workers);
            verify(second, baseline, steps);

            RunResult pooled = pooledFirst ? first : second;
            RunResult perStep = pooledFirst ? second : first;

            perStepTimes[run] = perStep.elapsedNanos();
            pooledTimes[run] = pooled.elapsedNanos();

            System.out.printf(
                    Locale.ROOT,
                    "%4d %-12s %14.3f %14.3f%n",
                    run + 1,
                    pooledFirst ? "Pooled" : "Per-step",
                    perStepTimes[run] / 1_000_000.0,
                    pooledTimes[run] / 1_000_000.0);
        }

        double perStepAverage = averageMillis(perStepTimes);
        double pooledAverage = averageMillis(pooledTimes);

        System.out.println(
                "\nAll warm-up and measured results passed verification.");

        System.out.printf(
                Locale.ROOT,
                "Average per-step time: %.3f ms%n", perStepAverage);
        System.out.printf(
                Locale.ROOT,
                "Average pooled time:   %.3f ms%n", pooledAverage);

        if (pooledAverage > 0) {
            System.out.printf(
                    Locale.ROOT,
                    "Per-step / pooled time ratio: %.2fx%n",
                    perStepAverage / pooledAverage);
        }

        System.out.println(
                "\nTiming includes simulator setup, execution and worker cleanup.");
        System.out.println(
                "Network construction, verification and printing are excluded.");
        System.out.println(
                "This ratio compares two parallel modes, not sequential speedup.");
    }
}