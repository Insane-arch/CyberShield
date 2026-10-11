package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class NetworkFactory {
    public static final int NO_DEFENCE = 0;
    public static final int ISOLATION = 1;
    public static final int PROTECTION = 2;

    public static Network createScenario(int scenario) {
        if (scenario < NO_DEFENCE || scenario > PROTECTION) {
            throw new IllegalArgumentException(
                    "Invalid scenario: " + scenario);
        }

        Network network = new Network(5);

        network.connect(1, 2);
        network.connect(1, 3);
        network.connect(2, 4);
        network.connect(3, 4);
        network.connect(4, 5);

        network.getComputer(1).compromise();

        if (scenario == ISOLATION) {
            network.getComputer(1).isolate();
        } else if (scenario == PROTECTION) {
            network.getComputer(4).protect();
        }

        return network;
    }
}