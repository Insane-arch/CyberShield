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

        boolean initialCorrect =
                simulator.getCompromisedCount() == 1
                && network.getComputer(1).isCompromised()
                && !network.getComputer(2).isCompromised()
                && !network.getComputer(3).isCompromised();

        if (!initialCorrect) {
            throw new AssertionError(
                    "FAIL: Incorrect initial propagation state");
        }

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

    private static void checkSharedTargetCountedOnce() {
        Network network = new Network(4);

        network.connect(1, 3);
        network.connect(2, 3);

        network.getComputer(1).compromise();
        network.getComputer(2).compromise();

        SequentialSimulator simulator =
                new SequentialSimulator(network);

        int newlyCompromised = simulator.runStep();

        boolean firstStepCorrect = newlyCompromised == 1
                && simulator.getCompromisedCount() == 3
                && network.getComputer(1).isCompromised()
                && network.getComputer(2).isCompromised()
                && network.getComputer(3).isCompromised()
                && !network.getComputer(4).isCompromised();

        if (!firstStepCorrect) {
            throw new AssertionError(
                    "FAIL: Two sources must count their shared target once");
        }

        int nextStep = simulator.runStep();

        boolean nextStepCorrect = nextStep == 0
                && simulator.getCompromisedCount() == 3
                && network.getComputer(1).isCompromised()
                && network.getComputer(2).isCompromised()
                && network.getComputer(3).isCompromised()
                && !network.getComputer(4).isCompromised();

        if (!nextStepCorrect) {
            throw new AssertionError(
                    "FAIL: Shared target must not be counted again");
        }

        System.out.println(
                "PASS: Multiple sources count a shared target only once");
    }

    private static void checkProtectionDoesNotCureSource() {
        Network network = createNetwork();

        // Computer 1 is already compromised before protection is applied.
        ComputerNode source = network.getComputer(1);
        source.protect();

        SequentialSimulator simulator =
                new SequentialSimulator(network);

        if (!source.isCompromised()
                || !source.isProtected()
                || source.isIsolated()) {
            throw new AssertionError(
                    "FAIL: Protection must preserve the compromised source");
        }

        int firstStep = simulator.runStep();

        boolean firstStepCorrect = firstStep == 1
                && simulator.getCompromisedCount() == 2
                && source.isCompromised()
                && source.isProtected()
                && !source.isIsolated()
                && network.getComputer(2).isCompromised()
                && !network.getComputer(3).isCompromised();

        if (!firstStepCorrect) {
            throw new AssertionError(
                    "FAIL: Protected compromised source must still spread");
        }

        int secondStep = simulator.runStep();

        boolean secondStepCorrect = secondStep == 1
                && simulator.getCompromisedCount() == 3
                && source.isCompromised()
                && source.isProtected()
                && !source.isIsolated()
                && network.getComputer(2).isCompromised()
                && network.getComputer(3).isCompromised();

        if (!secondStepCorrect) {
            throw new AssertionError(
                    "FAIL: Protection must not interrupt existing compromise");
        }

        System.out.println(
                "PASS: Protection does not cure a compromised source");
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
        checkSharedTargetCountedOnce();
        checkProtectionDoesNotCureSource();

        System.out.println("\nAll 6 containment checks passed.");
    }
}