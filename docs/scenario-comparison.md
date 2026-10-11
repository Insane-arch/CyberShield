# Sequential Scenario Comparison

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Compare the effects of preset containment actions using the
sequential simulator.

## Shared Conditions
Each scenario uses a separate, freshly created network:
- Five computers.
- Connections: 1-2, 1-3, 2-4, 3-4, and 4-5.
- Computer 1 initially compromised.
- Three simulation steps.
- Deterministic propagation.

## Scenarios
1. No defence: leave all computers unprotected and non-isolated.
2. Isolation: isolate Computer 1 before the first step.
3. Protection: protect Computer 4 before the first step.

## Expected Results
| Scenario | Compromised | Isolated | Protected |
| --- | --- | --- | --- |
| No defence | 5 | 0 | 0 |
| Isolate Computer 1 | 1 | 1 | 0 |
| Protect Computer 4 | 3 | 0 | 1 |

## Interpretation
Without defence, the attack reaches every computer.

Isolating Computer 1 prevents all outgoing spread, but Computer 1
remains compromised.

Protecting Computer 4 blocks that computer from becoming compromised.
Computers 2 and 3 still become compromised, but Computer 5 remains
healthy because its only connection is to Computer 4.

## Limitations
These are manually selected actions, not automatic strategies.
Isolation assumes the initially compromised computer is known
and can be isolated immediately.
Protection completely blocks new compromise in this model.

The results apply to this topology and these assumptions.
They do not establish which strategy is best for every network.
This comparison measures containment outcomes, not execution speed.

## Course Connection
This sequential implementation provides a baseline for later
parallel and distributed execution of independent scenarios.