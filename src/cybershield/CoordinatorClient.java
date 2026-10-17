package cybershield;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class CoordinatorClient {

    public static void main(String[] args) throws IOException {
        int taskId = 1;
        int size = 100;
        int steps = 99;
        int scenario = NetworkFactory.PROTECTION;

        System.out.println("CyberShield - Coordinator Client");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        int returnedTaskId;
        int workerResult;

        try (Socket connection = new Socket()) {
            connection.connect(
                    new InetSocketAddress("127.0.0.1", 5000), 5000);

            connection.setSoTimeout(10000);

            try (DataInputStream input = new DataInputStream(
                        connection.getInputStream());
                    DataOutputStream output = new DataOutputStream(
                        connection.getOutputStream())) {

                output.writeInt(taskId);
                output.writeInt(size);
                output.writeInt(steps);
                output.writeInt(scenario);
                output.flush();

                System.out.println("\nSent task: " + taskId);

                returnedTaskId = input.readInt();
                workerResult = input.readInt();
            }
        }

        if (returnedTaskId != taskId) {
            throw new IOException("Received a result for the wrong task.");
        }

        Network network =
                NetworkFactory.createChainScenario(size, scenario);

        ScenarioTask localTask = new ScenarioTask(network, steps);
        localTask.run();

        int localResult = localTask.getCompromisedCount();

        System.out.println("Local result: " + localResult);
        System.out.println("Worker result: " + workerResult);

        if (localResult != workerResult) {
            throw new AssertionError(
                    "Worker result does not match local execution.");
        }

        System.out.println("MATCH: Worker result equals local result.");
    }
}