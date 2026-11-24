#include "CpuSimulation.h"
#include <cstdio>
#include <cstdlib>
#include <exception>
#include <numeric>
#include <vector>

bool checkScenario(
    const char* name,
    int isolatedComputer,
    int protectedComputer,
    const std::vector<int>& expected
) {
    constexpr int count = 8;
    constexpr int steps = 7;

    std::vector<int> initial(count, 0);
    std::vector<int> isolated(count, 0);
    std::vector<int> protectedNodes(count, 0);

    initial[0] = 1;

    if (isolatedComputer > 0) {
        isolated.at(isolatedComputer - 1) = 1;
    }

    if (protectedComputer > 0) {
        protectedNodes.at(protectedComputer - 1) = 1;
    }

    std::vector<int> actual =
        simulateCpu(initial, isolated, protectedNodes, steps);

    bool passed = actual == expected;
    int actualTotal = std::accumulate(actual.begin(), actual.end(), 0);
    int expectedTotal =
        std::accumulate(expected.begin(), expected.end(), 0);

    std::printf("%-24s %10d %10d %8s\n",
                name, actualTotal, expectedTotal,
                passed ? "PASS" : "FAIL");

    if (!passed) {
        for (std::size_t i = 0; i < actual.size(); ++i) {
            if (actual[i] != expected.at(i)) {
                std::fprintf(
                    stderr,
                    "%s: Computer %zu expected %d but received %d\n",
                    name, i + 1, expected.at(i), actual[i]);
            }
        }
    }

    return passed;
}

int main() {
    std::printf("CyberShield - CPU Reference Checks\n");
    std::printf("Muhammad Hamza Naseer - 247121\n");
    std::printf("Sultan Majeed - 247187\n\n");

    std::printf("Computers: 8\n");
    std::printf("Simulation steps: 7\n\n");

    std::printf("%-24s %10s %10s %8s\n",
                "Scenario", "CPU total", "Expected", "Check");

    try {
        bool baseline = checkScenario(
            "No defence", 0, 0,
            {1, 1, 1, 1, 1, 1, 1, 1});

        bool source = checkScenario(
            "Isolate Computer 1", 1, 0,
            {1, 0, 0, 0, 0, 0, 0, 0});

        bool middle = checkScenario(
            "Isolate Computer 4", 4, 0,
            {1, 1, 1, 0, 0, 0, 0, 0});

        bool protection = checkScenario(
            "Protect Computer 4", 0, 4,
            {1, 1, 1, 0, 0, 0, 0, 0});

        bool passed = baseline && source && middle && protection;

        std::printf("\n%s\n",
                    passed
                        ? "All 4 CPU reference checks passed."
                        : "CPU reference verification failed.");

        return passed ? EXIT_SUCCESS : EXIT_FAILURE;
    } catch (const std::exception& error) {
        std::fprintf(stderr, "Error: %s\n", error.what());
        return EXIT_FAILURE;
    }
}