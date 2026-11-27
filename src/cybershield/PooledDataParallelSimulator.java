package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PooledDataParallelSimulator implements AutoCloseable {
    private final Network network;
    private final int workerCount;
    private final ExecutorService executor;
    private boolean closed;

    public PooledDataParallelSimulator(
            Network network, int workerCount) {

        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null.");
        }

        if (workerCount < 1 || workerCount > network.getSize()) {
            throw new IllegalArgumentException(
                    "Workers must be between 1 and the network size.");
        }

        this.network = network;
        this.workerCount = workerCount;
        this.executor = Executors.newFixedThreadPool(workerCount);
    }

    // External code must not modify this network during execution.
    public synchronized int runStep() throws InterruptedException {
        if (closed) {
            throw new IllegalStateException("Simulator is closed.");
        }

        int size = network.getSize();
        boolean[] pending = new boolean[size];

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < workerCount; i++) {
            final int start = (int) ((long) i * size / workerCount);
            final int end =
                    (int) ((long) (i + 1) * size / workerCount);

            tasks.add(() -> {
                for (int targetIndex = start;
                        targetIndex < end;
                        targetIndex++) {

                    // Permit cancellation without applying partial results.
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException(
                                "Target evaluation interrupted.");
                    }

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

                            pending[targetIndex] = true;
                            break;
                        }
                    }
                }

                return null;
            });
        }

        try {
            // Wait for every submitted task before applying changes.
            List<Future<Void>> futures = executor.invokeAll(tasks);

            // Surface task failures before mutating the network.
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException exception) {
            // Cancel work and await termination before returning control.
            closed = true;
            executor.shutdownNow();
            awaitTermination();
            Thread.currentThread().interrupt();
            throw exception;
        } catch (ExecutionException exception) {
            // invokeAll has already waited for all tasks.
            // Pending changes are discarded.
            throw new IllegalStateException(
                    "Parallel target evaluation failed.",
                    exception.getCause());
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

    public synchronized int getCompromisedCount() {
        int count = 0;

        for (int id = 1; id <= network.getSize(); id++) {
            if (network.getComputer(id).isCompromised()) {
                count++;
            }
        }

        return count;
    }

    private void awaitTermination() {
        boolean interrupted = false;

        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                interrupted = true;
                executor.shutdownNow();
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        executor.shutdown();
        awaitTermination();
    }
}
