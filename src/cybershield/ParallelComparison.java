package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ParallelComparison {

    public static void main(String[] args)
            throws InterruptedException {

        int workerCount = 3;

        if (args.length > 1) {
            System.out.println(
                    "Usage: java -cp out cybershield.ParallelComparison"
                    + " [workers]");
            return;
        }

        if (args.length == 1) {
            try {
                workerCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                System.out.println("Worker count must be 1, 2, or 3.");
                return;
            }
        }

        if (workerCount < 1 || workerCount > 3) {
            System.out.println("Worker count must be 1, 2, or 3.");
            return;
        }

        System.out.println("CyberShield - Worker Count Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");
        System.out.println("\nWorker threads: " + workerCount);

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect Computer 4"
        };

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        int steps = 3;
        int[] sequentialResults = new int[scenarios.length];

        // Sequential baseline.
        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createScenario(scenarios[i]);

            ScenarioTask task = new ScenarioTask(network, steps);
            task.run();

            sequentialResults[i] = task.getCompromisedCount();
        }

        // Fresh tasks for worker execution.
        ScenarioTask[] tasks = new ScenarioTask[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createScenario(scenarios[i]);

            tasks[i] = new ScenarioTask(network, steps);
        }

        Thread[] threads = new Thread[workerCount];

        for (int i = 0; i < workerCount; i++) {
            ScenarioWorker worker =
                    new ScenarioWorker(tasks, i, workerCount);

            threads[i] = new Thread(worker, "Worker-" + i);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.printf(
                "%n%-22s %12s %12s %8s%n",
                "Scenario", "Sequential", "Threaded", "Match");

        for (int i = 0; i < scenarios.length; i++) {
            int threadedResult = tasks[i].getCompromisedCount();
            boolean matches =
                    sequentialResults[i] == threadedResult;

            System.out.printf(
                    "%-22s %12d %12d %8s%n",
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
    }
}