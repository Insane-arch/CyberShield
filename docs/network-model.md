# Network Model

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Purpose
Network stores the simulated computers and their connections.

## Data Structures
- ComputerNode[] stores the computers.
- boolean[][] stores the adjacency matrix.
- Computer IDs range from 1 to the network size.
- Array indexes range from 0 to size minus one.

## Connection Rules
Connections are bidirectional.
Connecting A to B also connects B to A.
Self-connections are rejected.
Adding an existing connection leaves the network unchanged.
Invalid IDs and non-positive network sizes are rejected.

## Separation of Responsibilities
Network stores computers and topology.
ComputerNode stores each computer's state.
Main builds the demonstration and displays the results.

The adjacency matrix records topology. Isolation does not
delete a connection; later simulation rules will check whether
connected computers are allowed to communicate.

## Demonstration
Five computers are connected using these pairs:
1-2, 1-3, 2-4, 3-4, and 4-5.

Expected output:
- Five healthy, unprotected, non-isolated computers.
- A symmetric connection matrix.
- Zeros on the diagonal.
- Ten entries containing 1, representing five bidirectional links.

## Limitation
The matrix requires space proportional to the square of the
number of computers. It is intended for our modest simulation sizes.