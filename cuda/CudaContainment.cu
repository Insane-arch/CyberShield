#include <cuda_runtime.h>
#include "CpuSimulation.h"

#include <cstdio>
#include <cstdlib>
#include <exception>
#include <numeric>
#include <stdexcept>
#include <string>
#include <vector>

void checkCuda(cudaError_t result, const char* operation) {
    if (result != cudaSuccess) {
        std::fprintf(stderr, "%s failed: %s\n",
                     operation, cudaGetErrorString(result));
        std::exit(EXIT_FAILURE);
    }
}

int parseNumber(
    const char* argument,
    int minimum,
    int maximum,
    const char* name
) {
    std::string text(argument);
    std::string message =
        std::string(name) + " must be a whole number from " +
        std::to_string(minimum) + " to " + std::to_string(maximum) + ".";

    if (text.empty() ||
        text.find_first_not_of("0123456789") != std::string::npos) {
        throw std::invalid_argument(message);
    }

    int value;

    try {
        value = std::stoi(text);
    } catch (const std::out_of_range&) {
        throw std::invalid_argument(message);
    }

    if (value < minimum || value > maximum) {
        throw std::invalid_argument(message);
    }

    return value;
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

// Expected states for a chain initially compromised at Computer 1.
std::vector<int> expectedStates(
    int count,
    int steps,
    int isolatedComputer,
    int protectedComputer
) {
    std::vector<int> expected(count, 0);
    expected[0] = 1;

    if (isolatedComputer == 1) {
        return expected;
    }

    int lastReachable = count;

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
    const std::string& name,
    int isolatedComputer,
    int protectedComputer,
    int count,
    int steps
) {
    if (isolatedComputer < 0 || isolatedComputer > count ||
        protectedComputer < 0 || protectedComputer > count) {
        throw std::invalid_argument("Invalid defence computer ID.");
    }

    size_t bytes = static_cast<size_t>(count) * sizeof(int);

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

    std::vector<int> expected =
        expectedStates(count, steps, isolatedComputer, protectedComputer);

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
            deviceCurrent, deviceNext,
            deviceIsolated, deviceProtected, count);

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

    bool match = cpuResult == gpuResult;
    bool expectedMatch =
        cpuResult == expected && gpuResult == expected;

    int cpuTotal =
        std::accumulate(cpuResult.begin(), cpuResult.end(), 0);
    int gpuTotal =
        std::accumulate(gpuResult.begin(), gpuResult.end(), 0);

    std::printf("%-26s %8d %8d %8s %10s\n",
                name.c_str(), cpuTotal, gpuTotal,
                match ? "YES" : "NO",
                expectedMatch ? "PASS" : "FAIL");

    for (int i = 0; i < count; ++i) {
        if (cpuResult[i] != expected[i] ||
            gpuResult[i] != expected[i]) {
            std::fprintf(
                stderr,
                "%s: Computer %d | CPU=%d GPU=%d Expected=%d\n",
                name.c_str(), i + 1,
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
        if (argc > 3) {
            throw std::invalid_argument(
                "Usage: CudaContainment.exe [steps] [computers]");
        }

        int steps = argc >= 2
            ? parseNumber(argv[1], 0, 10000, "Steps") : 7;

        int count = argc == 3
            ? parseNumber(argv[2], 3, 10000, "Computers") : 8;

        // Lower middle for even sizes, preserving Computer 4 for size 8.
        int middle = (count + 1) / 2;

        std::printf("Computers: %d\n", count);
        std::printf("Simulation steps: %d\n", steps);
        std::printf("Initially compromised: Computer 1\n");
        std::printf("Middle defence target: Computer %d\n\n", middle);

        std::printf("%-26s %8s %8s %8s %10s\n",
                    "Scenario", "CPU", "GPU", "Match", "Expected");

        bool baseline = runScenario(
            "No defence", 0, 0, count, steps);

        bool source = runScenario(
            "Isolate Computer 1", 1, 0, count, steps);

        bool middleIsolation = runScenario(
            "Isolate Computer " + std::to_string(middle),
            middle, 0, count, steps);

        bool protection = runScenario(
            "Protect Computer " + std::to_string(middle),
            0, middle, count, steps);

        bool passed =
            baseline && source && middleIsolation && protection;

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