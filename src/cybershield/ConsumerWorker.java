package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ConsumerWorker implements Runnable {
    private final BlockingTaskQueue queue;

    public ConsumerWorker(BlockingTaskQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            ScenarioTask task = queue.take();

            while (task != null) {
                // Simulation runs outside the queue's lock.
                task.run();
                task = queue.take();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}