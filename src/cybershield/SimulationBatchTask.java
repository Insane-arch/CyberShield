package cybershield;

import java.util.concurrent.RecursiveAction;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class SimulationBatchTask extends RecursiveAction {
    private static final long serialVersionUID = 1L;

    private final ScenarioTask[] tasks;
    private final int start;
    private final int end;
    private final int threshold;

    public SimulationBatchTask(
            ScenarioTask[] tasks, int start, int end, int threshold) {

        if (tasks == null
                || start < 0
                || end < start
                || end > tasks.length
                || threshold < 1) {
            throw new IllegalArgumentException(
                    "Invalid batch range or threshold.");
        }

        this.tasks = tasks;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
    }

    @Override
    protected void compute() {
        int taskCount = end - start;

        if (taskCount <= threshold) {
            for (int i = start; i < end; i++) {
                tasks[i].run();
            }
            return;
        }

        int middle = start + taskCount / 2;

        SimulationBatchTask left = new SimulationBatchTask(
                tasks, start, middle, threshold);

        SimulationBatchTask right = new SimulationBatchTask(
                tasks, middle, end, threshold);

        invokeAll(left, right);
    }
}