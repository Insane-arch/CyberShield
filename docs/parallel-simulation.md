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
Three Thread objects execute three separate ScenarioTask objects.
All threads are started before the main thread joins any worker.

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