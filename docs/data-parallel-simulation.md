# Data-Parallel Network Simulation

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Work Completed By

Muhammad Hamza Naseer — parallel processing within one network
and step-by-step comparison against sequential execution.

## Purpose

Earlier threaded modes execute independent scenarios concurrently.

DataParallelSimulator divides the computers within a single network
between worker threads during each simulation step.

This introduces data parallelism within a scenario.

## Implementation Files

- `src/cybershield/DataParallelSimulator.java`
- `src/cybershield/DataParallelComparison.java`

## Work Assignment

Each worker receives a contiguous range of target computers.

For network size N, worker count W and zero-based worker index i:

- Start index, inclusive: floor(i × N / W).
- End index, exclusive: floor((i + 1) × N / W).

These ranges cover every target exactly once without overlap.

For 100 computers and three workers:

| Worker | Computer IDs | Number of computers |
|---|---|---:|
| DataWorker-0 | 1–33 | 33 |
| DataWorker-1 | 34–66 | 33 |
| DataWorker-2 | 67–100 | 34 |

Each worker checks possible compromised sources for its assigned targets.

## Two-Phase Simulation Step

### Phase 1: Calculate Pending Changes

Workers read the existing network state.

A healthy target can become compromised when it is connected to a
compromised source, provided that:

- The target is not isolated.
- The target is not protected.
- The source is not isolated.

Each worker writes only its assigned entries in the pending array.

Workers do not modify ComputerNode states during this phase.

### Phase 2: Apply Changes

The calling thread joins all workers.

After successful completion, it applies pending compromises and counts
the newly compromised computers.

A computer compromised during this step can spread only in the next step.

## Shared State and Synchronization

Workers share the network for reading but have separate pending-array
entries to write.

Joining workers occurs before pending results are read and before
computer states are changed.

The simulator's runStep method is synchronized to prevent overlapping
step calls on the same simulator instance.

External code must not modify the network while the simulator is running.
The simulator does not enforce this restriction through the Network API.

Using separate simulator instances on the same network concurrently
is not supported.

## Failure and Interruption Handling

Each worker has an uncaught-failure slot.

If a worker fails, the calling thread waits for started workers to finish
and reports the failure before applying pending changes.

If the calling thread is interrupted while joining, it continues waiting
for started workers, restores its interrupt flag and throws
InterruptedException.

Pending changes are discarded in that interrupted execution.

This implementation waits for worker computation to finish rather than
actively cancelling it.

## Comparison Program

DataParallelComparison uses:

| Setting | Value |
|---|---:|
| Computers | 100 |
| Simulation steps | 99 |
| Default workers | 3 |
| Accepted workers | 1–100 |

Each scenario creates separate sequential and parallel networks.

At the initial state, individual computer states are compared.

After every simulation step, the program compares:

- Newly compromised counts.
- Total compromised counts.
- Computer IDs.
- Compromised states.
- Isolation flags.
- Protection flags.

Final totals are also checked against known expected values.

## Scenarios

| Scenario | Expected final compromised total |
|---|---:|
| No defence | 100 |
| Isolate Computer 1 | 1 |
| Protect Computer 51 | 50 |

## Compile and Run

From the project directory:

```powershell
javac -d out src\cybershield\*.java
```

After successful compilation:

```powershell
java -cp out cybershield.DataParallelComparison 1
java -cp out cybershield.DataParallelComparison 2
java -cp out cybershield.DataParallelComparison 3
```

## Expected Successful Results

Each run should report totals 100, 1 and 50, with PASS for every scenario.

Successful execution ends with:

```text
All data-parallel states and counts match at every step.
```

The three-worker run also checks an uneven division of 100 computers.

## Connection to the Course

This demonstrates:

- Data parallelism.
- Java threads.
- Partitioning work into disjoint ranges.
- Synchronization and worker completion.
- Data dependence between simulation steps.
- Parallel-program correctness checks.

## Performance Limitations

New threads are created during every simulation step.

Thread creation and joining introduce overhead, especially for small
networks. This implementation has not yet demonstrated a speedup.

The sequential implementation scans sources first, while the parallel
implementation scans targets first and stops at the first eligible source.
A future benchmark must account for this algorithmic difference.

These checks verify the selected scenarios and worker counts.
They do not prove correctness for every possible network or thread schedule.