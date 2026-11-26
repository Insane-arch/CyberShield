# Java Simulation Correctness Checks

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Work Completed By

Muhammad Hamza Naseer — sequential simulation verification.

## Purpose

These checks verify containment rules and step-by-step propagation
before using the sequential simulator as a reference for parallel work.

Each check starts with a fresh three-computer chain:

Computer 1 connects to Computer 2.
Computer 2 connects to Computer 3.

Only Computer 1 is initially compromised.

## Checks

| Check | Required behaviour |
|---|---|
| Isolated source | Computer 1 remains compromised but cannot spread |
| Isolated target | Computer 2 cannot receive or relay the attack |
| Protected target | Computer 2 cannot become compromised |
| One connection per step | Computer 3 becomes compromised in step 2, not step 1 |

The blocked-spread checks execute two steps. Both steps must report
zero new compromises, and only Computer 1 must remain compromised.

## Propagation Check

| Stage | Computer 1 | Computer 2 | Computer 3 | Newly compromised |
|---|---|---|---|---:|
| Initial | Compromised | Healthy | Healthy | Not applicable |
| Step 1 | Compromised | Compromised | Healthy | 1 |
| Step 2 | Compromised | Compromised | Compromised | 1 |
| Step 3 | Compromised | Compromised | Compromised | 0 |

The check verifies individual computer states, total compromises,
and the new-compromise count returned by each step.

## Connection to the Course

This demonstrates data dependence between simulation steps.

The simulator first records pending compromises without changing
computer states. It applies those changes only after checking the
whole network.

Therefore, a computer compromised during one step can spread the
attack only in the following step.

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

All 4 containment checks passed.
```

A failed check throws an AssertionError and stops execution.
The checks do not require Java's `-ea` option because they explicitly
throw the error rather than using the `assert` keyword.

## Scope

These checks cover the sequential Java simulator on a small chain.
They do not establish parallel thread safety or measure performance.
## Execution Screenshot

![Java containment and propagation checks](screenshots/java-containment-checks.png)

Figure 1: All four Java checks pass, covering isolated sources,
isolated targets, protected targets, and one-connection-per-step propagation.
