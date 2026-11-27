package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class TargetSequentialSimulator {
    private final Network network;

    public TargetSequentialSimulator(Network network) {
        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null.");
        }

        this.network = network;
    }

    // External code must not modify the network during execution.
    public int runStep() {
        int size = network.getSize();
        boolean[] pending = new boolean[size];

        for (int targetId = 1; targetId <= size; targetId++) {
            ComputerNode target = network.getComputer(targetId);

            if (target.isCompromised()
                    || target.isIsolated()
                    || target.isProtected()) {
                continue;
            }

            for (int sourceId = 1; sourceId <= size; sourceId++) {
                ComputerNode source = network.getComputer(sourceId);

                if (source.isCompromised()
                        && !source.isIsolated()
                        && network.areConnected(sourceId, targetId)) {

                    pending[targetId - 1] = true;
                    break;
                }
            }
        }

        int newlyCompromised = 0;

        for (int index = 0; index < size; index++) {
            if (pending[index]) {
                network.getComputer(index + 1).compromise();
                newlyCompromised++;
            }
        }

        return newlyCompromised;
    }

    public int getCompromisedCount() {
        int count = 0;

        for (int id = 1; id <= network.getSize(); id++) {
            if (network.getComputer(id).isCompromised()) {
                count++;
            }
        }

        return count;
    }
}