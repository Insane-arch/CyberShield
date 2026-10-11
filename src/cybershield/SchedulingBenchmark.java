package cybershield;

import java.util.Arrays;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class SchedulingBenchmark {
    private static final int SEQUENTIAL = 0;
    private static final int STATIC = 1;
    private static final int QUEUED = 2;

    private static long measureRun(
            int mode, int size, int steps,
            int workerCount, int[] results)
            throws InterruptedException {

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        long start = System.nanoTime();

        ScenarioTask[] tasks = new ScenarioTask[scenarios.length];

        for (int i = 0; i < tasks.length; i++) {
            Network network =
                    NetworkFactory.createChainScenario(size, scenarios[i]);

            tasks[i] = new ScenarioTask(network, steps);
        }

        if (mode == SEQUENTIAL) {
            for (int i = 0; i < tasks.length; i++) {
                tasks[i].run();
            }
        } else {
            Thread[] threads = new Thread[workerCount];
            TaskQueue queue = null;

            if (mode == QUEUED) {
                queue = new TaskQueue(tasks);
            }

            for (int i = 0; i < threads.length; i++) {
                Runnable worker;

                if (mode == STATIC) {
                    worker = new ScenarioWorker(tasks, i, workerCount);
                } else {
                    worker = new QueueWorker(queue);
                }

                threads[i] = new Thread(worker, "Benchmark-" + i);
            }

            for (int i = 0; i < threads.length; i++) {
                threads[i].start();
            }

            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        }

        for (int i = 0; i < tasks.length; i++) {
            results[i] = tasks[i].getCompromisedCount();
        }

        return System.nanoTime() - start;
    }

    public static void main(String[] args)
            throws InterruptedException {

        int size = 500;
        int steps = 499;
        int workerCount = 2;

        if (args.length != 0
                && args.length != 2
                && args.length != 3) {

            System.out.println(
                    "Usage: java -cp out cybershield.SchedulingBenchmark"
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

        if (size < 3 || steps < 0
                || workerCount < 1 || workerCount > 3) {

            System.out.println(
                    "Size must be at least 3, steps cannot be negative,"
                    + " and workers must be 1, 2, or 3.");
            return;
        }

        String[] modeNames = {"Sequential", "Static", "Queued"};

        int warmupRuns = 3;
        int measuredRuns = 6;
        double[] totals = new double[3];

        System.out.println("CyberShield - Scheduling Benchmark");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Workers per threaded mode: " + workerCount);
        System.out.println("Warm-up runs per mode: " + warmupRuns);
        System.out.println("Measured runs per mode: " + measuredRuns);
        System.out.println("\nWarming up all three modes...");

        for (int round = 0;
                round < warmupRuns + measuredRuns;
                round++) {

            int[][] results = new int[3][3];
            long[] elapsed = new long[3];

            // Rotate which mode runs first, second, and third.
            for (int offset = 0; offset < 3; offset++) {
                int mode = (round + offset) % 3;

                elapsed[mode] = measureRun(
                        mode, size, steps, workerCount, results[mode]);
            }

            if (!Arrays.equals(results[SEQUENTIAL], results[STATIC])
                    || !Arrays.equals(
                            results[SEQUENTIAL], results[QUEUED])) {

                throw new AssertionError(
                        "Scenario totals differ between execution modes.");
            }

            if (round < warmupRuns) {
                continue;
            }

            if (round == warmupRuns) {
                System.out.printf(
                        "%n%-5s %-12s %14s %14s %14s%n",
                        "Run", "First mode",
                        "Sequential ms", "Static ms", "Queued ms");
            }

            double[] millis = new double[3];

            for (int mode = 0; mode < 3; mode++) {
                millis[mode] = elapsed[mode] / 1_000_000.0;
                totals[mode] += millis[mode];
            }

            System.out.printf(
                    "%-5d %-12s %14.3f %14.3f %14.3f%n",
                    round - warmupRuns + 1,
                    modeNames[round % 3],
                    millis[SEQUENTIAL],
                    millis[STATIC],
                    millis[QUEUED]);
        }

        double[] averages = new double[3];

        for (int mode = 0; mode < 3; mode++) {
            averages[mode] = totals[mode] / measuredRuns;
        }

        System.out.println("\nAll scenario totals matched in every run.");

        System.out.printf(
                "Average sequential time: %.3f ms%n",
                averages[SEQUENTIAL]);

        System.out.printf(
                "%n%-12s %14s %12s %14s%n",
                "Mode", "Average ms", "Speedup", "Efficiency");

        for (int mode = STATIC; mode <= QUEUED; mode++) {
            double speedup = averages[SEQUENTIAL] / averages[mode];
            double efficiency = speedup / workerCount * 100.0;

            System.out.printf(
                    "%-12s %14.3f %11.2fx %13.2f%%%n",
                    modeNames[mode],
                    averages[mode],
                    speedup,
                    efficiency);
        }
    }
}