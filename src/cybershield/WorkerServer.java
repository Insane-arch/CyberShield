package cybershield;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class WorkerServer {

    public static void main(String[] args) throws IOException {
        System.out.println("CyberShield - Worker Server");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress("127.0.0.1", 5000));

            System.out.println(
                    "\nWaiting for coordinator on 127.0.0.1:5000");

            try (Socket connection = server.accept();
                    DataInputStream input = new DataInputStream(
                            connection.getInputStream());
                    DataOutputStream output = new DataOutputStream(
                            connection.getOutputStream())) {

                connection.setSoTimeout(10000);

                int taskId = input.readInt();
                int size = input.readInt();
                int steps = input.readInt();
                int scenario = input.readInt();

                if (taskId < 1 || size < 3 || steps < 0
                        || scenario < NetworkFactory.NO_DEFENCE
                        || scenario > NetworkFactory.PROTECTION) {

                    throw new IOException("Invalid task parameters.");
                }

                System.out.println("Received task: " + taskId);

                Network network =
                        NetworkFactory.createChainScenario(size, scenario);

                ScenarioTask task = new ScenarioTask(network, steps);
                task.run();

                int result = task.getCompromisedCount();

                output.writeInt(taskId);
                output.writeInt(result);
                output.flush();

                System.out.println(
                        "Result sent: " + result + " compromised.");
            }
        }

        System.out.println("Worker finished.");
    }
}