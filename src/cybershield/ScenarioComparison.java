package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ScenarioComparison {

    private static Network createNetwork() {
        Network network = new Network(5);

        network.connect(1, 2);
        network.connect(1, 3);
        network.connect(2, 4);
        network.connect(3, 4);
        network.connect(4, 5);

        network.getComputer(1).compromise();

        return network;
    }

    private static void runScenario(
            String name, Network network, int steps) {

        SequentialSimulator simulator =
                new SequentialSimulator(network);

        for (int step = 0; step < steps; step++) {
            simulator.runStep();
        }

        int isolatedCount = 0;
        int protectedCount = 0;

        for (int id = 1; id <= network.getSize(); id++) {
            ComputerNode computer = network.getComputer(id);

            if (computer.isIsolated()) {
                isolatedCount++;
            }

            if (computer.isProtected()) {
                protectedCount++;
            }
        }

        System.out.printf(
                "%-22s %12d %10d %11d%n",
                name,
                simulator.getCompromisedCount(),
                isolatedCount,
                protectedCount);
    }

    public static void main(String[] args) {
        System.out.println("CyberShield - Scenario Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        int steps = 3;

        Network baseline =
            NetworkFactory.createScenario(NetworkFactory.NO_DEFENCE);

        Network isolation =
            NetworkFactory.createScenario(NetworkFactory.ISOLATION);

        Network protection =
            NetworkFactory.createScenario(NetworkFactory.PROTECTION);

        System.out.println("\nComputers: 5 | Steps per scenario: " + steps);
        System.out.println("Initially compromised computer: 1");
        System.out.println("Defences applied before the first step.\n");

        System.out.printf(
                "%-22s %12s %10s %11s%n",
                "Scenario", "Compromised", "Isolated", "Protected");

        runScenario("No defence", baseline, steps);
        runScenario("Isolate Computer 1", isolation, steps);
        runScenario("Protect Computer 4", protection, steps);
    }
}