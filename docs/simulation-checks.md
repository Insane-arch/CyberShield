# Java Simulation Correctness Checks

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Work Completed By

Muhammad Hamza Naseer — sequential simulation verification.

## Purpose

These checks verify containment rules, step-by-step propagation,
and counting when multiple sources attack the same computer.

Each check starts with a fresh network.

## Checks

| Check | Required behaviour |
|---|---|
| Isolated source | A compromised isolated computer cannot spread |
| Isolated target | An isolated healthy computer cannot receive |
| Protected target | A protected healthy computer cannot become compromised |
| One connection per step | Newly compromised computers spread only in the next step |
| Shared target | Multiple sources compromise and count the same target only once |

## Blocked-Spread Checks

The first three checks use a three-computer chain:

- Computer 1 connects to Computer 2.
- Computer 2 connects to Computer 3.
- Only Computer 1 starts compromised.

Each check applies one defence and executes two steps.

Both steps must report zero new compromises. Only Computer 1
must remain compromised.

## Step-by-Step Propagation

The fourth check uses the same chain without defence.

| Stage | Computer 1 | Computer 2 | Computer 3 | Newly compromised |
|---|---|---|---|---:|
| Initial | Compromised | Healthy | Healthy | Not applicable |
| Step 1 | Compromised | Compromised | Healthy | 1 |
| Step 2 | Compromised | Compromised | Compromised | 1 |
| Step 3 | Compromised | Compromised | Compromised | 0 |

The check verifies individual states, total compromises, and the
new-compromise count returned by each step.

## Multiple Sources and a Shared Target

The fifth check uses four computers:

- Computers 1 and 2 start compromised.
- Both connect to Computer 3.
- Computer 4 is disconnected and starts healthy.

| Stage | Compromised computers | Newly compromised |
|---|---|---:|
| Initial | 1 and 2 | Not applicable |
| Step 1 | 1, 2 and 3 | 1 |
| Step 2 | 1, 2 and 3 | 0 |

Although Computer 3 receives attacks from two sources, it is only
one newly compromised computer.

Computer 4 must remain healthy in both steps.

## Connection to the Course

The propagation check demonstrates data dependence between steps.

The simulator first records pending compromises without changing
computer states. It applies changes after checking the whole network.
This prevents propagation through several connections in one step.

The shared-target check verifies that the simulator counts distinct
newly compromised computers rather than the number of incoming attacks.

The boolean pending-compromise array records each target once.
The application phase counts each marked target once.

This defines expected behaviour for future parallel implementations.
These sequential checks do not establish thread safety.

## Compile and Run

From the project directory:

```powershell
javac -d out src\cybershield\*.java
```

After successful compilation:

```powershell
java -cp out cybershield.SimulationChecks
```

## Successful Output

```text
CyberShield - Containment Checks
Muhammad Hamza Naseer - 247121
Sultan Majeed - 247187

PASS: Isolated source cannot spread
PASS: Isolated target cannot receive
PASS: Protected target blocks compromise
PASS: Attack spreads one connection per step
PASS: Multiple sources count a shared target only once

All 5 containment checks passed.
```

A failed check explicitly throws an AssertionError and stops execution.
Java's `-ea` option is not required.

## Earlier Execution Evidence

![Earlier four Java containment checks](screenshots/java-containment-checks.png)

Figure 1: The earlier four-check version verifies containment and
one-connection-per-step propagation. This screenshot predates the
shared-target check.

## Scope

These checks cover small sequential Java simulations.
They do not measure performance or verify concurrent execution.
## Five-Check Execution Evidence

![All five Java containment checks passing](screenshots/java-five-containment-checks.png)

Figure 2: All five checks pass, including verification that multiple
compromised sources count their shared target only once.
