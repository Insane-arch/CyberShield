package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class DataParallelSimulator {
    private final Network network;
    private final int workerCount;

    public DataParallelSimulator(Network network, int workerCount) {
        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null.");
        }

        if (workerCount < 1 || workerCount > network.getSize()) {
            throw new IllegalArgumentException(
                    "Workers must be between 1 and the network size.");
        }

        this.network = network;
        this.workerCount = workerCount;
    }

    // Only one caller may execute a step on this simulator at a time.
    // External code must not modify the network during execution.
    public synchronized int runStep() throws InterruptedException {
        int size = network.getSize();

        boolean[] pending = new boolean[size];
        Throwable[] failures = new Throwable[workerCount];
        Thread[] workers = new Thread[workerCount];

        for (int i = 0; i < workerCount; i++) {
            final int workerIndex = i;

            // Zero-based start inclusive, end exclusive.
            final int start = (int) ((long) i * size / workerCount);
            final int end =
                    (int) ((long) (i + 1) * size / workerCount);

            workers[i] = new Thread(() -> {
                for (int targetIndex = start;
                        targetIndex < end;
                        targetIndex++) {

                    int targetId = targetIndex + 1;
                    ComputerNode target = network.getComputer(targetId);

                    if (target.isCompromised()
                            || target.isIsolated()
                            || target.isProtected()) {
                        continue;
                    }

                    for (int sourceId = 1;
                            sourceId <= size;
                            sourceId++) {

                        ComputerNode source =
                                network.getComputer(sourceId);

                        if (source.isCompromised()
                                && !source.isIsolated()
                                && network.areConnected(sourceId, targetId)) {

                            // Only this worker writes this array entry.
                            pending[targetIndex] = true;
                            break;
                        }
                    }
                }
            }, "DataWorker-" + i);

            workers[i].setUncaughtExceptionHandler(
                    (thread, failure) ->
                            failures[workerIndex] = failure);
        }

        int started = 0;
        InterruptedException interruption = null;

        try {
            for (Thread worker : workers) {
                worker.start();
                started++;
            }
        } finally {
            // Finish joining even if interrupted, so no workers remain
            // reading the network when this method returns or throws.
            for (int i = 0; i < started; i++) {
                boolean joined = false;

                while (!joined) {
                    try {
                        workers[i].join();
                        joined = true;
                    } catch (InterruptedException exception) {
                        interruption = exception;
                    }
                }
            }

            if (interruption != null) {
                Thread.currentThread().interrupt();
            }
        }

        if (interruption != null) {
            // Pending changes are discarded.
            throw interruption;
        }

        for (int i = 0; i < failures.length; i++) {
            if (failures[i] != null) {
                throw new IllegalStateException(
                        "DataWorker-" + i + " failed.", failures[i]);
            }
        }

        // Every worker has finished reading the current states.
        int newlyCompromised = 0;

        for (int index = 0; index < size; index++) {
            if (pending[index]) {
                network.getComputer(index + 1).compromise();
                newlyCompromised++;
            }
        }

        return newlyCompromised;
    }

    public synchronized int getCompromisedCount() {
        int count = 0;

        for (int id = 1; id <= network.getSize(); id++) {
            if (network.getComputer(id).isCompromised()) {
                count++;
            }
        }

        return count;
    }
}