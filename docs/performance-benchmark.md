# Performance Benchmark

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Compare sequential execution with configurable worker counts,
using warm-up runs and repeated measurements.

## Configuration
Command arguments: [size steps [workers]]

Defaults:
- 100 computers.
- 99 simulation steps.
- 3 worker threads.

Supported worker counts: 1, 2, and 3.
Network size must be at least 3 and steps cannot be negative.

## Workload
Each execution processes three chain-network scenarios:
- No defence.
- Isolation of Computer 1.
- Protection of the middle computer.

Each execution creates fresh networks.
Both execution modes use identical simulation settings.

## Worker Assignment
ScenarioWorker uses static cyclic assignment.

With two workers, worker 0 handles no defence and protection.
Worker 1 handles isolation.

With one worker, all scenarios execute on one background thread.
The sequential baseline executes directly on the main thread.

## Procedure
1. Perform three warm-up runs per mode.
2. Perform six measured runs per mode.
3. Alternate which mode executes first.
4. Compare final compromised totals after every pair.
5. Report individual times, averages, speedup, and efficiency.

Warm-up timings are excluded from the averages.

## Timing Boundaries
System.nanoTime() measures elapsed time.

Both modes include network and task construction, execution,
and result collection.

Threaded execution also includes worker and thread construction,
startup, and joining.

Result comparison and console output are outside timed sections.

## Calculations
Average time = total measured time / measured run count.
Speedup = average sequential time / average threaded time.
Efficiency (%) = speedup / worker count * 100.

Efficiency is a parallel scaling measure, not CPU utilization.

## Limitations
Three warm-up runs do not guarantee settled JVM performance.
Background activity, scheduling, garbage collection, and hardware
can affect measurements.

There are only three tasks, and they have unequal workloads.
Static assignment can leave some workers idle while another is busy.
Two workers are therefore not guaranteed to outperform one.

Only final compromised totals are compared for correctness.
Earlier fixed-three-worker measurements remain historical results.