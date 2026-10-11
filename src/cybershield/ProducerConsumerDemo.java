package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ProducerConsumerDemo {

    public static void main(String[] args)
            throws InterruptedException {

        int size = 100;
        int steps = 99;
        int workerCount = 2;
        int queueCapacity = 1;

        System.out.println("CyberShield - Producer Consumer");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nConsumers: " + workerCount);
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

        // Start consumers before producing the tasks.
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new Thread(
                    new ConsumerWorker(queue), "Consumer-" + i);

            consumers[i].start();
        }

        ScenarioTask[] producedTasks =
                new ScenarioTask[scenarios.length];

        // The main thread acts as the producer.
        try {
            for (int i = 0; i < scenarios.length; i++) {
                Network network =
                        NetworkFactory.createChainScenario(
                                size, scenarios[i]);

                producedTasks[i] = new ScenarioTask(network, steps);
                queue.put(producedTasks[i]);
            }
        } finally {
            // Consumers finish remaining tasks before exiting.
            queue.close();
        }

        for (int i = 0; i < consumers.length; i++) {
            consumers[i].join();
        }

        System.out.println("\nProduction finished; consumers completed.");

        System.out.printf(
                "%n%-22s %12s %12s %8s%n",
                "Scenario", "Sequential", "Consumed", "Match");

        // Compare against fresh sequential executions.
        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createChainScenario(
                            size, scenarios[i]);

            ScenarioTask baseline = new ScenarioTask(network, steps);
            baseline.run();

            int expected = baseline.getCompromisedCount();
            int actual = producedTasks[i].getCompromisedCount();
            boolean matches = expected == actual;

            System.out.printf(
                    "%-22s %12d %12d %8s%n",
                    names[i], expected, actual,
                    matches ? "YES" : "NO");

            if (!matches) {
                throw new AssertionError(
                        "Result mismatch for " + names[i]);
            }
        }

        System.out.println("\nAll producer-consumer totals match.");
    }
}