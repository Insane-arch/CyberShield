# Socket-Based Simulation Execution

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Purpose
Execute a simulation in a separate Java process using TCP sockets.

## Programs
WorkerServer listens on 127.0.0.1:5000.
CoordinatorClient connects and sends a simulation task.

Both programs run on the same laptop in separate JVMs.
They exchange data through sockets rather than sharing Java objects.

## Request
The coordinator sends four binary integers in order:
1. Task ID.
2. Network size.
3. Simulation steps.
4. Scenario code.

Scenario codes: 0 = no defence, 1 = isolation, 2 = protection.

## Response
The worker returns:
1. Task ID.
2. Final compromised count.

The coordinator checks the task ID and compares the result
against a fresh local execution.

## Demonstration
Task: 100 computers, 99 steps, protection scenario.
Expected local result: 50.
Expected worker result: 50.

Start WorkerServer first, then CoordinatorClient in another terminal.

## Lifecycle and Limitations
The worker handles one task and exits.
Restart it before repeating the demonstration.

The server binds only to the local loopback address.
Cross-machine execution and multiple workers are not implemented yet.

The coordinator has a five-second connection timeout.
Both connections have ten-second blocking read timeouts.
The worker's initial wait for a connection has no timeout.
Read timeouts do not limit simulation computation time.

Sockets and streams are closed using try-with-resources.
Automatic retry and distributed performance measurement are not
implemented.