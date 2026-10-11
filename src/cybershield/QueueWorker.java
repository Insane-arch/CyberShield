package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class QueueWorker implements Runnable {
    private final TaskQueue queue;

    public QueueWorker(TaskQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        ScenarioTask task = queue.poll();

        while (task != null) {
            task.run();
            task = queue.poll();
        }
    }
}