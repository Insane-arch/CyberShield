package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class SequentialSimulator {
    private final Network network;

    public SequentialSimulator(Network network) {
        this.network = network;
    }

    public int runStep() {
        int size = network.getSize();
        boolean[] pendingCompromises = new boolean[size];

        // Phase 1: decide which computers will become compromised.
        // Do not change computer states during this phase.
        for (int sourceId = 1; sourceId <= size; sourceId++) {
            ComputerNode source = network.getComputer(sourceId);

            if (!source.isCompromised() || source.isIsolated()) {
                continue;
            }

            for (int targetId = 1; targetId <= size; targetId++) {
                ComputerNode target = network.getComputer(targetId);

                if (target.isCompromised()
                        || target.isIsolated()
                        || target.isProtected()) {
                    continue;
                }

                if (network.areConnected(sourceId, targetId)) {
                    pendingCompromises[targetId - 1] = true;
                }
            }
        }

        // Phase 2: apply all changes after checking the network.
        int newlyCompromised = 0;

        for (int id = 1; id <= size; id++) {
            if (pendingCompromises[id - 1]) {
                network.getComputer(id).compromise();
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