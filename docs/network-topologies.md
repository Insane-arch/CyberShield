# Network Topologies and Containment

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Work Completed By

Muhammad Hamza Naseer — ring and star scenario factories,
topology validation and sequential/data-parallel comparison.

## Purpose

Network structure affects how an attack spreads and whether protecting
one computer prevents it from reaching other computers.

CyberShield now provides chain, ring and star scenario factories.

## Supported Topologies

| Topology | Connections | Number of edges for N computers |
|---|---|---:|
| Chain | Consecutive computers connect | N - 1 |
| Ring | Consecutive computers connect, plus last to first | N |
| Star | Computer 1 connects to every other computer | N - 1 |

Connections are undirected. Self-connections are not created.

All three factories require at least three computers.

## Scenario Rules

Each factory supports the same scenario codes:

| Code | Scenario | Action |
|---:|---|---|
| 0 | No defence | No defensive flags are applied |
| 1 | Isolation | Isolate Computer 1 |
| 2 | Protection | Protect Computer size / 2 + 1 |

Computer 1 starts compromised in every scenario.

The protection target uses integer division.
For seven computers, the target is Computer 4.

In the star topology, Computer 1 is the central hub.

## Factory Methods

```java
NetworkFactory.createChainScenario(size, scenario);
NetworkFactory.createRingScenario(size, scenario);
NetworkFactory.createStarScenario(size, scenario);
```

The existing createScenario method retains its original five-computer
network and protection target of Computer 4.

## Comparison Configuration

TopologyComparison uses:

| Setting | Value |
|---|---:|
| Computers | 7 |
| Steps | 6 |
| Data-parallel workers | 3 |
| Topologies | 3 |
| Scenarios per topology | 3 |

Each scenario creates separate sequential and data-parallel networks.

Six steps are sufficient for full reachable propagation in these
seven-computer scenarios.

## Verification

The comparison checks:

1. Every connection against the expected topology, including absent
   connections and self-connections.
2. Matching initial states between sequential and parallel networks.
3. Matching newly compromised counts after every step.
4. Matching compromised, isolated and protected states after every step.
5. Final states against independently specified topology/scenario rules.

An incorrect connection, state or count causes an AssertionError.

## Expected Final Totals

| Topology | No defence | Isolate Computer 1 | Protect Computer 4 |
|---|---:|---:|---:|
| Chain | 7 | 1 | 3 |
| Ring | 7 | 1 | 6 |
| Star | 7 | 1 | 6 |

Both sequential and data-parallel execution should produce these totals.

## Why Protection Has Different Effects

### Chain

Computer 4 lies on the only route from Computer 1 to Computers 5–7.

Protecting Computer 4 stops propagation beyond Computer 3.
Only Computers 1, 2 and 3 become compromised.

### Ring

The attack can travel in both directions from Computer 1.

Although Computer 4 is protected, Computers 5–7 can be reached through
the connection between Computers 1 and 7.

All computers except Computer 4 become compromised.

### Star

Computer 1 is the initially compromised hub.

It directly reaches every unprotected leaf during the first step.
Protecting Computer 4 leaves that computer healthy, but does not block
the hub's connections to the other leaves.

## Source Isolation

Isolating Computer 1 prevents the only initially compromised computer
from sending attacks in every topology.

Computer 1 remains compromised, so the final total is one.

## Compile and Run

From the project directory:

```powershell
javac -d out src\cybershield\*.java
```

After successful compilation:

```powershell
java -cp out cybershield.TopologyComparison
```

Successful execution ends with:

```text
All 9 topology scenario checks passed.
```

## Existing Behaviour Checks

The factory update also preserves the original scenarios used by:

```powershell
java -cp out cybershield.SimulationChecks
java -cp out cybershield.ParallelComparison 3
```

The containment checks should all pass.

The original five-computer comparison should produce totals 5, 1 and 3,
with matching sequential and threaded states.

## Connection to the Course

This feature connects network structure with parallel-program design,
data dependence and data parallelism.

The data-parallel simulator divides target computers among workers.
All pending changes are applied only after worker completion.

## Limitations

The demonstration uses fixed size, step count, worker count and source.

Its results describe the configured scenarios. They do not establish
that one topology is always more secure than another.

The star scenario starts the attack at the hub; starting at a leaf
would change the propagation sequence.

This comparison checks correctness, not performance.

The CUDA implementation still supports chains only.