package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class QueuedComparison {

    private static ScenarioTask[] createTasks(int size, int steps) {
        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        ScenarioTask[] tasks = new ScenarioTask[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createChainScenario(size, scenarios[i]);

            tasks[i] = new ScenarioTask(network, steps);
        }

        return tasks;
    }

    public static void main(String[] args)
            throws InterruptedException {

        int workerCount = 2;

        if (args.length > 1) {
            System.out.println(
                    "Usage: java -cp out cybershield.QueuedComparison"
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

        int size = 100;
        int steps = 99;

        System.out.println("CyberShield - Shared Task Queue");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Worker threads: " + workerCount);

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect Computer 51"
        };

        // Sequential baseline.
        ScenarioTask[] sequentialTasks = createTasks(size, steps);

        for (int i = 0; i < sequentialTasks.length; i++) {
            sequentialTasks[i].run();
        }

        // Fresh tasks and one queue shared by every worker.
        ScenarioTask[] queuedTasks = createTasks(size, steps);
        TaskQueue queue = new TaskQueue(queuedTasks);

        Thread[] threads = new Thread[workerCount];

        for (int i = 0; i < threads.length; i++) {
            QueueWorker worker = new QueueWorker(queue);
            threads[i] = new Thread(worker, "QueueWorker-" + i);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.printf(
                "%n%-22s %12s %12s %8s%n",
                "Scenario", "Sequential", "Queued", "Match");

        for (int i = 0; i < names.length; i++) {
            int sequentialResult =
                    sequentialTasks[i].getCompromisedCount();

            int queuedResult =
                    queuedTasks[i].getCompromisedCount();

            boolean matches = sequentialResult == queuedResult;

            System.out.printf(
                    "%-22s %12d %12d %8s%n",
                    names[i],
                    sequentialResult,
                    queuedResult,
                    matches ? "YES" : "NO");

            if (!matches) {
                throw new AssertionError(
                        "Result mismatch for " + names[i]);
            }
        }

        System.out.println("\nAll queued scenario totals match.");
    }
}