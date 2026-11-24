#include <cuda_runtime.h>
#include <cstdio>
#include <cstdlib>

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
    int count
) {
    int index = blockIdx.x * blockDim.x + threadIdx.x;

    if (index >= count) {
        return;
    }

    next[index] = current[index];

    if (current[index] != 0 || isolated[index] != 0) {
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
    const int* expected
) {
    constexpr int count = 8;
    constexpr int steps = 7;
    constexpr size_t bytes = count * sizeof(int);

    int initial[count] = {1, 0, 0, 0, 0, 0, 0, 0};
    int isolated[count] = {};
    int result[count] = {};

    // Zero means no isolation; otherwise use a one-based computer ID.
    if (isolatedComputer < 0 || isolatedComputer > count) {
        std::fprintf(stderr, "Invalid isolated computer ID.\n");
        return false;
    }

    if (isolatedComputer > 0) {
        isolated[isolatedComputer - 1] = 1;
    }

    int* deviceCurrent = nullptr;
    int* deviceNext = nullptr;
    int* deviceIsolated = nullptr;

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceCurrent), bytes),
        "Allocate current states");

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceNext), bytes),
        "Allocate next states");

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceIsolated), bytes),
        "Allocate isolation flags");

    checkCuda(cudaMemcpy(deviceCurrent, initial, bytes,
                         cudaMemcpyHostToDevice),
              "Copy initial states");

    checkCuda(cudaMemcpy(deviceIsolated, isolated, bytes,
                         cudaMemcpyHostToDevice),
              "Copy isolation flags");

    constexpr int threadsPerBlock = 128;
    int blocks = (count + threadsPerBlock - 1) / threadsPerBlock;

    for (int step = 0; step < steps; ++step) {
        spreadOneStep<<<blocks, threadsPerBlock>>>(
            deviceCurrent, deviceNext, deviceIsolated, count);

        checkCuda(cudaGetLastError(), "Launch propagation kernel");
        checkCuda(cudaDeviceSynchronize(), "Complete propagation step");

        int* temporary = deviceCurrent;
        deviceCurrent = deviceNext;
        deviceNext = temporary;
    }

    checkCuda(cudaMemcpy(result, deviceCurrent, bytes,
                         cudaMemcpyDeviceToHost),
              "Copy final states");

    checkCuda(cudaFree(deviceCurrent), "Free current states");
    checkCuda(cudaFree(deviceNext), "Free next states");
    checkCuda(cudaFree(deviceIsolated), "Free isolation flags");

    bool passed = true;
    int actualTotal = 0;
    int expectedTotal = 0;

    for (int i = 0; i < count; ++i) {
        actualTotal += result[i];
        expectedTotal += expected[i];

        if (result[i] != expected[i]) {
            passed = false;
            std::fprintf(
                stderr,
                "%s: Computer %d expected %d but received %d\n",
                name, i + 1, expected[i], result[i]);
        }
    }

    std::printf("%-24s %10d %10d %8s\n",
                name, actualTotal, expectedTotal,
                passed ? "PASS" : "FAIL");

    return passed;
}

int main() {
    std::printf("CyberShield - CUDA Isolation Checks\n");
    std::printf("Muhammad Hamza Naseer - 247121\n");
    std::printf("Sultan Majeed - 247187\n\n");

    std::printf("Computers: 8\n");
    std::printf("Simulation steps: 7\n");
    std::printf("Initially compromised: Computer 1\n\n");

    const int noDefenceExpected[8] =
        {1, 1, 1, 1, 1, 1, 1, 1};

    const int sourceIsolationExpected[8] =
        {1, 0, 0, 0, 0, 0, 0, 0};

    const int middleIsolationExpected[8] =
        {1, 1, 1, 0, 0, 0, 0, 0};

    std::printf("%-24s %10s %10s %8s\n",
                "Scenario", "GPU total", "Expected", "Check");

    // Run every scenario, even if an earlier verification fails.
    bool noDefencePassed =
        runScenario("No defence", 0, noDefenceExpected);

    bool sourcePassed =
        runScenario("Isolate Computer 1", 1, sourceIsolationExpected);

    bool middlePassed =
        runScenario("Isolate Computer 4", 4, middleIsolationExpected);

    bool allPassed = noDefencePassed && sourcePassed && middlePassed;

    std::printf("\n%s\n",
                allPassed
                    ? "All 3 CUDA scenario checks passed."
                    : "CUDA scenario verification failed.");

    return allPassed ? EXIT_SUCCESS : EXIT_FAILURE;
}