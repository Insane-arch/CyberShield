package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ProducerConsumerDemo {

    private static boolean compareStates(
            Network sequential,
            Network consumed,
            String scenarioName) {

        if (sequential.getSize() != consumed.getSize()) {
            System.err.println(
                    "Network size mismatch for " + scenarioName);
            return false;
        }

        boolean allMatch = true;

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = consumed.getComputer(id);

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
                System.err.println("Consumed:   " + actual);
            }
        }

        return allMatch;
    }

    public static void main(String[] args)
            throws InterruptedException {

        int size = 100;
        int steps = 99;
        int workerCount = 2;
        int queueCapacity = 1;

        System.out.println(
                "CyberShield - Producer Consumer State Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Consumers: " + workerCount);
        System.out.println("Queue capacity: " + queueCapacity);

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect Computer 51"
        };

        BlockingTaskQueue queue =
                new BlockingTaskQueue(queueCapacity);

        Thread[] consumers = new Thread[workerCount];
        Throwable[] consumerFailures = new Throwable[workerCount];

        Network[] producedNetworks = new Network[scenarios.length];
        ScenarioTask[] producedTasks =
                new ScenarioTask[scenarios.length];

        // Start consumers before producing tasks.
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new Thread(
                    new ConsumerWorker(queue), "Consumer-" + i);

            final int consumerIndex = i;

            consumers[i].setUncaughtExceptionHandler(
                    (thread, failure) -> {
                        consumerFailures[consumerIndex] = failure;

                        // Wake a producer waiting on a full queue.
                        queue.close();
                    });

            consumers[i].start();
        }

        try {
            // The main thread produces independent scenario tasks.
            for (int i = 0; i < scenarios.length; i++) {
                producedNetworks[i] =
                        NetworkFactory.createChainScenario(
                                size, scenarios[i]);

                producedTasks[i] =
                        new ScenarioTask(producedNetworks[i], steps);

                queue.put(producedTasks[i]);
            }
        } finally {
            // Normal closure allows consumers to drain queued tasks.
            queue.close();

            for (Thread consumer : consumers) {
                consumer.join();
            }
        }

        for (int i = 0; i < consumerFailures.length; i++) {
            if (consumerFailures[i] != null) {
                throw new IllegalStateException(
                        "Consumer-" + i + " failed.",
                        consumerFailures[i]);
            }
        }

        System.out.println(
                "\nProduction finished; consumers completed.");

        System.out.printf(
                "%n%-22s %12s %12s %8s %8s%n",
                "Scenario", "Sequential", "Consumed",
                "Totals", "States");

        boolean allMatch = true;

        for (int i = 0; i < scenarios.length; i++) {
            Network baselineNetwork =
                    NetworkFactory.createChainScenario(
                            size, scenarios[i]);

            ScenarioTask baseline =
                    new ScenarioTask(baselineNetwork, steps);

            baseline.run();

            int expected = baseline.getCompromisedCount();
            int actual = producedTasks[i].getCompromisedCount();

            boolean totalsMatch = expected == actual;

            boolean statesMatch = compareStates(
                    baselineNetwork,
                    producedNetworks[i],
                    names[i]);

            System.out.printf(
                    "%-22s %12d %12d %8s %8s%n",
                    names[i], expected, actual,
                    totalsMatch ? "YES" : "NO",
                    statesMatch ? "YES" : "NO");

            if (!totalsMatch || !statesMatch) {
                allMatch = false;
            }
        }

        if (!allMatch) {
            throw new AssertionError(
                    "Sequential and producer-consumer results differ.");
        }

        System.out.println(
                "\nAll producer-consumer totals and computer states match.");
    }
}