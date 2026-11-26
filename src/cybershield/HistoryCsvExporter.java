package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class HistoryCsvExporter {

    private HistoryCsvExporter() {
    }

    public static void write(
            Path destination,
            List<SimulationHistory.HistoryEntry> history)
            throws IOException {

        if (destination == null || history == null) {
            throw new IllegalArgumentException(
                    "Destination and history cannot be null.");
        }

        // Validate before opening or replacing the output file.
        for (SimulationHistory.HistoryEntry entry : history) {
            if (entry == null) {
                throw new IllegalArgumentException(
                        "History cannot contain null entries.");
            }
        }

        Path absoluteDestination = destination.toAbsolutePath();
        Path parent = absoluteDestination.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                absoluteDestination,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            writer.write("step,newly_compromised,total_compromised,healthy");
            writer.newLine();

            for (SimulationHistory.HistoryEntry entry : history) {
                writer.write(
                        entry.step() + ","
                        + entry.newlyCompromised() + ","
                        + entry.totalCompromised() + ","
                        + entry.healthy());

                writer.newLine();
            }
        }
    }
}