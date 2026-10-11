# Scheduling Benchmark

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Compare sequential execution, static task assignment, and a
synchronized shared task queue on the same workload.

## Configuration
Arguments: [size steps [workers]]
Defaults: 500 computers, 499 steps, and 2 workers.

Supported worker counts are 1, 2, and 3.
Both threaded modes use the selected worker count.

## Workload
Each mode runs three scenarios on freshly created chain networks:
- No defence.
- Isolation of Computer 1.
- Protection of the middle computer.

## Execution Modes
Sequential executes every task on the main thread.

Static uses ScenarioWorker to assign tasks by worker index.

Queued uses QueueWorker and one shared TaskQueue.
Workers request another task after completing their current task.

## Measurement Procedure
- Three warm-up runs per mode.
- Six measured runs per mode.
- Rotate execution order across rounds.
- During measured rounds, each mode occupies each execution
  position twice.
- Compare final compromised totals after every round.

Timings include network and task construction, execution, and
result collection.

Threaded timings additionally include worker and thread creation,
startup, joining, and applicable queue overhead.

Console output and correctness comparisons are excluded.

## Metrics
Speedup = average sequential time / average selected-mode time.
Efficiency (%) = speedup / worker count * 100.

Both threaded modes use the same sequential average as their baseline.

## Interpretation
Static assignment with two workers places both heavier scenarios
on one worker.

The queue allows an available worker to take the next task.
Actual assignments depend on thread scheduling.

A queue may improve work distribution, but speedup is not guaranteed.
There are only three tasks, and running tasks cannot be split
or reassigned by this queue.

## Limitations
Warm-up and repeated measurements reduce some measurement noise
but do not eliminate it.

Results depend on hardware, runtime behaviour, and system activity.
Only final compromised totals are compared for correctness.

## Recorded Results — 11 October 2026
Configuration: 500 computers, 499 steps, and 2 workers.
Hardware and runtime: see benchmark-environment.md.

All scenario totals matched in every run.

| Run | First mode | Sequential ms | Static ms | Queued ms |
| --- | --- | --- | --- | --- |
| 1 | Sequential | 79.079 | 83.356 | 48.258 |
| 2 | Static | 81.451 | 87.938 | 46.201 |
| 3 | Queued | 81.408 | 79.648 | 44.029 |
| 4 | Sequential | 129.572 | 84.290 | 44.459 |
| 5 | Static | 79.839 | 78.087 | 43.480 |
| 6 | Queued | 80.191 | 76.828 | 43.036 |

| Mode | Average ms | Speedup | Efficiency |
| --- | --- | --- | --- |
| Sequential | 88.590 | Baseline | Not applicable |
| Static | 81.691 | 1.08x | 54.22% |
| Queued | 44.910 | 1.97x | 98.63% |

### Interpretation
Queued execution took approximately 45% less time than static
assignment in this session.

This is consistent with improved work distribution, although
individual worker activity was not measured.

Sequential run 4 was noticeably slower than the other sequential
runs. Its cause was not established. It is retained in the results
and raises the sequential average and calculated speedups.

Efficiency is a scaling metric, not CPU utilization.
These results do not guarantee the same performance in future runs.