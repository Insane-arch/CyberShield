#include <cuda_runtime.h>
#include "CpuSimulation.h"

#include <cstdio>
#include <cstdlib>
#include <exception>
#include <numeric>
#include <stdexcept>
#include <string>
#include <vector>

constexpr int COMPUTER_COUNT = 8;

void checkCuda(cudaError_t result, const char* operation) {
    if (result != cudaSuccess) {
        std::fprintf(stderr, "%s failed: %s\n",
                     operation, cudaGetErrorString(result));
        std::exit(EXIT_FAILURE);
    }
}

int parseSteps(const char* argument) {
    std::string text(argument);

    if (text.empty() ||
        text.find_first_not_of("0123456789") != std::string::npos) {
        throw std::invalid_argument(
            "Steps must be a whole number from 0 to 10000.");
    }

    // Also rejects numbers too large to fit in an int.
    int steps = std::stoi(text);

    if (steps > 10000) {
        throw std::invalid_argument(
            "Steps must be a whole number from 0 to 10000.");
    }

    return steps;
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

// Independent expected result for this specific scenario family:
// a chain, initially compromised Computer 1, and at most one
// isolation target and one protection target.
std::vector<int> expectedStates(
    int steps,
    int isolatedComputer,
    int protectedComputer
) {
    std::vector<int> expected(COMPUTER_COUNT, 0);
    expected[0] = 1;

    if (isolatedComputer == 1) {
        return expected;
    }

    int lastReachable = COMPUTER_COUNT;

    if (isolatedComputer > 1 &&
        isolatedComputer - 1 < lastReachable) {
        lastReachable = isolatedComputer - 1;
    }

    if (protectedComputer > 1 &&
        protectedComputer - 1 < lastReachable) {
        lastReachable = protectedComputer - 1;
    }

    for (int id = 2; id <= lastReachable; ++id) {
        if (id - 1 <= steps) {
            expected[id - 1] = 1;
        }
    }

    return expected;
}

bool runScenario(
    const char* name,
    int isolatedComputer,
    int protectedComputer,
    int steps
) {
    constexpr size_t bytes = COMPUTER_COUNT * sizeof(int);

    if (isolatedComputer < 0 || isolatedComputer > COMPUTER_COUNT ||
        protectedComputer < 0 || protectedComputer > COMPUTER_COUNT) {
        throw std::invalid_argument("Invalid defence computer ID.");
    }

    std::vector<int> initial(COMPUTER_COUNT, 0);
    std::vector<int> isolated(COMPUTER_COUNT, 0);
    std::vector<int> protectedNodes(COMPUTER_COUNT, 0);
    std::vector<int> gpuResult(COMPUTER_COUNT, 0);

    initial[0] = 1;

    if (isolatedComputer > 0) {
        isolated[isolatedComputer - 1] = 1;
    }

    if (protectedComputer > 0) {
        protectedNodes[protectedComputer - 1] = 1;
    }

    std::vector<int> expected =
        expectedStates(steps, isolatedComputer, protectedComputer);

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
    int blocks =
        (COMPUTER_COUNT + threadsPerBlock - 1) / threadsPerBlock;

    for (int step = 0; step < steps; ++step) {
        spreadOneStep<<<blocks, threadsPerBlock>>>(
            deviceCurrent,
            deviceNext,
            deviceIsolated,
            deviceProtected,
            COMPUTER_COUNT);

        checkCuda(cudaGetLastError(), "Launch propagation kernel");
        checkCuda(cudaDeviceSynchronize(), "Complete propagation step");

        int* temporary = deviceCurrent;
        deviceCurrent = deviceNext;
        deviceNext = temporary;
    }

    // With zero steps, deviceCurrent still contains the initial states.
    checkCuda(cudaMemcpy(gpuResult.data(), deviceCurrent, bytes,
                         cudaMemcpyDeviceToHost),
              "Copy final states");

    checkCuda(cudaFree(deviceCurrent), "Free current states");
    checkCuda(cudaFree(deviceNext), "Free next states");
    checkCuda(cudaFree(deviceIsolated), "Free isolation flags");
    checkCuda(cudaFree(deviceProtected), "Free protection flags");

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

    for (int i = 0; i < COMPUTER_COUNT; ++i) {
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

int main(int argc, char* argv[]) {
    std::printf("CyberShield - CPU and CUDA Comparison\n");
    std::printf("Muhammad Hamza Naseer - 247121\n");
    std::printf("Sultan Majeed - 247187\n\n");

    try {
        if (argc > 2) {
            throw std::invalid_argument(
                "Usage: CudaContainment.exe [steps]");
        }

        int steps = argc == 2 ? parseSteps(argv[1]) : 7;

        std::printf("Computers: %d\n", COMPUTER_COUNT);
        std::printf("Simulation steps: %d\n", steps);
        std::printf("Initially compromised: Computer 1\n\n");

        std::printf("%-24s %6s %6s %8s %10s\n",
                    "Scenario", "CPU", "GPU", "Match", "Expected");

        bool baseline = runScenario("No defence", 0, 0, steps);
        bool source = runScenario("Isolate Computer 1", 1, 0, steps);
        bool middle = runScenario("Isolate Computer 4", 4, 0, steps);
        bool protection = runScenario("Protect Computer 4", 0, 4, steps);

        bool passed = baseline && source && middle && protection;

        std::printf("\n%s\n",
                    passed
                        ? "All 4 CPU/GPU state comparisons passed."
                        : "CPU/GPU verification failed.");

        return passed ? EXIT_SUCCESS : EXIT_FAILURE;
    } catch (const std::out_of_range&) {
        std::fprintf(stderr, "Error: Steps must be between 0 and 10000.\n");
        return EXIT_FAILURE;
    } catch (const std::exception& error) {
        std::fprintf(stderr, "Error: %s\n", error.what());
        return EXIT_FAILURE;
    }
}