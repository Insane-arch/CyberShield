package cybershield;

import java.util.concurrent.ForkJoinPool;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ForkJoinDemo {

    private static ScenarioTask[] createTasks(
            int taskCount, int size, int steps) {

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        ScenarioTask[] tasks = new ScenarioTask[taskCount];

        for (int i = 0; i < tasks.length; i++) {
            int scenario = scenarios[i % scenarios.length];

            Network network =
                    NetworkFactory.createChainScenario(size, scenario);

            tasks[i] = new ScenarioTask(network, steps);
        }

        return tasks;
    }

    public static void main(String[] args) {
        int size = 100;
        int steps = 99;
        int taskCount = 12;
        int parallelism = 3;
        int threshold = 2;

        System.out.println("CyberShield - Fork Join");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nSimulation tasks: " + taskCount);
        System.out.println("Pool parallelism: " + parallelism);
        System.out.println("Leaf threshold: " + threshold);

        ScenarioTask[] sequentialTasks =
                createTasks(taskCount, size, steps);

        for (int i = 0; i < sequentialTasks.length; i++) {
            sequentialTasks[i].run();
        }

        // Separate networks for the Fork/Join execution.
        ScenarioTask[] forkJoinTasks =
                createTasks(taskCount, size, steps);

        SimulationBatchTask batch = new SimulationBatchTask(
                forkJoinTasks, 0, forkJoinTasks.length, threshold);

        try (ForkJoinPool pool = new ForkJoinPool(parallelism)) {
            pool.invoke(batch);
        }

        String[] names = {
            "No defence",
            "Isolation",
            "Protection"
        };

        System.out.printf(
                "%n%-6s %-14s %12s %12s %8s%n",
                "Task", "Scenario", "Sequential", "ForkJoin", "Match");

        for (int i = 0; i < taskCount; i++) {
            int expected = sequentialTasks[i].getCompromisedCount();
            int actual = forkJoinTasks[i].getCompromisedCount();
            boolean matches = expected == actual;

            System.out.printf(
                    "%-6d %-14s %12d %12d %8s%n",
                    i + 1,
                    names[i % names.length],
                    expected,
                    actual,
                    matches ? "YES" : "NO");

            if (!matches) {
                throw new AssertionError(
                        "Result mismatch for task " + (i + 1));
            }
        }

        System.out.println("\nAll 12 Fork/Join task totals match.");
    }
}