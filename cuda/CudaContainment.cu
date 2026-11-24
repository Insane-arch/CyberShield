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

// An isolated computer cannot send or receive the attack.
// Each thread writes one next-state value.
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

    // Existing compromises persist, including on isolated computers.
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

int main() {
    std::printf("CyberShield - CUDA Containment: Isolation\n");
    std::printf("Muhammad Hamza Naseer - 247121\n");
    std::printf("Sultan Majeed - 247187\n\n");

    constexpr int count = 8;
    constexpr int steps = 7;
    constexpr size_t bytes = count * sizeof(int);

    int initial[count] = {1, 0, 0, 0, 0, 0, 0, 0};
    int isolated[count] = {1, 0, 0, 0, 0, 0, 0, 0};
    int result[count] = {};

    const int expected[count] = {1, 0, 0, 0, 0, 0, 0, 0};

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
    int compromisedCount = 0;

    std::printf("Computers: %d\n", count);
    std::printf("Simulation steps: %d\n", steps);
    std::printf("Isolated computer: 1\n\n");

    std::printf("Computer   Initial   Isolated   Final   Expected\n");

    for (int i = 0; i < count; ++i) {
        std::printf("%8d %9d %10d %7d %10d\n",
                    i + 1, initial[i], isolated[i],
                    result[i], expected[i]);

        compromisedCount += result[i];

        if (result[i] != expected[i]) {
            passed = false;
        }
    }

    std::printf("\nCompromised computers: %d\n", compromisedCount);
    std::printf("Verification: %s\n", passed ? "PASS" : "FAIL");

    return passed ? EXIT_SUCCESS : EXIT_FAILURE;
}