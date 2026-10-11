package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class TaskQueue {
    private final ScenarioTask[] tasks;
    private int nextIndex;

    public TaskQueue(ScenarioTask[] tasks) {
        this.tasks = tasks.clone();
        this.nextIndex = 0;
    }

    public synchronized ScenarioTask poll() {
        if (nextIndex >= tasks.length) {
            return null;
        }

        ScenarioTask task = tasks[nextIndex];
        nextIndex++;

        return task;
    }
}