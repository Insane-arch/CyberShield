# Simulation History

## Project Members

- Muhammad Hamza Naseer â€” 247121
- Sultan Majeed â€” 247187

## Work Completed By

Muhammad Hamza Naseer â€” per-step history recording and
sequential/data-parallel verification.

## Purpose

SimulationHistory records how the network changes after every step,
rather than showing only a final compromised total.

It runs separate sequential and data-parallel networks and compares
their histories and individual computer states.

## Current Configuration

| Setting | Value |
|---|---|
| Topology | Chain |
| Computers | 7 |
| Simulation steps | 8 |
| Initially compromised | Computer 1 |
| Defence | None |
| Data-parallel workers | 3 |

The configuration is currently fixed in the program.

## History Entries

Each HistoryEntry is an immutable Java record containing:

| Field | Meaning |
|---|---|
| step | Simulation step number |
| newlyCompromised | Computers newly compromised during this step |
| totalCompromised | Total compromised computers after the step |
| healthy | Computers that are not compromised |

Entries store integer values, not references to mutable computer objects.
Later simulation changes therefore do not alter earlier history entries.

## Initial State

Step 0 records the network before any propagation occurs.

Computer 1 is already compromised, so the total is one.
The new-compromise count is zero because no simulation step has run yet.

## Expected History

| Step | New | Compromised | Healthy |
|---:|---:|---:|---:|
| 0 | 0 | 1 | 6 |
| 1 | 1 | 2 | 5 |
| 2 | 1 | 3 | 4 |
| 3 | 1 | 4 | 3 |
| 4 | 1 | 5 | 2 |
| 5 | 1 | 6 | 1 |
| 6 | 1 | 7 | 0 |
| 7 | 0 | 7 | 0 |
| 8 | 0 | 7 | 0 |

The attack advances one connection per step.

All computers are compromised after step 6.
Steps 7 and 8 verify that a completed spread adds no new compromises.

## Verification

The program checks:

- Initial individual states match between execution modes.
- Individual compromised, isolated and protected states match after every step.
- Sequential and parallel history entries match.
- History contains one initial entry plus one entry per simulation step.
- Step numbers are in order.
- Compromised plus healthy equals the network size.
- New-compromise counts and healthy counts are nonnegative.
- Compromised totals never decrease.
- Each increase in the total equals that step's new-compromise count.
- Counts match the independent expectation for the configured chain.

The complete sequential and parallel history lists are also compared.

A verification failure throws an AssertionError.

## Compile and Run

From the project directory:

```powershell
javac -d out src\cybershield\*.java
```

After successful compilation:

```powershell
java -cp out cybershield.SimulationHistory
```

Successful execution prints the history table with YES for every row,
followed by:

```text
Sequential and parallel histories match at every step.
History consistency checks passed.
```

## Connection to the Course

This feature demonstrates data dependence between simulation steps,
comparison of sequential and parallel results, and storing results
after worker completion.

Each simulator operates on its own network.
History recording occurs on the main thread after a step completes.

## Limitations

- Configuration is fixed to a seven-computer chain without defence.
- History stores aggregate counts rather than full per-computer snapshots.
- Individual states are compared during execution but are not retained.
- The expected-history formula applies only to this configured scenario.
- CSV export is not implemented yet.
- The program verifies correctness rather than measuring performance.
## Execution Screenshot

![Sequential and data-parallel simulation history](screenshots/java-simulation-history.png)

Figure 1: Both execution modes produce matching histories for steps
0–8. All seven computers are compromised by step 6, and steps 7–8
produce no new compromises.
