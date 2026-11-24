# CUDA Chain Containment Simulation

## Project Members

- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Implementation By

Muhammad Hamza Naseer — CUDA propagation, defence handling,
sequential C++ reference and CPU/GPU comparison.

## Simulation Setup

- Eight computers connected in a chain.
- Computer 1 starts compromised.
- Each scenario runs for seven steps.
- Each execution starts from fresh initial states.

## Containment Rules

A healthy computer becomes compromised when an immediate neighbour
was compromised at the beginning of the step, unless the receiving
computer is isolated or protected, or the sending computer is isolated.

Isolation blocks sending and receiving.

Protection blocks new compromise. It does not cure an existing
compromise or prevent an already compromised computer from sending.

Existing compromises persist. No recovery is implemented.

## GPU Implementation

Each GPU thread calculates one computer's next state.

The kernel reads from the current-state array and writes to a separate
next-state array. After the kernel completes, the pointers are swapped.

This ensures that the attack spreads at most one connection per step.

## CPU Reference

`cuda/CpuSimulation.h` implements a sequential C++ chain simulation.

The CPU visits compromised sources and updates their eligible neighbours
in a separate next-state array. The GPU instead calculates each receiving
computer's next state in parallel.

Both implementations use identical initial states, defence flags and
step counts.

`cuda/CpuReferenceChecks.cpp` checks the CPU implementation independently
against manually specified expected states.

This C++ reference is separate from the existing Java simulator.
Direct automated Java/CUDA comparison is not implemented yet.

## Verification

| Scenario | CPU total | GPU total | Expected total |
|---|---:|---:|---:|
| No defence | 8 | 8 | 8 |
| Isolate Computer 1 | 1 | 1 | 1 |
| Isolate Computer 4 | 3 | 3 | 3 |
| Protect Computer 4 | 3 | 3 | 3 |

The program compares complete CPU and GPU state arrays.

Both results are also checked against manually specified expected states.
Matching totals alone are not sufficient.

A state mismatch reports the computer ID and its CPU, GPU and expected
values. Verification failure produces a nonzero exit code.

CUDA API calls, kernel launches and kernel completion are checked for errors.

## Build Environment

Run in CMD:

```bat
call "C:\Program Files\Microsoft Visual Studio\18\Community\VC\Auxiliary\Build\vcvarsall.bat" x64 -vcvars_ver=14.44
cd /d D:\pdc-lab-project
```

## Build and Run CPU Checks

```bat
cl /nologo /EHsc /std:c++17 cuda\CpuReferenceChecks.cpp /Fo:out\CpuReferenceChecks.obj /Fe:out\CpuReferenceChecks.exe
```

After successful compilation:

```bat
out\CpuReferenceChecks.exe
```

## Build and Run CPU/GPU Comparison

```bat
nvcc -std=c++17 cuda\CudaContainment.cu -o out\CudaContainment.exe
```

After successful compilation:

```bat
out\CudaContainment.exe
```

Successful execution ends with:

```text
All 4 CPU/GPU state comparisons passed.
```

## Execution Evidence

![CPU and GPU comparison for four scenarios](screenshots/cuda-cpu-comparison.png)

Figure 1: CPU and GPU final states match in all four scenarios and
also match the expected states.

## Earlier Milestones

![GPU defence checks](screenshots/cuda-defence-checks.png)

Figure 2: GPU isolation and protection checks before CPU integration.

![Seven-step propagation](screenshots/cuda-seven-steps.png)

Figure 3: Earlier propagation verification without defence.

## Current Limitations

- Network size, step count and scenarios are fixed.
- Only chain topology is supported by this CUDA implementation.
- Performance and GPU speedup have not been measured.
- Dedicated checks for zero steps and protection of an already
  compromised source are still pending.