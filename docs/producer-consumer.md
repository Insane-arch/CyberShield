# Producer-Consumer Simulation

Project members:
- Muhammad Hamza Naseer — 247121
- Sultan Majeed — 247187

## Objective
Allow tasks to be submitted while consumer threads are running,
using a bounded queue and Java monitor synchronization.

## Roles
The main thread produces three simulation tasks.
Two consumer threads take and execute tasks.
Each task owns a separate network.

## Queue Implementation
BlockingTaskQueue uses a circular array.
head identifies the next removal position.
tail identifies the next insertion position.
count records the number of waiting tasks.

The demonstration uses capacity 1.
This limits waiting tasks, not tasks already running.

## Coordination
put() waits while the queue is full and open.
take() waits while the queue is empty and open.

Both methods use synchronized, wait(), and notifyAll().
Waiting releases the queue lock.
Conditions are checked in while loops after waking.

Task execution occurs outside the queue lock.

## Completion
The producer closes the queue after submitting its tasks.
Closing prevents new submissions and wakes waiting threads.
Consumers can still take tasks remaining in a closed queue.
An empty, closed queue returns null so consumers can finish.

The main thread joins the consumers before reading their results.

## Verification
Use a 100-computer chain and 99 steps.
Expected totals: 100, 1, and 50.
Compare each result with a fresh sequential execution.

This demonstration checks normal completion and result agreement.
It does not prove that every possible waiting or interruption path
occurred during the run.

## Current Limitations
No performance measurement is included.
Automatic recovery from failed consumer threads is not implemented.
The existing finite-batch queue remains available separately.