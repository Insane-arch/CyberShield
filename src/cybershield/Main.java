package cybershield;

public class Main {
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("             CyberShield");
        System.out.println(" Network Attack Containment Simulator");
        System.out.println("======================================");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        Network network = new Network(5);

        network.connect(1, 2);
        network.connect(1, 3);
        network.connect(2, 4);
        network.connect(3, 4);
        network.connect(4, 5);

        // Start the simulation with Computer 1 compromised.
        network.getComputer(1).compromise();

        SequentialSimulator simulator =
                new SequentialSimulator(network);

        System.out.println("\nInitial compromised computers: "
                + simulator.getCompromisedCount());

        for (int step = 1; step <= 3; step++) {
            int newCases = simulator.runStep();

            System.out.println("Step " + step
                    + " | Newly compromised: " + newCases
                    + " | Total compromised: "
                    + simulator.getCompromisedCount());
        }

        System.out.println("\nFinal computer states:");

        for (int id = 1; id <= network.getSize(); id++) {
            System.out.println(network.getComputer(id));
        }
    }
}