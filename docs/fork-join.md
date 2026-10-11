# Fork/Join Simulation Batch

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Execute independent simulations using the Java Fork/Join framework.

## Workload
Twelve tasks repeat the three existing scenarios four times.
Every task owns a fresh 100-computer chain and runs 99 steps.

These are repetitions of deterministic scenarios, not independent
random trials.

## SimulationBatchTask
The class extends RecursiveAction.

Each batch covers task indexes from start inclusive to end exclusive.
A batch with at most two tasks executes them directly.
Larger batches divide into two non-overlapping subranges.
invokeAll() schedules both subranges and waits for completion.

The threshold controls subdivision, not the number of threads.

## ForkJoinPool
The demonstration uses a dedicated pool with parallelism 3.
The pool manages worker scheduling.
pool.invoke() waits for the root batch to complete.
The pool is shut down after execution.

## Data Ownership
Subtasks share the array of task references but process disjoint ranges.
Each ScenarioTask owns its network and result.
Results are read after the root batch completes.

## Verification
Run an equivalent batch sequentially using separate networks.
Compare final compromised totals for all twelve tasks.

Expected totals repeat:
100, 1, 50.

Every task should match its sequential counterpart.

## Course Concepts
- Fork/Join framework.
- Recursive task decomposition.
- Task parallelism.
- Completion coordination.
- Independent mutable state.

## Limitations
This demonstration checks final totals, not intermediate states.
It does not measure performance.
Equal-sized task groups do not necessarily have equal computational cost.
Existing benchmarks are unchanged.