# Data Parallelism with Reusable Workers

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Work Completed By

Muhammad Hamza Naseer — reusable worker execution and comparison
against the sequential simulator.

## Purpose

The original DataParallelSimulator creates new threads for every step.

PooledDataParallelSimulator uses a fixed thread pool that remains
available across steps. This avoids repeatedly creating a full set
of worker threads during normal execution.

A separate pool belongs to each simulator instance.

## Implementation Files

- `src/cybershield/PooledDataParallelSimulator.java`
- `src/cybershield/PooledParallelComparison.java`

## Work Assignment

Every simulation step creates one target-range task per configured worker.

For network size N, worker count W and task index i:

- Start, inclusive: floor(i × N / W).
- End, exclusive: floor((i + 1) × N / W).

The ranges cover all computers without overlap.

For seven computers and three range tasks:

| Task index | Computer IDs | Count |
|---|---|---:|
| 0 | 1–2 | 2 |
| 1 | 3–4 | 2 |
| 2 | 5–7 | 3 |

The executor assigns these tasks to available pool threads.
A range is not permanently attached to a particular thread.

## Simulation Step

1. Create a fresh pending-compromise array.
2. Submit target-range tasks using invokeAll.
3. Wait for all tasks to finish.
4. Read each Future to detect task failures.
5. Apply pending compromises on the calling thread.

Workers read current computer states and write separate entries in
the pending array.

Computer states are changed only after all tasks complete successfully.

Newly compromised computers therefore spread only in the following step.

## Thread Reuse

The executor is created once in the simulator constructor.

Pool threads are created as needed and reused for later tasks.
New task objects and a new pending array are still created each step.

Thread reuse reduces one source of overhead but does not eliminate
task submission, allocation, synchronization or computation costs.

## Synchronization

runStep is synchronized to prevent overlapping calls on the same
simulator instance.

Future completion provides the synchronization needed before the
calling thread reads task results and applies pending changes.

External code must not modify the network during execution.
Concurrent simulator instances operating on the same network are
not supported.

## Failure Handling

If a task fails, invokeAll waits for the task batch to complete.
Reading its Future reports the failure.

The simulator throws IllegalStateException with the original cause.
Pending changes from that step are not applied.

## Interruption Handling

If the calling thread is interrupted while awaiting results, the
simulator shuts down the pool, requests cancellation and waits for
termination before returning control.

Workers check their interrupt status between target computers.

Pending changes are discarded, the caller's interrupt status is
restored, and InterruptedException is thrown.

That simulator is closed and cannot execute further steps.

## Resource Lifecycle

The simulator implements AutoCloseable.

Use try-with-resources so its pool is shut down after execution:

```java
try (PooledDataParallelSimulator simulator =
        new PooledDataParallelSimulator(network, workers)) {
    simulator.runStep();
}
```

close shuts down the executor and waits for termination.

Calling runStep after closure throws IllegalStateException.

The comparison program uses a separate try-with-resources block
for each scenario.

## Verification Program

PooledParallelComparison uses:

| Setting | Value |
|---|---:|
| Computers | 7 |
| Steps | 8 |
| Default workers | 3 |
| Accepted workers | 1–7 |
| Topologies | Chain, ring, star |
| Scenarios per topology | 3 |

It compares initial states and, after every step:

- Newly compromised counts.
- Total compromised counts.
- Computer IDs.
- Compromised, isolated and protected flags.

It also checks known final totals.

## Expected Final Totals

| Topology | No defence | Isolate Computer 1 | Protect Computer 4 |
|---|---:|---:|---:|
| Chain | 7 | 1 | 3 |
| Ring | 7 | 1 | 6 |
| Star | 7 | 1 | 6 |

Eight steps include steps after spread has finished.

## Compile and Run

From the project directory:

```powershell
javac -d out src\cybershield\*.java
```

After successful compilation:

```powershell
java -cp out cybershield.PooledParallelComparison 1
java -cp out cybershield.PooledParallelComparison 3
```

Both runs should end with:

```text
All 9 pooled scenario checks passed.
All simulation pools closed.
```

The three-worker run checks uneven target partitioning.

## Connection to the Course

This demonstrates Java concurrency libraries, task submission,
data parallelism, synchronization, worker lifecycle management and
parallelism overhead.

## Integration Status

The pooled simulator is currently exercised by PooledParallelComparison.

SimulationRunner still uses the original DataParallelSimulator.
Pooled execution has not yet been integrated into that runner.

## Limitations

- No performance improvement has been measured yet.
- The comparison uses small, fixed scenarios.
- Normal execution and closure are checked by the demonstration.
- Dedicated injected-failure and interruption checks are not included.
- Worker tasks still allocate fresh task objects and pending arrays per step.
- Network connections still use an adjacency matrix.