# Computer Node Model

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Purpose
ComputerNode represents one computer in the simulated network.

## Stored Information
- ID: a permanent identifier.
- Compromised: whether the computer has been compromised.
- Isolated: whether communication has been disabled.
- Protected: whether a defensive protection has been applied.

## Design
Fields are private and accessed through public methods.
Each new computer starts healthy, connected, and unprotected.
Isolation and protection are separate from compromise status.
Node IDs will be assigned uniquely when building the network.

## Current Demonstration
Main creates two computers and prints their initial states.
It then protects the first computer and compromises and isolates
the second computer before printing their updated states.

Attack propagation and the effects of protection are not
implemented yet.