# Sequential and Threaded State Comparison

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Work Completed By

Muhammad Hamza Naseer — threaded scenario verification.

## Purpose

Matching compromised totals does not guarantee matching computer states.
Two executions could compromise different computers while reporting
the same total.

ParallelComparison checks both totals and individual computer states.

## Scenarios

The program uses the existing five-computer network from NetworkFactory.
Each scenario runs for three simulation steps.

| Scenario | Expected compromised total |
|---|---:|
| No defence | 5 |
| Isolate Computer 1 | 1 |
| Protect Computer 4 | 3 |

## Execution Model

The sequential baseline runs each scenario directly on the main thread.

The threaded execution creates fresh networks and assigns independent
scenario tasks to worker threads.

ScenarioWorker uses cyclic assignment: a worker processes task indices
starting at its worker index and increasing by the worker count.

Each task owns a separate network. Computers within one scenario are
still processed sequentially.

This is task parallelism across independent scenarios.

## State Comparison

For every computer, the program compares:

- Computer ID.
- Compromised state.
- Isolation flag.
- Protection flag.

The program also compares each scenario's final compromised total.

A state mismatch reports the scenario, computer ID, and both computer
representations.

All scenarios are compared before the program reports an overall
result mismatch using AssertionError.

## Completion and Worker Failures

The main thread joins every worker before reading results.

Thread.join ensures that completed worker actions are visible to the
main thread after the join returns successfully.

Each worker has a separate failure slot populated by its uncaught
exception handler. After joining, the main thread checks these slots.

A worker failure produces an IllegalStateException with the original
failure as its cause.

## Compile

From the project directory:

```powershell
javac -d out src\cybershield\*.java
```

## Run

After successful compilation:

```powershell
java -cp out cybershield.ParallelComparison 1
java -cp out cybershield.ParallelComparison 2
java -cp out cybershield.ParallelComparison 3
```

The default worker count is three when no argument is supplied.

## Expected Successful Results

Each supported worker count should produce the following results:

| Scenario | Sequential | Threaded | Totals | States |
|---|---:|---:|---|---|
| No defence | 5 | 5 | YES | YES |
| Isolate Computer 1 | 1 | 1 | YES | YES |
| Protect Computer 4 | 3 | 3 | YES | YES |

Successful execution ends with:

```text
All scenario totals and computer states match.
```

## Connection to the Course

This demonstrates:

- Runnable tasks and Java threads.
- Task parallelism.
- Static cyclic work assignment.
- Waiting for worker completion using join.
- Comparing parallel results with a sequential baseline.

## Limitations

The program compares final states, not states after every step.

Matching executions do not prove correctness for every possible
network, input or thread schedule.

These runs do not measure speedup or efficiency.