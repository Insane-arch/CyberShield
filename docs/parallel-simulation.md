# Thread-Based Scenario Execution

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Execute independent scenarios using Java Runnable and Thread,
then compare their compromised totals with sequential execution.

## Task Design
ScenarioTask implements Runnable.
Each task owns a separate network and runs a fixed number of steps.
The task stores its final compromised count.

Each task is executed once.

## Sequential Execution
Calling task.run() directly executes the task on the main thread.

## Threaded Execution
ParallelComparison accepts an optional worker count from 1 to 3.
The default is 3.

ScenarioWorker implements Runnable and assigns tasks using:
taskIndex = workerIndex, increasing by workerCount each time.

Assignments using zero-based indexes:
- One worker: worker 0 executes tasks 0, 1, and 2.
- Two workers: worker 0 executes tasks 0 and 2; worker 1 executes task 1.
- Three workers: each worker executes one task.

Workers share the task array for reading, but each task is executed
by exactly one worker and owns a separate network.

All workers start before the main thread joins them.

One worker still uses a separate Thread, so it differs from the
sequential baseline, which runs directly on the main thread.

This static assignment does not guarantee balanced workloads.
Worker count changes are not yet integrated into PerformanceBenchmark.

## Coordination
Thread.join() waits for a worker to terminate.
After a successful join, the main thread can observe that worker's
completed writes, including its stored result.

Networks and simulation state are not shared between workers.
Only the main thread prints results, after joining the workers.

## Course Concepts
- Runnable interface.
- Thread creation and lifecycle.
- Task parallelism.
- Coordination using join().
- Avoiding races through independent mutable state.

## Expected Results
| Scenario | Sequential total | Threaded total |
| --- | --- | --- |
| No defence | 5 | 5 |
| Isolate Computer 1 | 1 | 1 |
| Protect Computer 4 | 3 | 3 |

The comparison checks final compromised totals for these scenarios.
It does not yet compare every intermediate state.

## Performance Limitation
This small demonstration establishes threaded execution and
checks results. It does not establish a speed improvement.
Thread creation overhead may outweigh the work for tiny networks.