package cybershield;

import java.util.Arrays;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class PerformanceBenchmark {

    private static long measureRun(
            boolean threaded, int size, int steps, int[] results)
            throws InterruptedException {

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        long start = System.nanoTime();

        ScenarioTask[] tasks = new ScenarioTask[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createChainScenario(size, scenarios[i]);

            tasks[i] = new ScenarioTask(network, steps);
        }

        if (threaded) {
            Thread[] threads = new Thread[tasks.length];

            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(tasks[i], "Benchmark-" + i);
            }

            for (int i = 0; i < threads.length; i++) {
                threads[i].start();
            }

            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        } else {
            for (int i = 0; i < tasks.length; i++) {
                tasks[i].run();
            }
        }

        for (int i = 0; i < tasks.length; i++) {
            results[i] = tasks[i].getCompromisedCount();
        }

        return System.nanoTime() - start;
    }

    public static void main(String[] args)
            throws InterruptedException {

        int size = 100;
        int steps = 99;

        if (args.length != 0 && args.length != 2) {
            System.out.println(
                    "Usage: java -cp out cybershield.PerformanceBenchmark"
                    + " [size steps]");
            return;
        }

        if (args.length == 2) {
            try {
                size = Integer.parseInt(args[0]);
                steps = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                System.out.println(
                        "Enter whole numbers within the Java int range.");
                return;
            }
        }

        if (size < 3 || steps < 0) {
            System.out.println(
                    "Size must be at least 3. Steps cannot be negative.");
            return;
        }

        int warmupRuns = 3;
        int measuredRuns = 6;

        double sequentialTotal = 0;
        double threadedTotal = 0;

        System.out.println("CyberShield - Performance Benchmark");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Worker threads: 3");
        System.out.println("Warm-up runs per mode: " + warmupRuns);
        System.out.println("Measured runs per mode: " + measuredRuns);
        System.out.println("\nWarming up both execution modes...");

        for (int round = 0;
                round < warmupRuns + measuredRuns;
                round++) {

            int[] sequentialResults = new int[3];
            int[] threadedResults = new int[3];

            long sequentialNanos;
            long threadedNanos;

            // Alternate execution order to reduce order bias.
            if (round % 2 == 0) {
                sequentialNanos =
                        measureRun(false, size, steps, sequentialResults);

                threadedNanos =
                        measureRun(true, size, steps, threadedResults);
            } else {
                threadedNanos =
                        measureRun(true, size, steps, threadedResults);

                sequentialNanos =
                        measureRun(false, size, steps, sequentialResults);
            }

            // Correctness checks happen outside the timed sections.
            if (!Arrays.equals(sequentialResults, threadedResults)) {
                throw new AssertionError(
                        "Sequential and threaded totals do not match.");
            }

            if (round < warmupRuns) {
                continue;
            }

            if (round == warmupRuns) {
                System.out.printf(
                        "%n%-6s %-18s %16s %16s%n",
                        "Run", "First mode", "Sequential ms", "Threaded ms");
            }

            double sequentialMillis = sequentialNanos / 1_000_000.0;
            double threadedMillis = threadedNanos / 1_000_000.0;

            sequentialTotal += sequentialMillis;
            threadedTotal += threadedMillis;

            int runNumber = round - warmupRuns + 1;
            String firstMode =
                    round % 2 == 0 ? "Sequential" : "Threaded";

            System.out.printf(
                    "%-6d %-18s %16.3f %16.3f%n",
                    runNumber, firstMode, sequentialMillis, threadedMillis);
        }

        double sequentialAverage = sequentialTotal / measuredRuns;
        double threadedAverage = threadedTotal / measuredRuns;
        double speedup = sequentialAverage / threadedAverage;

        System.out.println("\nAll scenario totals matched in every run.");

        System.out.printf(
                "Average sequential time: %.3f ms%n", sequentialAverage);

        System.out.printf(
                "Average threaded time:   %.3f ms%n", threadedAverage);

        System.out.printf(
                "Speedup: %.2fx%n", speedup);
    }
}