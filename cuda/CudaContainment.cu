#include <cuda_runtime.h>
#include "CpuSimulation.h"

#include <cstdio>
#include <cstdlib>
#include <exception>
#include <numeric>
#include <vector>

void checkCuda(cudaError_t result, const char* operation) {
    if (result != cudaSuccess) {
        std::fprintf(stderr, "%s failed: %s\n",
                     operation, cudaGetErrorString(result));
        std::exit(EXIT_FAILURE);
    }
}

__global__ void spreadOneStep(
    const int* current,
    int* next,
    const int* isolated,
    const int* protectedNodes,
    int count
) {
    int index = blockIdx.x * blockDim.x + threadIdx.x;

    if (index >= count) {
        return;
    }

    next[index] = current[index];

    if (current[index] != 0 ||
        isolated[index] != 0 ||
        protectedNodes[index] != 0) {
        return;
    }

    bool receivesAttack = false;

    if (index > 0 &&
        current[index - 1] != 0 &&
        isolated[index - 1] == 0) {
        receivesAttack = true;
    }

    if (index + 1 < count &&
        current[index + 1] != 0 &&
        isolated[index + 1] == 0) {
        receivesAttack = true;
    }

    next[index] = receivesAttack ? 1 : 0;
}

bool runScenario(
    const char* name,
    int isolatedComputer,
    int protectedComputer,
    const std::vector<int>& expected
) {
    constexpr int count = 8;
    constexpr int steps = 7;
    constexpr size_t bytes = count * sizeof(int);

    if (isolatedComputer < 0 || isolatedComputer > count ||
        protectedComputer < 0 || protectedComputer > count ||
        expected.size() != count) {
        std::fprintf(stderr, "Invalid scenario configuration.\n");
        return false;
    }

    std::vector<int> initial(count, 0);
    std::vector<int> isolated(count, 0);
    std::vector<int> protectedNodes(count, 0);
    std::vector<int> gpuResult(count, 0);

    initial[0] = 1;

    if (isolatedComputer > 0) {
        isolated[isolatedComputer - 1] = 1;
    }

    if (protectedComputer > 0) {
        protectedNodes[protectedComputer - 1] = 1;
    }

    // CPU starts from the same initial states and defence flags.
    std::vector<int> cpuResult =
        simulateCpu(initial, isolated, protectedNodes, steps);

    int* deviceCurrent = nullptr;
    int* deviceNext = nullptr;
    int* deviceIsolated = nullptr;
    int* deviceProtected = nullptr;

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceCurrent), bytes),
        "Allocate current states");

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceNext), bytes),
        "Allocate next states");

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceIsolated), bytes),
        "Allocate isolation flags");

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceProtected), bytes),
        "Allocate protection flags");

    checkCuda(cudaMemcpy(deviceCurrent, initial.data(), bytes,
                         cudaMemcpyHostToDevice),
              "Copy initial states");

    checkCuda(cudaMemcpy(deviceIsolated, isolated.data(), bytes,
                         cudaMemcpyHostToDevice),
              "Copy isolation flags");

    checkCuda(cudaMemcpy(deviceProtected, protectedNodes.data(), bytes,
                         cudaMemcpyHostToDevice),
              "Copy protection flags");

    constexpr int threadsPerBlock = 128;
    int blocks = (count + threadsPerBlock - 1) / threadsPerBlock;

    for (int step = 0; step < steps; ++step) {
        spreadOneStep<<<blocks, threadsPerBlock>>>(
            deviceCurrent,
            deviceNext,
            deviceIsolated,
            deviceProtected,
            count);

        checkCuda(cudaGetLastError(), "Launch propagation kernel");
        checkCuda(cudaDeviceSynchronize(), "Complete propagation step");

        int* temporary = deviceCurrent;
        deviceCurrent = deviceNext;
        deviceNext = temporary;
    }

    checkCuda(cudaMemcpy(gpuResult.data(), deviceCurrent, bytes,
                         cudaMemcpyDeviceToHost),
              "Copy final states");

    checkCuda(cudaFree(deviceCurrent), "Free current states");
    checkCuda(cudaFree(deviceNext), "Free next states");
    checkCuda(cudaFree(deviceIsolated), "Free isolation flags");
    checkCuda(cudaFree(deviceProtected), "Free protection flags");

    // Compare complete state arrays, not only compromised totals.
    bool match = cpuResult == gpuResult;
    bool expectedMatch =
        cpuResult == expected && gpuResult == expected;

    int cpuTotal =
        std::accumulate(cpuResult.begin(), cpuResult.end(), 0);

    int gpuTotal =
        std::accumulate(gpuResult.begin(), gpuResult.end(), 0);

    std::printf("%-24s %6d %6d %8s %10s\n",
                name, cpuTotal, gpuTotal,
                match ? "YES" : "NO",
                expectedMatch ? "PASS" : "FAIL");

    for (int i = 0; i < count; ++i) {
        if (cpuResult[i] != expected[i] ||
            gpuResult[i] != expected[i]) {
            std::fprintf(
                stderr,
                "%s: Computer %d | CPU=%d GPU=%d Expected=%d\n",
                name, i + 1,
                cpuResult[i], gpuResult[i], expected[i]);
        }
    }

    return match && expectedMatch;
}

int main() {
    std::printf("CyberShield - CPU and CUDA Comparison\n");
    std::printf("Muhammad Hamza Naseer - 247121\n");
    std::printf("Sultan Majeed - 247187\n\n");

    std::printf("Computers: 8\n");
    std::printf("Simulation steps: 7\n");
    std::printf("Initially compromised: Computer 1\n\n");

    std::printf("%-24s %6s %6s %8s %10s\n",
                "Scenario", "CPU", "GPU", "Match", "Expected");

    try {
        bool baseline = runScenario(
            "No defence", 0, 0,
            {1, 1, 1, 1, 1, 1, 1, 1});

        bool source = runScenario(
            "Isolate Computer 1", 1, 0,
            {1, 0, 0, 0, 0, 0, 0, 0});

        bool middle = runScenario(
            "Isolate Computer 4", 4, 0,
            {1, 1, 1, 0, 0, 0, 0, 0});

        bool protection = runScenario(
            "Protect Computer 4", 0, 4,
            {1, 1, 1, 0, 0, 0, 0, 0});

        bool passed = baseline && source && middle && protection;

        std::printf("\n%s\n",
                    passed
                        ? "All 4 CPU/GPU state comparisons passed."
                        : "CPU/GPU verification failed.");

        return passed ? EXIT_SUCCESS : EXIT_FAILURE;
    } catch (const std::exception& error) {
        std::fprintf(stderr, "Error: %s\n", error.what());
        return EXIT_FAILURE;
    }
}