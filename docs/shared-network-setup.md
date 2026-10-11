# Shared Network Setup

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Purpose
Keep the five-computer demonstration setup in one place so
sequential and threaded programs use consistent scenarios.

## NetworkFactory
createScenario() returns a fresh network on every call.

Scenario constants:
- NO_DEFENCE = 0
- ISOLATION = 1
- PROTECTION = 2

Each network has the same five computers and connections.
Computer 1 starts compromised.
The selected defence is applied before the network is returned.
Invalid scenario numbers are rejected.

## Programs Using the Factory
- Main
- ScenarioComparison
- ParallelComparison

SimulationChecks retains its separate three-computer setup.

## Independence
The factory shares construction code, not network objects.
Each call creates new computer objects and a new adjacency matrix.
Different worker threads therefore retain separate simulation state.

## Expected Behaviour
This refactoring should not change simulation results:
- Main: compromised totals of 3, 4, and 5.
- ScenarioComparison: final totals of 5, 1, and 3.
- ParallelComparison: all three totals match sequential execution.