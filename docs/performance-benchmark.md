# Performance Benchmark

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Compare sequential and threaded execution using warm-up and
repeated measurements.

## Workload
Each execution processes three chain-network scenarios:
- No defence.
- Isolation of Computer 1.
- Protection of the middle computer.

Every execution creates fresh networks.
Network size and simulation steps are identical between modes.
Threaded execution uses three workers, one per scenario.

## Procedure
1. Perform three warm-up runs per mode.
2. Perform six measured runs per mode.
3. Alternate which mode executes first.
4. Compare scenario totals after every pair of runs.
5. Report each measured time, averages, and speedup.

Warm-up timings are excluded from the averages.

## Timing Boundaries
System.nanoTime() measures elapsed time.

Both modes include network and task construction, execution,
and result collection.

The threaded mode also includes thread construction, startup,
and joining.

Result comparison and console output are outside the timed sections.

## Calculations
Average time = total measured time / number of measured runs.
Speedup = average sequential time / average threaded time.

Speedup above 1 means threaded execution was faster on average.
Speedup below 1 means threaded execution was slower on average.

## Limitations
Three warm-up runs do not guarantee that JVM performance has settled.
Measurements can vary with background activity, scheduling,
garbage collection, and hardware.

The scenarios have unequal workloads, limiting load balance.
These results describe this workload and machine.

Only final compromised totals are compared for correctness.
The benchmark does not validate every intermediate state.