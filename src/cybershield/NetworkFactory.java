package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class NetworkFactory {
    public static final int NO_DEFENCE = 0;
    public static final int ISOLATION = 1;
    public static final int PROTECTION = 2;

    public static Network createScenario(int scenario) {
        validateScenario(scenario);

        Network network = new Network(5);

        network.connect(1, 2);
        network.connect(1, 3);
        network.connect(2, 4);
        network.connect(3, 4);
        network.connect(4, 5);

        applyScenario(network, scenario, 4);
        return network;
    }

    public static Network createChainScenario(int size, int scenario) {
        validateSize(size, "Chain");
        validateScenario(scenario);

        Network network = new Network(size);

        for (int id = 1; id < size; id++) {
            network.connect(id, id + 1);
        }

        applyScenario(network, scenario, size / 2 + 1);
        return network;
    }

    public static Network createRingScenario(int size, int scenario) {
        validateSize(size, "Ring");
        validateScenario(scenario);

        Network network = new Network(size);

        for (int id = 1; id < size; id++) {
            network.connect(id, id + 1);
        }

        // Close the chain into a ring.
        network.connect(size, 1);

        applyScenario(network, scenario, size / 2 + 1);
        return network;
    }

    public static Network createStarScenario(int size, int scenario) {
        validateSize(size, "Star");
        validateScenario(scenario);

        Network network = new Network(size);

        // Computer 1 is the hub. All other computers are leaves.
        for (int id = 2; id <= size; id++) {
            network.connect(1, id);
        }

        applyScenario(network, scenario, size / 2 + 1);
        return network;
    }

    private static void validateSize(int size, String topology) {
        if (size < 3) {
            throw new IllegalArgumentException(
                    topology + " network must contain at least 3 computers.");
        }
    }

    private static void validateScenario(int scenario) {
        if (scenario < NO_DEFENCE || scenario > PROTECTION) {
            throw new IllegalArgumentException(
                    "Invalid scenario: " + scenario);
        }
    }

    private static void applyScenario(
            Network network, int scenario, int protectedId) {

        network.getComputer(1).compromise();

        if (scenario == ISOLATION) {
            network.getComputer(1).isolate();
        } else if (scenario == PROTECTION) {
            network.getComputer(protectedId).protect();
        }
    }
}