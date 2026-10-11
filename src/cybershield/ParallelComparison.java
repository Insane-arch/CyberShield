package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ParallelComparison {

    private static Network createNetwork(int scenario) {
        Network network = new Network(5);

        network.connect(1, 2);
        network.connect(1, 3);
        network.connect(2, 4);
        network.connect(3, 4);
        network.connect(4, 5);

        network.getComputer(1).compromise();

        if (scenario == 1) {
            network.getComputer(1).isolate();
        } else if (scenario == 2) {
            network.getComputer(4).protect();
        }

        return network;
    }

    public static void main(String[] args)
            throws InterruptedException {

        System.out.println("CyberShield - Sequential vs Threaded");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect Computer 4"
        };

        int steps = 3;
        int[] sequentialResults = new int[names.length];

        // Run each scenario sequentially on a fresh network.
        for (int i = 0; i < names.length; i++) {
            ScenarioTask task =
                    new ScenarioTask(NetworkFactory.createScenario(i), steps);

            task.run();
            sequentialResults[i] = task.getCompromisedCount();
        }

        ScenarioTask[] tasks = new ScenarioTask[names.length];
        Thread[] threads = new Thread[names.length];

        // Create a separate task and network for each thread.
        for (int i = 0; i < names.length; i++) {
            tasks[i] = new ScenarioTask(createNetwork(i), steps);
            threads[i] = new Thread(tasks[i], "Scenario-" + i);
        }

        // Start every thread before waiting for any of them.
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        // Wait for all worker threads to finish.
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.println("\nFinal compromised totals:");
        System.out.printf(
                "%-22s %12s %12s %10s%n",
                "Scenario", "Sequential", "Threaded", "Match");

        for (int i = 0; i < names.length; i++) {
            int threadedResult = tasks[i].getCompromisedCount();
            boolean matches =
                    sequentialResults[i] == threadedResult;

            System.out.printf(
                    "%-22s %12d %12d %10s%n",
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