package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class Network {
    private final ComputerNode[] computers;
    private final boolean[][] connections;

    public Network(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Network size must be greater than zero.");
        }

        computers = new ComputerNode[size];
        connections = new boolean[size][size];

        for (int i = 0; i < size; i++) {
            computers[i] = new ComputerNode(i + 1);
        }
    }

    public int getSize() {
        return computers.length;
    }

    private void validateId(int id) {
        if (id < 1 || id > computers.length) {
            throw new IllegalArgumentException(
                    "Invalid computer ID: " + id);
        }
    }

    public ComputerNode getComputer(int id) {
        validateId(id);
        return computers[id - 1];
    }

    public void connect(int firstId, int secondId) {
        validateId(firstId);
        validateId(secondId);

        if (firstId == secondId) {
            throw new IllegalArgumentException(
                    "A computer cannot connect to itself.");
        }

        connections[firstId - 1][secondId - 1] = true;
        connections[secondId - 1][firstId - 1] = true;
    }

    public boolean areConnected(int firstId, int secondId) {
        validateId(firstId);
        validateId(secondId);

        return connections[firstId - 1][secondId - 1];
    }
}