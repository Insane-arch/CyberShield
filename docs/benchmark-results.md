# Initial Benchmark Results

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

Date: 11 October 2026

## Method
- Chain network with three containment scenarios.
- Sequential execution compared with three worker threads.
- Three warm-up runs per mode.
- Six measured runs per mode.
- Alternating execution order.
- Fresh networks for every execution.
- Final compromised totals matched in every run.

## Results
| Computers | Steps | Sequential average (ms) | Threaded average (ms) | Speedup |
| --- | --- | --- | --- | --- |
| 100 | 99 | 0.668 | 0.795 | 0.84x |
| 500 | 499 | 82.597 | 45.984 | 1.80x |

## Individual Measurements
All times are in milliseconds.

| Run | First mode | 100 sequential | 100 threaded | 500 sequential | 500 threaded |
| --- | --- | --- | --- | --- | --- |
| 1 | Threaded | 0.613 | 1.097 | 78.805 | 44.940 |
| 2 | Sequential | 0.647 | 0.759 | 80.616 | 45.214 |
| 3 | Threaded | 0.605 | 0.751 | 83.742 | 46.930 |
| 4 | Sequential | 0.629 | 0.648 | 85.605 | 45.914 |
| 5 | Threaded | 0.665 | 0.687 | 82.781 | 47.071 |
| 6 | Sequential | 0.850 | 0.826 | 84.031 | 45.835 |

Values are copied from console output and rounded to three decimals.

## Interpretation
For 100 computers, threaded execution took about 19% longer.
For 500 computers, threaded execution took about 44% less time.

These observations are consistent with thread overhead outweighing
the benefit for small workloads, while larger workloads benefit
from concurrent execution.

Both network size and simulation steps increased between experiments.
This comparison does not isolate the effect of network size alone.

## Limitations
Results describe these benchmark sessions, not guaranteed performance.
The three scenarios have unequal workloads.
Hardware details still need to be recorded.
Additional sessions may produce different timings.