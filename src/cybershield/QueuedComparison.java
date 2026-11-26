package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class QueuedComparison {

    private static ScenarioTask[] createTasks(
            int size, int steps, Network[] networks) {

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        ScenarioTask[] tasks = new ScenarioTask[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            networks[i] =
                    NetworkFactory.createChainScenario(size, scenarios[i]);

            tasks[i] = new ScenarioTask(networks[i], steps);
        }

        return tasks;
    }

    private static boolean compareStates(
            Network sequential,
            Network queued,
            String scenarioName) {

        if (sequential.getSize() != queued.getSize()) {
            System.err.println(
                    "Network size mismatch for " + scenarioName);
            return false;
        }

        boolean allMatch = true;

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = queued.getComputer(id);

            boolean matches =
                    expected.getId() == actual.getId()
                    && expected.isCompromised() == actual.isCompromised()
                    && expected.isIsolated() == actual.isIsolated()
                    && expected.isProtected() == actual.isProtected();

            if (!matches) {
                allMatch = false;

                System.err.println(
                        "State mismatch for " + scenarioName
                        + ", Computer " + id);
                System.err.println("Sequential: " + expected);
                System.err.println("Queued:     " + actual);
            }
        }

        return allMatch;
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

        System.out.println("CyberShield - Shared Queue State Comparison");
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

        // Retain the networks to compare individual computer states.
        Network[] sequentialNetworks = new Network[names.length];
        ScenarioTask[] sequentialTasks =
                createTasks(size, steps, sequentialNetworks);

        for (ScenarioTask task : sequentialTasks) {
            task.run();
        }

        // Each queued task owns a fresh network.
        Network[] queuedNetworks = new Network[names.length];
        ScenarioTask[] queuedTasks =
                createTasks(size, steps, queuedNetworks);

        TaskQueue queue = new TaskQueue(queuedTasks);

        Thread[] threads = new Thread[workerCount];
        Throwable[] workerFailures = new Throwable[workerCount];

        for (int i = 0; i < threads.length; i++) {
            QueueWorker worker = new QueueWorker(queue);
            threads[i] = new Thread(worker, "QueueWorker-" + i);

            final int workerIndex = i;
            threads[i].setUncaughtExceptionHandler(
                    (thread, failure) ->
                            workerFailures[workerIndex] = failure);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all workers before reading results or failure slots.
        for (Thread thread : threads) {
            thread.join();
        }

        for (int i = 0; i < workerFailures.length; i++) {
            if (workerFailures[i] != null) {
                throw new IllegalStateException(
                        "QueueWorker-" + i + " failed.",
                        workerFailures[i]);
            }
        }

        System.out.printf(
                "%n%-22s %12s %12s %8s %8s%n",
                "Scenario", "Sequential", "Queued",
                "Totals", "States");

        boolean allMatch = true;

        for (int i = 0; i < names.length; i++) {
            int sequentialResult =
                    sequentialTasks[i].getCompromisedCount();

            int queuedResult =
                    queuedTasks[i].getCompromisedCount();

            boolean totalsMatch = sequentialResult == queuedResult;

            boolean statesMatch = compareStates(
                    sequentialNetworks[i],
                    queuedNetworks[i],
                    names[i]);

            System.out.printf(
                    "%-22s %12d %12d %8s %8s%n",
                    names[i],
                    sequentialResult,
                    queuedResult,
                    totalsMatch ? "YES" : "NO",
                    statesMatch ? "YES" : "NO");

            if (!totalsMatch || !statesMatch) {
                allMatch = false;
            }
        }

        if (!allMatch) {
            throw new AssertionError(
                    "Sequential and queued results differ.");
        }

        System.out.println(
                "\nAll queued scenario totals and computer states match.");
    }
}