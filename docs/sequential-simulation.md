# Sequential Simulation

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Purpose
Provide a sequential baseline for attack propagation before
introducing parallel and distributed execution.

## Initial Rules
- An attack spreads through existing network connections.
- A compromised, non-isolated computer can spread the attack.
- A healthy neighbour is eligible only when it is neither
  isolated nor protected.
- Every eligible neighbour becomes compromised in this version.
- Protection completely blocks new compromise in this simplified model.
- Protection does not cure an already compromised computer.
- Isolation blocks both incoming and outgoing spread.
- Compromised computers do not recover.

## Two-Phase Update
First, inspect the current states and mark pending compromises
in a temporary boolean array.

Second, apply the pending changes to the computers.

This prevents newly compromised computers from spreading within
the same step and counts each new compromise only once.

## Demonstration
Use connections 1-2, 1-3, 2-4, 3-4, and 4-5.
Initially, only Computer 1 is compromised.

Expected results:
- Step 1: Computers 2 and 3 become compromised; total = 3.
- Step 2: Computer 4 becomes compromised; total = 4.
- Step 3: Computer 5 becomes compromised; total = 5.

## Current Limitations
Propagation probabilities, automatic containment strategies,
multithreading, and distributed execution are not implemented yet.

This demonstration checks propagation timing and avoids duplicate
counting. Protection and isolation behaviour require separate checks.