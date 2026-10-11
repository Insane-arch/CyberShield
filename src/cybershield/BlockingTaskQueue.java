package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class BlockingTaskQueue {
    private final ScenarioTask[] buffer;

    private int head;
    private int tail;
    private int count;
    private boolean closed;

    public BlockingTaskQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(
                    "Queue capacity must be at least 1.");
        }

        buffer = new ScenarioTask[capacity];
    }

    public synchronized void put(ScenarioTask task)
            throws InterruptedException {

        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        while (count == buffer.length && !closed) {
            wait();
        }

        if (closed) {
            throw new IllegalStateException("Queue is closed.");
        }

        buffer[tail] = task;
        tail = (tail + 1) % buffer.length;
        count++;

        notifyAll();
    }

    public synchronized ScenarioTask take()
            throws InterruptedException {

        while (count == 0 && !closed) {
            wait();
        }

        // An empty, closed queue means no more tasks will arrive.
        if (count == 0) {
            return null;
        }

        ScenarioTask task = buffer[head];
        buffer[head] = null;

        head = (head + 1) % buffer.length;
        count--;

        notifyAll();

        return task;
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }
}