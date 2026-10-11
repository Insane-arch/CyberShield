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

        System.out.println("\nComputer states:");

        for (int id = 1; id <= network.getSize(); id++) {
            System.out.println(network.getComputer(id));
        }

        System.out.println("\nConnections: 1 = connected, 0 = no connection");

        System.out.print("    ");
        for (int id = 1; id <= network.getSize(); id++) {
            System.out.printf("%3d", id);
        }
        System.out.println();

        for (int row = 1; row <= network.getSize(); row++) {
            System.out.printf("%3d ", row);

            for (int column = 1; column <= network.getSize(); column++) {
                int value = network.areConnected(row, column) ? 1 : 0;
                System.out.printf("%3d", value);
            }

            System.out.println();
        }
    }
}