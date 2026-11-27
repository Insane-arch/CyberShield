package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class PooledParallelComparison {

    private static Network createNetwork(
            String topology, int size, int scenario) {

        switch (topology) {
            case "Chain":
                return NetworkFactory.createChainScenario(size, scenario);
            case "Ring":
                return NetworkFactory.createRingScenario(size, scenario);
            case "Star":
                return NetworkFactory.createStarScenario(size, scenario);
            default:
                throw new IllegalArgumentException("Unknown topology.");
        }
    }

    private static void verifyStates(
            Network sequential,
            Network pooled,
            String label,
            int step) {

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = pooled.getComputer(id);

            if (expected.getId() != actual.getId()
                    || expected.isCompromised() != actual.isCompromised()
                    || expected.isIsolated() != actual.isIsolated()
                    || expected.isProtected() != actual.isProtected()) {

                throw new AssertionError(
                        label + ": state mismatch at step "
                        + step + ", Computer " + id);
            }
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        int size = 7;
        int steps = 8;
        int workers = 3;

        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Usage: PooledParallelComparison [workers]");
        }

        if (args.length == 1) {
            workers = Integer.parseInt(args[0]);
        }

        if (workers < 1 || workers > size) {
            throw new IllegalArgumentException(
                    "Workers must be between 1 and " + size + ".");
        }

        String[] topologies = {"Chain", "Ring", "Star"};

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect Computer 4"
        };

        int[][] expectedTotals = {
            {7, 1, 3},
            {7, 1, 6},
            {7, 1, 6}
        };

        System.out.println("CyberShield - Reusable Worker Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Pool workers: " + workers);

        System.out.printf(
                "%n%-8s %-22s %10s %10s %8s%n",
                "Topology", "Scenario", "Sequential", "Pooled", "Check");

        int passed = 0;

        for (int topology = 0; topology < topologies.length; topology++) {
            for (int scenario = 0; scenario < scenarios.length; scenario++) {
                Network sequentialNetwork = createNetwork(
                        topologies[topology], size, scenarios[scenario]);

                Network pooledNetwork = createNetwork(
                        topologies[topology], size, scenarios[scenario]);

                SequentialSimulator sequential =
                        new SequentialSimulator(sequentialNetwork);

                String label = topologies[topology] + " / " + names[scenario];

                try (PooledDataParallelSimulator pooled =
                        new PooledDataParallelSimulator(
                                pooledNetwork, workers)) {

                    verifyStates(sequentialNetwork, pooledNetwork, label, 0);

                    for (int step = 1; step <= steps; step++) {
                        int sequentialNew = sequential.runStep();
                        int pooledNew = pooled.runStep();

                        if (sequentialNew != pooledNew
                                || sequential.getCompromisedCount()
                                != pooled.getCompromisedCount()) {

                            throw new AssertionError(
                                    label + ": count mismatch at step " + step);
                        }

                        verifyStates(
                                sequentialNetwork, pooledNetwork, label, step);
                    }

                    int expected = expectedTotals[topology][scenario];

                    if (sequential.getCompromisedCount() != expected
                            || pooled.getCompromisedCount() != expected) {
                        throw new AssertionError(
                                label + ": incorrect final total.");
                    }

                    System.out.printf(
                            "%-8s %-22s %10d %10d %8s%n",
                            topologies[topology], names[scenario],
                            sequential.getCompromisedCount(),
                            pooled.getCompromisedCount(), "PASS");

                    passed++;
                }
            }
        }

        System.out.println(
                "\nAll " + passed + " pooled scenario checks passed.");
        System.out.println("All simulation pools closed.");
    }
}