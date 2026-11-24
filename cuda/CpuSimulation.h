#ifndef CYBERSHIELD_CPU_SIMULATION_H
#define CYBERSHIELD_CPU_SIMULATION_H

#include <stdexcept>
#include <vector>

// Sequential chain simulation.
// Reads one step's states and writes the next step separately.
inline std::vector<int> simulateCpu(
    const std::vector<int>& initial,
    const std::vector<int>& isolated,
    const std::vector<int>& protectedNodes,
    int steps
) {
    if (initial.empty() ||
        initial.size() != isolated.size() ||
        initial.size() != protectedNodes.size()) {
        throw std::invalid_argument(
            "State and defence arrays must have equal, nonzero sizes.");
    }

    if (steps < 0) {
        throw std::invalid_argument("Steps cannot be negative.");
    }

    for (std::size_t i = 0; i < initial.size(); ++i) {
        if ((initial[i] != 0 && initial[i] != 1) ||
            (isolated[i] != 0 && isolated[i] != 1) ||
            (protectedNodes[i] != 0 && protectedNodes[i] != 1)) {
            throw std::invalid_argument("States and flags must be 0 or 1.");
        }
    }

    std::vector<int> current = initial;

    for (int step = 0; step < steps; ++step) {
        std::vector<int> next = current;

        // Visit each compromised source and its immediate neighbours.
        for (std::size_t source = 0; source < current.size(); ++source) {
            if (current[source] == 0 || isolated[source] != 0) {
                continue;
            }

            if (source > 0) {
                std::size_t target = source - 1;

                if (isolated[target] == 0 && protectedNodes[target] == 0) {
                    next[target] = 1;
                }
            }

            if (source + 1 < current.size()) {
                std::size_t target = source + 1;

                if (isolated[target] == 0 && protectedNodes[target] == 0) {
                    next[target] = 1;
                }
            }
        }

        current.swap(next);
    }

    return current;
}

#endif