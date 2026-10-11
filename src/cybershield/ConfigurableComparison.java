package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ConfigurableComparison {

    public static void main(String[] args) {
        int size = 6;
        int steps = 5;

        if (args.length != 0 && args.length != 2) {
            System.out.println(
                    "Usage: java -cp out cybershield.ConfigurableComparison"
                    + " [size steps]");
            return;
        }

        if (args.length == 2) {
            try {
                size = Integer.parseInt(args[0]);
                steps = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                System.out.println(
                        "Network size and steps must be whole numbers"
                        + " within the Java int range.");
                return;
            }
        }

        if (size < 3 || steps < 0) {
            System.out.println(
                    "Network size must be at least 3."
                    + " Steps must be zero or greater.");
            return;
        }

        System.out.println("CyberShield - Configurable Comparison");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        System.out.println("\nNetwork type: Chain");
        System.out.println("Computers: " + size);
        System.out.println("Simulation steps: " + steps);
        System.out.println("Initially compromised: Computer 1");
        System.out.println(
                "Protection target: Computer " + (size / 2 + 1));

        String[] names = {
            "No defence",
            "Isolate Computer 1",
            "Protect middle computer"
        };

        int[] scenarios = {
            NetworkFactory.NO_DEFENCE,
            NetworkFactory.ISOLATION,
            NetworkFactory.PROTECTION
        };

        System.out.printf(
                "%n%-26s %12s%n", "Scenario", "Compromised");

        for (int i = 0; i < scenarios.length; i++) {
            Network network =
                    NetworkFactory.createChainScenario(size, scenarios[i]);

            ScenarioTask task = new ScenarioTask(network, steps);

            // Direct call: this comparison is sequential.
            task.run();

            System.out.printf(
                    "%-26s %12d%n",
                    names[i],
                    task.getCompromisedCount());
        }
    }
}