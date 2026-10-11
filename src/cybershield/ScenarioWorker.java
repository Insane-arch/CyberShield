package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ScenarioWorker implements Runnable {
    private final ScenarioTask[] tasks;
    private final int workerIndex;
    private final int workerCount;

    public ScenarioWorker(
            ScenarioTask[] tasks, int workerIndex, int workerCount) {

        if (workerCount < 1
                || workerIndex < 0
                || workerIndex >= workerCount) {
            throw new IllegalArgumentException(
                    "Invalid worker configuration.");
        }

        this.tasks = tasks;
        this.workerIndex = workerIndex;
        this.workerCount = workerCount;
    }

    @Override
    public void run() {
        for (int taskIndex = workerIndex;
                taskIndex < tasks.length;
                taskIndex += workerCount) {

            tasks[taskIndex].run();
        }
    }
}