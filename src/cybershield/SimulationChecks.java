package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class SimulationChecks {

    private static Network createNetwork() {
        Network network = new Network(3);

        network.connect(1, 2);
        network.connect(2, 3);

        network.getComputer(1).compromise();

        return network;
    }

    private static void checkBlockedSpread(
            Network network, String checkName) {

        SequentialSimulator simulator =
                new SequentialSimulator(network);

        int firstStep = simulator.runStep();
        int secondStep = simulator.runStep();

        boolean passed = firstStep == 0
                && secondStep == 0
                && simulator.getCompromisedCount() == 1
                && network.getComputer(1).isCompromised()
                && !network.getComputer(2).isCompromised()
                && !network.getComputer(3).isCompromised();

        if (!passed) {
            throw new AssertionError("FAIL: " + checkName);
        }

        System.out.println("PASS: " + checkName);
    }

    private static void checkOneConnectionPerStep() {
        Network network = createNetwork();
        SequentialSimulator simulator =
                new SequentialSimulator(network);

        // Before propagation, only Computer 1 is compromised.
        boolean initialCorrect =
                simulator.getCompromisedCount() == 1
                && network.getComputer(1).isCompromised()
                && !network.getComputer(2).isCompromised()
                && !network.getComputer(3).isCompromised();

        if (!initialCorrect) {
            throw new AssertionError(
                    "FAIL: Incorrect initial propagation state");
        }

        // Computer 2 becomes compromised, but cannot spread yet.
        int firstStep = simulator.runStep();

        boolean firstStepCorrect = firstStep == 1
                && simulator.getCompromisedCount() == 2
                && network.getComputer(1).isCompromised()
                && network.getComputer(2).isCompromised()
                && !network.getComputer(3).isCompromised();

        if (!firstStepCorrect) {
            throw new AssertionError(
                    "FAIL: Step 1 must compromise only Computer 2");
        }

        // Computer 2 can now spread to Computer 3.
        int secondStep = simulator.runStep();

        boolean secondStepCorrect = secondStep == 1
                && simulator.getCompromisedCount() == 3
                && network.getComputer(1).isCompromised()
                && network.getComputer(2).isCompromised()
                && network.getComputer(3).isCompromised();

        if (!secondStepCorrect) {
            throw new AssertionError(
                    "FAIL: Step 2 must compromise Computer 3");
        }

        // Once all computers are compromised, no new ones are counted.
        int thirdStep = simulator.runStep();

        boolean thirdStepCorrect = thirdStep == 0
                && simulator.getCompromisedCount() == 3
                && network.getComputer(1).isCompromised()
                && network.getComputer(2).isCompromised()
                && network.getComputer(3).isCompromised();

        if (!thirdStepCorrect) {
            throw new AssertionError(
                    "FAIL: Completed spread must preserve all states");
        }

        System.out.println(
                "PASS: Attack spreads one connection per step");
    }

    public static void main(String[] args) {
        System.out.println("CyberShield - Containment Checks");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");
        System.out.println();

        Network isolatedSource = createNetwork();
        isolatedSource.getComputer(1).isolate();
        checkBlockedSpread(
                isolatedSource, "Isolated source cannot spread");

        Network isolatedTarget = createNetwork();
        isolatedTarget.getComputer(2).isolate();
        checkBlockedSpread(
                isolatedTarget, "Isolated target cannot receive");

        Network protectedTarget = createNetwork();
        protectedTarget.getComputer(2).protect();
        checkBlockedSpread(
                protectedTarget, "Protected target blocks compromise");

        checkOneConnectionPerStep();

        System.out.println("\nAll 4 containment checks passed.");
    }
}