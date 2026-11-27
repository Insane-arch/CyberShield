package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class TargetSequentialComparison {

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
            Network sourceFirst,
            Network targetFirst,
            String label,
            int step) {

        for (int id = 1; id <= sourceFirst.getSize(); id++) {
            ComputerNode expected = sourceFirst.getComputer(id);
            ComputerNode actual = targetFirst.getComputer(id);

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

    public static void main(String[] args) {
        int size = 7;
        int steps = 8;

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

        System.out.println("CyberShield - Sequential Algorithm Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Both implementations run sequentially.");

        System.out.printf(
                "%n%-8s %-22s %12s %12s %8s%n",
                "Topology", "Scenario", "Source-first", "Target-first", "Check");

        int passed = 0;

        for (int topology = 0; topology < topologies.length; topology++) {
            for (int scenario = 0; scenario < scenarios.length; scenario++) {
                Network sourceNetwork = createNetwork(
                        topologies[topology], size, scenarios[scenario]);

                Network targetNetwork = createNetwork(
                        topologies[topology], size, scenarios[scenario]);

                SequentialSimulator sourceFirst =
                        new SequentialSimulator(sourceNetwork);

                TargetSequentialSimulator targetFirst =
                        new TargetSequentialSimulator(targetNetwork);

                String label = topologies[topology] + " / " + names[scenario];

                verifyStates(sourceNetwork, targetNetwork, label, 0);

                for (int step = 1; step <= steps; step++) {
                    int sourceNew = sourceFirst.runStep();
                    int targetNew = targetFirst.runStep();

                    if (sourceNew != targetNew
                            || sourceFirst.getCompromisedCount()
                            != targetFirst.getCompromisedCount()) {

                        throw new AssertionError(
                                label + ": count mismatch at step " + step);
                    }

                    verifyStates(
                            sourceNetwork, targetNetwork, label, step);
                }

                int expected = expectedTotals[topology][scenario];

                if (sourceFirst.getCompromisedCount() != expected
                        || targetFirst.getCompromisedCount() != expected) {
                    throw new AssertionError(
                            label + ": incorrect final total.");
                }

                System.out.printf(
                        "%-8s %-22s %12d %12d %8s%n",
                        topologies[topology], names[scenario],
                        sourceFirst.getCompromisedCount(),
                        targetFirst.getCompromisedCount(),
                        "PASS");

                passed++;
            }
        }

        System.out.println(
                "\nAll " + passed + " sequential algorithm checks passed.");
    }
}