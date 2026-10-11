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

        System.out.println("\nAll 3 containment checks passed.");
    }
}