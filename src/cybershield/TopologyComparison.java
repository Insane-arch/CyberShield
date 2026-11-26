package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class TopologyComparison {

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
                throw new IllegalArgumentException(
                        "Unknown topology: " + topology);
        }
    }

    private static void verifyConnections(
            Network network, String topology) {

        int size = network.getSize();

        for (int first = 1; first <= size; first++) {
            for (int second = 1; second <= size; second++) {
                boolean expected;

                switch (topology) {
                    case "Chain":
                        expected = Math.abs(first - second) == 1;
                        break;
                    case "Ring":
                        expected = Math.abs(first - second) == 1
                                || Math.abs(first - second) == size - 1;
                        break;
                    case "Star":
                        expected = first != second
                                && (first == 1 || second == 1);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Unknown topology: " + topology);
                }

                if (network.areConnected(first, second) != expected) {
                    throw new AssertionError(
                            topology + ": incorrect connection between "
                            + first + " and " + second);
                }
            }
        }
    }

    private static void verifyMatchingStates(
            Network sequential,
            Network parallel,
            String label,
            int step) {

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = parallel.getComputer(id);

            if (expected.isCompromised() != actual.isCompromised()
                    || expected.isIsolated() != actual.isIsolated()
                    || expected.isProtected() != actual.isProtected()) {

                throw new AssertionError(
                        label + ": state mismatch at step "
                        + step + ", Computer " + id);
            }
        }
    }

    private static void verifyExpectedFinalStates(
            Network network, String topology, int scenario) {

        int protectedId = network.getSize() / 2 + 1;

        for (int id = 1; id <= network.getSize(); id++) {
            boolean expectedCompromised;

            if (scenario == NetworkFactory.ISOLATION) {
                expectedCompromised = id == 1;
            } else if (scenario == NetworkFactory.PROTECTION) {
                expectedCompromised = topology.equals("Chain")
                        ? id < protectedId
                        : id != protectedId;
            } else {
                expectedCompromised = true;
            }

            boolean expectedIsolated =
                    scenario == NetworkFactory.ISOLATION && id == 1;

            boolean expectedProtected =
                    scenario == NetworkFactory.PROTECTION
                    && id == protectedId;

            ComputerNode computer = network.getComputer(id);

            if (computer.isCompromised() != expectedCompromised
                    || computer.isIsolated() != expectedIsolated
                    || computer.isProtected() != expectedProtected) {

                throw new AssertionError(
                        topology + ": unexpected final state for Computer "
                        + id + ", scenario " + scenario);
            }
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        int size = 7;
        int steps = size - 1;
        int workers = 3;

        String[] topologies = {"Chain", "Ring", "Star"};

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        String[] scenarioNames = {
            "No defence",
            "Isolate Computer 1",
            "Protect Computer 4"
        };

        System.out.println("CyberShield - Network Topology Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Workers per network: " + workers);

        System.out.printf(
                "%n%-8s %-22s %10s %10s %8s%n",
                "Topology", "Scenario", "Sequential", "Parallel", "Check");

        int passed = 0;

        for (String topology : topologies) {
            for (int index = 0; index < scenarios.length; index++) {
                int scenario = scenarios[index];

                Network sequentialNetwork =
                        createNetwork(topology, size, scenario);

                Network parallelNetwork =
                        createNetwork(topology, size, scenario);

                verifyConnections(sequentialNetwork, topology);
                verifyConnections(parallelNetwork, topology);

                SequentialSimulator sequential =
                        new SequentialSimulator(sequentialNetwork);

                DataParallelSimulator parallel =
                        new DataParallelSimulator(parallelNetwork, workers);

                String label = topology + " / " + scenarioNames[index];

                verifyMatchingStates(
                        sequentialNetwork, parallelNetwork, label, 0);

                for (int step = 1; step <= steps; step++) {
                    int sequentialNew = sequential.runStep();
                    int parallelNew = parallel.runStep();

                    if (sequentialNew != parallelNew) {
                        throw new AssertionError(
                                label + ": new-compromise count mismatch"
                                + " at step " + step);
                    }

                    verifyMatchingStates(
                            sequentialNetwork, parallelNetwork, label, step);
                }

                verifyExpectedFinalStates(
                        sequentialNetwork, topology, scenario);
                verifyExpectedFinalStates(
                        parallelNetwork, topology, scenario);

                System.out.printf(
                        "%-8s %-22s %10d %10d %8s%n",
                        topology, scenarioNames[index],
                        sequential.getCompromisedCount(),
                        parallel.getCompromisedCount(),
                        "PASS");

                passed++;
            }
        }

        System.out.println(
                "\nAll " + passed + " topology scenario checks passed.");
    }
}