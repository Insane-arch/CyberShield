# Worker Count Benchmark Results

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

Date: 11 October 2026

## Configuration
- 500 computers in a chain.
- 499 simulation steps.
- Three containment scenarios.
- Three warm-up runs and six measured runs per mode.
- Alternating execution order.
- Hardware and runtime: see benchmark-environment.md.

All scenario totals matched in every run.

## Results
| Workers | Sequential average (ms) | Threaded average (ms) | Speedup | Efficiency |
| --- | --- | --- | --- | --- |
| 1 | 83.084 | 86.359 | 0.96x | 96.21% |
| 2 | 78.125 | 77.957 | 1.00x | 50.11% |
| 3 | 81.106 | 47.146 | 1.72x | 57.34% |

Values are copied from the benchmark output.
Efficiency uses unrounded timings, not the displayed rounded speedup.

## Interpretation
One worker executes all tasks serially on a background thread.
It provided no speed improvement in this session.

Two workers achieved approximately the same time as sequential
execution. Static assignment places both heavier scenarios on
worker 0, while worker 1 executes only the lighter isolation scenario.

Three workers separate the heavier scenarios and achieved the
lowest threaded average among the tested configurations.

These findings are consistent with load imbalance affecting the
two-worker configuration. Individual worker times were not measured.

## Limitations
Each worker count was tested in a separate program invocation.
The sequential averages vary between sessions.
Results are observations, not guaranteed performance.

Efficiency describes scaling relative to the sequential baseline;
it does not measure CPU utilization.