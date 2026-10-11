package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ScenarioTask implements Runnable {
    private final Network network;
    private final int steps;
    private int compromisedCount = -1;

    public ScenarioTask(Network network, int steps) {
        this.network = network;
        this.steps = steps;
    }

    @Override
    public void run() {
        SequentialSimulator simulator =
                new SequentialSimulator(network);

        for (int step = 0; step < steps; step++) {
            simulator.runStep();
        }

        compromisedCount = simulator.getCompromisedCount();
    }

    public int getCompromisedCount() {
        if (compromisedCount == -1) {
            throw new IllegalStateException(
                    "The task has not completed successfully.");
        }

        return compromisedCount;
    }
}