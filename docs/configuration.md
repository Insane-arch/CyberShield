# Configurable Chain Simulation

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Purpose
Allow network size and simulation steps to be selected at runtime.

## Input
ConfigurableComparison accepts either no arguments or two arguments:
1. Network size.
2. Number of simulation steps.

Defaults: 6 computers and 5 steps.
Minimum network size: 3.
Minimum steps: 0.

Invalid argument counts, invalid integers, and out-of-range
configuration values produce a message and end the program.

## Topology and Scenarios
Computers form a chain, with each computer connected to the next.
Computer 1 starts compromised.

Three scenarios use separate networks:
- No defence.
- Isolate Computer 1.
- Protect the computer with ID size / 2 + 1.

Java integer division is used to calculate the protection target.
Defences are applied before the first step.

## Execution
The program executes all three scenarios sequentially, then executes
fresh copies of the same scenarios using three worker threads.

Both modes use identical network sizes, step counts, and defences.
Each task owns its network, preventing simulation state from being
shared between workers.

All threads start before the main thread joins them.
Results are read and printed after every worker finishes.

The table compares final compromised totals for each scenario.
A mismatch throws AssertionError.

This checks agreement of final counts, not every intermediate state.
Elapsed time is measured as described below.
Speedup is not calculated yet.

## Expected Results
With 6 computers and 5 steps:
- No defence: 6 compromised.
- Isolation: 1 compromised.
- Protection: 3 compromised.

With 10 computers and 9 steps:
- No defence: 10 compromised.
- Isolation: 1 compromised.
- Protection: 5 compromised.

With zero steps, all three scenarios retain only the initial compromise.

## Limitations
The topology is a chain, not a randomly generated network.
A sufficiently small step count may stop before spread finishes.
The adjacency matrix requires quadratic memory, so use modest sizes
during these initial demonstrations.

## Initial Timing Measurement
System.nanoTime() measures elapsed time for each execution mode.
Nanoseconds are converted to milliseconds for display.

Both measurements include:
- Network construction.
- Task construction and simulation execution.
- Result collection.

The threaded measurement also includes thread creation, startup,
and waiting for completion.

Console output and result comparison are outside the timed sections.

These are single-run measurements without warm-up.
JVM startup effects, compilation, garbage collection, and operating
system scheduling can influence the readings.

Sequential execution always runs first in this initial version.
The three scenarios also have unequal workloads.

These readings are exploratory. Repeated measurements and warm-up
are required before reporting performance conclusions.