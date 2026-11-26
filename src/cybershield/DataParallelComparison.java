package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class DataParallelComparison {

    private static void verifyStates(
            Network sequential,
            Network parallel,
            String scenario,
            int step) {

        if (sequential.getSize() != parallel.getSize()) {
            throw new AssertionError("Network sizes differ.");
        }

        for (int id = 1; id <= sequential.getSize(); id++) {
            ComputerNode expected = sequential.getComputer(id);
            ComputerNode actual = parallel.getComputer(id);

            boolean matches =
                    expected.getId() == actual.getId()
                    && expected.isCompromised() == actual.isCompromised()
                    && expected.isIsolated() == actual.isIsolated()
                    && expected.isProtected() == actual.isProtected();

            if (!matches) {
                throw new AssertionError(
                        scenario + ", step " + step
                        + ", Computer " + id
                        + "\nSequential: " + expected
                        + "\nParallel:   " + actual);
            }
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        int size = 100;
        int steps = 99;
        int workerCount = 3;

        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Usage: DataParallelComparison [workers]");
        }

        if (args.length == 1) {
            try {
                workerCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Worker count must be a whole number.", exception);
            }
        }

        if (workerCount < 1 || workerCount > size) {
            throw new IllegalArgumentException(
                    "Worker count must be between 1 and " + size + ".");
        }

        System.out.println("CyberShield - Data Parallel Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nComputers: " + size);
        System.out.println("Steps: " + steps);
        System.out.println("Workers per network: " + workerCount);

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect Computer 51"
        };

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        int[] expectedTotals = {100, 1, 50};

        System.out.printf(
                "%n%-22s %12s %12s %12s%n",
                "Scenario", "Sequential", "Parallel", "Every step");

        for (int scenario = 0; scenario < scenarios.length; scenario++) {
            Network sequentialNetwork =
                    NetworkFactory.createChainScenario(
                            size, scenarios[scenario]);

            Network parallelNetwork =
                    NetworkFactory.createChainScenario(
                            size, scenarios[scenario]);

            SequentialSimulator sequential =
                    new SequentialSimulator(sequentialNetwork);

            DataParallelSimulator parallel =
                    new DataParallelSimulator(
                            parallelNetwork, workerCount);

            verifyStates(
                    sequentialNetwork, parallelNetwork,
                    names[scenario], 0);

            for (int step = 1; step <= steps; step++) {
                int sequentialNew = sequential.runStep();
                int parallelNew = parallel.runStep();

                if (sequentialNew != parallelNew) {
                    throw new AssertionError(
                            names[scenario] + ", step " + step
                            + ": new-compromise counts differ.");
                }

                if (sequential.getCompromisedCount()
                        != parallel.getCompromisedCount()) {
                    throw new AssertionError(
                            names[scenario] + ", step " + step
                            + ": total counts differ.");
                }

                verifyStates(
                        sequentialNetwork, parallelNetwork,
                        names[scenario], step);
            }

            int sequentialTotal = sequential.getCompromisedCount();
            int parallelTotal = parallel.getCompromisedCount();

            if (sequentialTotal != expectedTotals[scenario]
                    || parallelTotal != expectedTotals[scenario]) {
                throw new AssertionError(
                        "Unexpected final total for " + names[scenario]);
            }

            System.out.printf(
                    "%-22s %12d %12d %12s%n",
                    names[scenario], sequentialTotal,
                    parallelTotal, "PASS");
        }

        System.out.println(
                "\nAll data-parallel states and counts match at every step.");
    }
}