# Synchronized Task Queue

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Assign independent simulation tasks to available workers using
a shared queue instead of fixed worker assignments.

## TaskQueue
The queue contains a predefined array of tasks and a nextIndex.

The synchronized poll() method checks for an available task,
advances the index, and returns the task.

Only one worker can perform these actions on the queue at a time.
Each array entry is handed out at most once.
An empty queue returns null.

## QueueWorker
A worker takes a task, executes it, and requests another.
It exits when the queue returns null.

Task execution happens outside the synchronized method, allowing
different workers to execute different simulations concurrently.

## State Ownership
Workers share the queue.
Each scenario task owns a separate network.
The main thread reads results after joining all workers.

## Current Scope
All tasks are loaded before workers start.
No new tasks are added during execution.
An empty queue therefore means there is no more work to assign.

This is a finite batch queue. A producer-consumer implementation
with tasks arriving later would require additional coordination.

## Verification
QueuedComparison uses a 100-computer chain and 99 steps.
Expected totals are 100, 1, and 50.

Run with 1, 2, and 3 workers and compare final compromised totals
against fresh sequential executions.

## Limitations
Worker-to-task assignments depend on scheduling.
Dynamic assignment may improve balance but does not guarantee speedup.
This demonstration does not measure performance.
PerformanceBenchmark still uses static assignment.