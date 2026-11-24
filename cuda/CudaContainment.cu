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

// Chain topology: each computer connects to its immediate neighbours.
// Read only the current state; write each computer's next state separately.
__global__ void spreadOneStep(
    const int* current,
    int* next,
    int count
) {
    int index = blockIdx.x * blockDim.x + threadIdx.x;

    if (index >= count) {
        return;
    }

    bool compromised = current[index] != 0;

    if (index > 0 && current[index - 1] != 0) {
        compromised = true;
    }

    if (index + 1 < count && current[index + 1] != 0) {
        compromised = true;
    }

    next[index] = compromised ? 1 : 0;
}

int main() {
    std::printf("CyberShield - CUDA Containment: One Step\n");
    std::printf("Muhammad Hamza Naseer - 247121\n");
    std::printf("Sultan Majeed - 247187\n\n");

    constexpr int count = 8;
    constexpr size_t bytes = count * sizeof(int);

    int initial[count] = {1, 0, 0, 0, 0, 0, 0, 0};
    int result[count] = {};
    const int expected[count] = {1, 1, 0, 0, 0, 0, 0, 0};

    int* deviceCurrent = nullptr;
    int* deviceNext = nullptr;

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceCurrent), bytes),
        "Allocate current states");

    checkCuda(cudaMalloc(
        reinterpret_cast<void**>(&deviceNext), bytes),
        "Allocate next states");

    checkCuda(cudaMemcpy(deviceCurrent, initial, bytes,
                         cudaMemcpyHostToDevice),
              "Copy initial states");

    constexpr int threadsPerBlock = 128;
    int blocks = (count + threadsPerBlock - 1) / threadsPerBlock;

    spreadOneStep<<<blocks, threadsPerBlock>>>(
        deviceCurrent, deviceNext, count);

    checkCuda(cudaGetLastError(), "Launch propagation kernel");
    checkCuda(cudaDeviceSynchronize(), "Complete propagation");

    checkCuda(cudaMemcpy(result, deviceNext, bytes,
                         cudaMemcpyDeviceToHost),
              "Copy next states");

    checkCuda(cudaFree(deviceCurrent), "Free current states");
    checkCuda(cudaFree(deviceNext), "Free next states");

    bool passed = true;
    int compromisedCount = 0;

    std::printf("Computer   Initial   After step   Expected\n");

    for (int i = 0; i < count; ++i) {
        std::printf("%8d %9d %12d %10d\n",
                    i + 1, initial[i], result[i], expected[i]);

        compromisedCount += result[i];

        if (result[i] != expected[i]) {
            passed = false;
        }
    }

    std::printf("\nCompromised computers: %d\n", compromisedCount);
    std::printf("Verification: %s\n", passed ? "PASS" : "FAIL");

    return passed ? EXIT_SUCCESS : EXIT_FAILURE;
}