# CyberShield — Project Requirements

## 1. Project Information
### Project Members
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

University: Air University Kharian Campus
Course: Parallel and Distributed Computing
Project Type: Individual
Language: Java

## 2. Problem Statement
Containing an attack can reduce its spread but also disrupt
healthy computers and network connections.

CyberShield will simulate this trade-off and evaluate how
parallel and distributed execution affect experiment runtime.

## 3. Objective
Build a reproducible network attack containment simulator with
sequential, parallel, and distributed execution modes.

## 4. Network Model
- Represent computers as nodes and connections as edges.
- Give each computer a unique ID.
- Track whether each computer is healthy or compromised.
- Track protection and isolation separately.
- Advance the simulation in discrete time steps.
- Use configurable propagation probabilities and random seeds.

## 5. Containment Strategies
Compare a no-defence baseline against:
1. Reactive isolation: isolate detected compromised computers.
2. Targeted protection: protect highly connected computers.
3. Network segmentation: disable selected connections between groups.

Detection delays, protection effects, and intervention budgets
will be documented explicitly as simulation assumptions.

## 6. Execution Modes
### Sequential
Run experiment trials one after another.

### Parallel
Run independent trials concurrently using Java worker threads.

### Distributed
Use a coordinator to assign independent trials to worker
applications through Java sockets and collect their results.

Each trial has its own network state and random seed.

## 7. Functional Requirements
- Configure network size, trial count, and random seed.
- Select a containment strategy and execution mode.
- Display a representative simulation visually.
- Report compromise counts and containment actions.
- Export experiment results to CSV.
- Identify failed distributed tasks and report incomplete runs.

## 8. Evaluation
Measure:
- Number of computers ever compromised.
- Number of healthy computers isolated.
- Number of connections disabled.
- Total experiment execution time.
- Speedup: sequential time divided by parallel time.
- Efficiency: speedup divided by worker count.

Include communication overhead in distributed runtime.
Exclude visualization from computation benchmarks.
Record hardware, worker counts, settings, and trial seeds.
Use warm-up runs and repeated measurements.

## 9. Correctness Requirements
- Identical trial inputs and seeds produce identical results.
- Results agree across all three execution modes.
- Every requested trial is counted exactly once.
- State updates follow documented time-step rules.
- Shared data is accessed safely.

## 10. Scope Boundaries
- Attacks occur only inside the simulation.
- No real malware, exploitation, or live network scanning.
- No machine learning or cloud deployment in the initial version.
- Results describe the chosen model, not real-world predictions.

## 11. Completion Criteria
- All three execution modes work.
- Containment strategies can be compared.
- Correctness checks pass.
- Performance results are recorded, including any slowdowns.
- Documentation explains setup, design, limitations, and usage.