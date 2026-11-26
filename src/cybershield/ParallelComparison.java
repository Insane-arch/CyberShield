package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ParallelComparison {

    private static boolean compareStates(
            Network sequential,
            Network threaded,
            String scenarioName) {

        if (sequential.getSize() != threaded.getSize()) {
            System.err.println(
                    "Network size mismatch for " + scenarioName);
            return false;
        }

        boolean allMatch = true;

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = threaded.getComputer(id);

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
                System.err.println("Threaded:   " + actual);
            }
        }

        return allMatch;
    }

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

        System.out.println("CyberShield - Worker State Comparison");
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
        Network[] sequentialNetworks = new Network[scenarios.length];
        Network[] threadedNetworks = new Network[scenarios.length];

        // Keep each baseline network for individual state comparison.
        for (int i = 0; i < scenarios.length; i++) {
            sequentialNetworks[i] =
                    NetworkFactory.createScenario(scenarios[i]);

            ScenarioTask task =
                    new ScenarioTask(sequentialNetworks[i], steps);

            task.run();
            sequentialResults[i] = task.getCompromisedCount();
        }

        // Each threaded task owns a fresh, independent network.
        ScenarioTask[] tasks = new ScenarioTask[scenarios.length];

        for (int i = 0; i < scenarios.length; i++) {
            threadedNetworks[i] =
                    NetworkFactory.createScenario(scenarios[i]);

            tasks[i] = new ScenarioTask(threadedNetworks[i], steps);
        }

        Thread[] threads = new Thread[workerCount];

        // Each worker has its own failure slot.
        Throwable[] workerFailures = new Throwable[workerCount];

        for (int i = 0; i < workerCount; i++) {
            ScenarioWorker worker =
                    new ScenarioWorker(tasks, i, workerCount);

            threads[i] = new Thread(worker, "Worker-" + i);

            final int workerIndex = i;
            threads[i].setUncaughtExceptionHandler(
                    (thread, failure) ->
                            workerFailures[workerIndex] = failure);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        // Join before reading results, states or failure slots.
        for (Thread thread : threads) {
            thread.join();
        }

        for (int i = 0; i < workerFailures.length; i++) {
            if (workerFailures[i] != null) {
                throw new IllegalStateException(
                        "Worker-" + i + " failed.",
                        workerFailures[i]);
            }
        }

        System.out.printf(
                "%n%-22s %12s %12s %8s %8s%n",
                "Scenario", "Sequential", "Threaded",
                "Totals", "States");

        boolean allMatch = true;

        for (int i = 0; i < scenarios.length; i++) {
            int threadedResult = tasks[i].getCompromisedCount();

            boolean totalsMatch =
                    sequentialResults[i] == threadedResult;

            boolean statesMatch = compareStates(
                    sequentialNetworks[i],
                    threadedNetworks[i],
                    names[i]);

            System.out.printf(
                    "%-22s %12d %12d %8s %8s%n",
                    names[i],
                    sequentialResults[i],
                    threadedResult,
                    totalsMatch ? "YES" : "NO",
                    statesMatch ? "YES" : "NO");

            if (!totalsMatch || !statesMatch) {
                allMatch = false;
            }
        }

        if (!allMatch) {
            throw new AssertionError(
                    "Sequential and threaded results differ.");
        }

        System.out.println(
                "\nAll scenario totals and computer states match.");
    }
}