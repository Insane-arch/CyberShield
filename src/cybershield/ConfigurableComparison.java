package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ConfigurableComparison {

    public static void main(String[] args)
            throws InterruptedException {

        int size = 6;
        int steps = 5;

        if (args.length != 0 && args.length != 2) {
            System.out.println(
                    "Usage: java -cp out cybershield.ConfigurableComparison"
                    + " [size steps]");
            return;
        }

        if (args.length == 2) {
            try {
                size = Integer.parseInt(args[0]);
                steps = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                System.out.println(
                        "Network size and steps must be whole numbers"
                        + " within the Java int range.");
                return;
            }
        }

        if (size < 3 || steps < 0) {
            System.out.println(
                    "Network size must be at least 3."
                    + " Steps must be zero or greater.");
            return;
        }

        System.out.println("CyberShield - Configurable Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nNetwork type: Chain");
        System.out.println("Computers: " + size);
        System.out.println("Simulation steps: " + steps);
        System.out.println("Initially compromised: Computer 1");
        System.out.println(
                "Protection target: Computer " + (size / 2 + 1));

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect middle computer"
        };

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        // 1. Sequential execution and timing.
        long sequentialStart = System.nanoTime();

        int[] sequentialResults = new int[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createChainScenario(size, scenarios[i]);

            ScenarioTask task = new ScenarioTask(network, steps);
            task.run();

            sequentialResults[i] = task.getCompromisedCount();
        }

        long sequentialNanos = System.nanoTime() - sequentialStart;

        // 2. Threaded execution and timing.
        long threadedStart = System.nanoTime();

        ScenarioTask[] tasks = new ScenarioTask[scenarios.length];
        Thread[] threads = new Thread[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createChainScenario(size, scenarios[i]);

            tasks[i] = new ScenarioTask(network, steps);
            threads[i] = new Thread(tasks[i], "Scenario-" + i);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        int[] threadedResults = new int[scenarios.length];

        for (int i = 0; i < tasks.length; i++) {
            threadedResults[i] = tasks[i].getCompromisedCount();
        }

        long threadedNanos = System.nanoTime() - threadedStart;

        // 3. Compare and display results outside the timed sections.
        System.out.printf(
                "%n%-26s %12s %12s %8s%n",
                "Scenario", "Sequential", "Threaded", "Match");

        for (int i = 0; i < scenarios.length; i++) {
            int threadedResult = threadedResults[i];
            boolean matches =
                    sequentialResults[i] == threadedResult;

            System.out.printf(
                    "%-26s %12d %12d %8s%n",
                    names[i],
                    sequentialResults[i],
                    threadedResult,
                    matches ? "YES" : "NO");

            if (!matches) {
                throw new AssertionError(
                        "Result mismatch for " + names[i]);
            }
        }

        System.out.println("\nAll scenario totals match.");

        double sequentialMillis = sequentialNanos / 1_000_000.0;
        double threadedMillis = threadedNanos / 1_000_000.0;

        System.out.printf(
                "%nSequential elapsed time: %.3f ms%n",
                sequentialMillis);

        System.out.printf(
                "Threaded elapsed time:   %.3f ms%n",
                threadedMillis);

        System.out.println(
                "Single-run measurements;"
                + " performance conclusions require repeated runs.");
    }
}