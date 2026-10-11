package cybershield;

import java.util.Arrays;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class PerformanceBenchmark {

    private static long measureRun(
            boolean threaded, int size, int steps,
            int workerCount, int[] results)
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
            Thread[] threads = new Thread[workerCount];

            for (int i = 0; i < threads.length; i++) {
                ScenarioWorker worker =
                        new ScenarioWorker(tasks, i, workerCount);

                threads[i] = new Thread(worker, "Benchmark-" + i);
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
        int workerCount = 3;

        if (args.length != 0
                && args.length != 2
                && args.length != 3) {

            System.out.println(
                    "Usage: java -cp out cybershield.PerformanceBenchmark"
                    + " [size steps [workers]]");
            return;
        }

        try {
            if (args.length >= 2) {
                size = Integer.parseInt(args[0]);
                steps = Integer.parseInt(args[1]);
            }

            if (args.length == 3) {
                workerCount = Integer.parseInt(args[2]);
            }
        } catch (NumberFormatException exception) {
            System.out.println(
                    "Enter whole numbers within the Java int range.");
            return;
        }

        if (size < 3 || steps < 0) {
            System.out.println(
                    "Size must be at least 3. Steps cannot be negative.");
            return;
        }

        if (workerCount < 1 || workerCount > 3) {
            System.out.println("Worker count must be 1, 2, or 3.");
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
        System.out.println("Worker threads: " + workerCount);
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

            if (round % 2 == 0) {
                sequentialNanos = measureRun(
                        false, size, steps, workerCount, sequentialResults);

                threadedNanos = measureRun(
                        true, size, steps, workerCount, threadedResults);
            } else {
                threadedNanos = measureRun(
                        true, size, steps, workerCount, threadedResults);

                sequentialNanos = measureRun(
                        false, size, steps, workerCount, sequentialResults);
            }

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
        double efficiency = speedup / workerCount * 100.0;

        System.out.println("\nAll scenario totals matched in every run.");

        System.out.printf(
                "Average sequential time: %.3f ms%n", sequentialAverage);

        System.out.printf(
                "Average threaded time:   %.3f ms%n", threadedAverage);

        System.out.printf("Speedup: %.2fx%n", speedup);
        System.out.printf("Efficiency: %.2f%%%n", efficiency);
    }
}