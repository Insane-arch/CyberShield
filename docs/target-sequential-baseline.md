# Target-First Sequential Baseline

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Work Completed By

Muhammad Hamza Naseer — target-first sequential implementation
and correctness comparison.

## Purpose

The original sequential simulator scans compromised sources first.

The data-parallel simulators scan target computers first and stop
searching sources after finding one eligible attacker.

TargetSequentialSimulator provides a sequential implementation of
that target-first approach. It supports a more comparable baseline
for future parallel performance measurements.

The original SequentialSimulator remains available.

## Implementation Files

- `src/cybershield/TargetSequentialSimulator.java`
- `src/cybershield/TargetSequentialComparison.java`

## Algorithm Comparison

| Property | Source-first sequential | Target-first sequential |
|---|---|---|
| Outer loop | Source computers | Target computers |
| Inner loop | Target computers | Source computers |
| Early exit after an eligible source | No | Yes |
| Pending changes | Boolean array | Boolean array |
| State changes applied | After evaluation | After evaluation |
| Execution | Main thread | Main thread |

Both implementations follow the same containment rules.

## Target-First Step

For each target computer:

1. Skip it if already compromised, isolated or protected.
2. Examine possible sources in computer-ID order.
3. Find a connected source that is compromised and not isolated.
4. Mark the target as pending and stop searching its sources.

After all targets are evaluated, apply pending changes.

Each marked target is counted once, even if several sources could
have compromised it.

Newly compromised computers cannot spread until the next step.

## Relationship to Parallel Execution

The data-parallel implementations divide target computers into
disjoint ranges.

TargetSequentialSimulator evaluates all targets sequentially using
the same source-search condition and early-exit rule.

This reduces algorithmic differences in a future benchmark.
Parallel task creation, scheduling, synchronization and cleanup still
introduce additional work and must be accounted for in timing.

## Correctness Comparison

TargetSequentialComparison uses:

| Setting | Value |
|---|---:|
| Computers | 7 |
| Steps | 8 |
| Topologies | Chain, ring, star |
| Scenarios per topology | 3 |

Each case creates fresh, separate networks for the two implementations.

The program compares:

- Initial computer states.
- Newly compromised counts after every step.
- Total compromised counts after every step.
- Individual computer IDs and state flags after every step.
- Final totals against known expected values.

A mismatch causes an AssertionError.

## Expected Final Totals

| Topology | No defence | Isolate Computer 1 | Protect Computer 4 |
|---|---:|---:|---:|
| Chain | 7 | 1 | 3 |
| Ring | 7 | 1 | 6 |
| Star | 7 | 1 | 6 |

Both implementations should produce these totals.

The eight-step runs also include steps after propagation has finished.

## Compile and Run

From the project directory:

```powershell
javac -d out src\cybershield\*.java
```

After successful compilation:

```powershell
java -cp out cybershield.TargetSequentialComparison
```

Successful execution ends with:

```text
All 9 sequential algorithm checks passed.
```

## Connection to the Course

This demonstrates sequential algorithm design, data dependence,
early termination and establishing a suitable baseline for parallel
performance analysis.

## Usage Constraint

External code must not modify the network while the simulator runs.

TargetSequentialSimulator is intended for sequential use.
It does not provide synchronization for concurrent callers.

## Limitations

- The comparison uses small, fixed networks.
- Passing these cases does not prove correctness for every input.
- This program does not measure execution time.
- No sequential-versus-pooled speedup has been established yet.
- The configurable runner still uses the original source-first simulator
  as its sequential correctness reference.