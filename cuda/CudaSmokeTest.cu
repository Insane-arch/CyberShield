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

__global__ void doubleValues(int* values, int count) {
    int index = blockIdx.x * blockDim.x + threadIdx.x;

    if (index < count) {
        values[index] *= 2;
    }
}

int main() {
    std::printf("CyberShield - CUDA Verification\n");
    std::printf("Muhammad Hamza Naseer - 247121\n");
    std::printf("Sultan Majeed - 247187\n\n");

    cudaDeviceProp properties{};
    checkCuda(cudaGetDeviceProperties(&properties, 0),
              "Read GPU information");
    checkCuda(cudaSetDevice(0), "Select GPU");

    std::printf("GPU: %s\n", properties.name);

    constexpr int count = 8;
    int values[count] = {1, 2, 3, 4, 5, 6, 7, 8};
    int* deviceValues = nullptr;

    checkCuda(cudaMalloc(reinterpret_cast<void**>(&deviceValues),
                         sizeof(values)), "Allocate GPU memory");

    checkCuda(cudaMemcpy(deviceValues, values, sizeof(values),
                         cudaMemcpyHostToDevice), "Copy input to GPU");

    doubleValues<<<1, 32>>>(deviceValues, count);
    checkCuda(cudaGetLastError(), "Launch GPU kernel");
    checkCuda(cudaDeviceSynchronize(), "Complete GPU computation");

    checkCuda(cudaMemcpy(values, deviceValues, sizeof(values),
                         cudaMemcpyDeviceToHost), "Copy result to CPU");

    checkCuda(cudaFree(deviceValues), "Free GPU memory");

    bool passed = true;
    std::printf("Results:");

    for (int i = 0; i < count; ++i) {
        std::printf(" %d", values[i]);
        if (values[i] != (i + 1) * 2) {
            passed = false;
        }
    }

    std::printf("\nVerification: %s\n", passed ? "PASS" : "FAIL");
    return passed ? EXIT_SUCCESS : EXIT_FAILURE;
}