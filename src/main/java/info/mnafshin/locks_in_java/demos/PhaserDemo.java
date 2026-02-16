package info.mnafshin.locks_in_java.demos;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demo: Phaser
 * 
 * Use Case: Complex multi-phase synchronization with dynamic parties
 * Use Case: Dynamic group of threads that can join/leave at runtime
 * Best For: Advanced scenarios where thread count changes during execution
 */
public class PhaserDemo {

    /**
     * ✅ BEST PRACTICE: Multi-Phase Synchronization
     * Different threads participate in different phases
     */
    public static class MultiPhaseExecution {
        private final Phaser phaser;
        private final int numPhases;

        public MultiPhaseExecution(int initialParties, int numPhases) {
            this.phaser = new Phaser(initialParties);
            this.numPhases = numPhases;
        }

        public void execute() throws InterruptedException {
            System.out.println("Starting multi-phase execution with " + 
                phaser.getRegisteredParties() + " parties");

            Thread[] threads = new Thread[phaser.getRegisteredParties() - 1];

            for (int i = 0; i < threads.length; i++) {
                final int workerId = i;
                threads[i] = new Thread(() -> {
                    try {
                        for (int phase = 0; phase < numPhases; phase++) {
                            System.out.println("[Worker-" + workerId + "] " +
                                "Phase " + phase + ": Working(phase=" + 
                                phaser.getPhase() + ")");
                            
                            // Simulate phase work
                            Thread.sleep((long)(Math.random() * 1000) + 500);
                            
                            System.out.println("[Worker-" + workerId + "] " +
                                "Phase " + phase + ": Advancing");
                            
                            phaser.arriveAndAwaitAdvance();
                        }

                        System.out.println("[Worker-" + workerId + "] All phases complete!");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "Worker-" + workerId);
            }

            for (Thread t : threads) {
                t.start();
            }

            // Main thread coordinates phases
            for (int phase = 0; phase < numPhases; phase++) {
                System.out.println("\n>>> PHASE " + phase + " ACTIVE <<<");
                Thread.sleep(500); // Give workers time
                
                System.out.println(">>> MAIN: Advancing from phase " + phase);
                phaser.arriveAndAwaitAdvance();
            }

            for (Thread t : threads) {
                t.join();
            }

            System.out.println("Phaser terminated: " + phaser.isTerminated());
        }
    }

    /**
     * ✅ BEST PRACTICE: Dynamic Party Registration
     * Workers can register and deregister dynamically
     */
    public static class DynamicPartyPhaser {
        private final Phaser phaser;

        public DynamicPartyPhaser() {
            // Start with just the main thread
            this.phaser = new Phaser(1);
        }

        public void registerWorker(String taskName) {
            phaser.register();
            System.out.println("Registered worker for: " + taskName + 
                " (Total parties: " + phaser.getRegisteredParties() + ")");

            new Thread(() -> {
                try {
                    for (int i = 0; i < 3; i++) {
                        System.out.println("[" + taskName + "] Phase " + i + " work");
                        Thread.sleep((long)(Math.random() * 1000) + 500);
                        
                        System.out.println("[" + taskName + "] Advancing from phase " + i);
                        phaser.arriveAndAwaitAdvance();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    phaser.arriveAndDeregister(); // Deregister when done
                    System.out.println("[" + taskName + "] Deregistered " +
                        "(Remaining parties: " + phaser.getRegisteredParties() + ")");
                }
            }, taskName).start();
        }

        public void coordinatePhases() throws InterruptedException {
            for (int phase = 0; phase < 3; phase++) {
                System.out.println("\n=== PHASE " + phase + " COORDINATION ===");
                System.out.println("Registered parties: " + phaser.getRegisteredParties());
                
                Thread.sleep(500);
                
                System.out.println("Main thread advancing from phase " + phase);
                phaser.arriveAndAwaitAdvance();
            }

            // Wait for everyone to finish
            System.out.println("\nWaiting for all workers to complete...");
            phaser.arriveAndDeregister(); // Deregister main thread
            
            // Wait with timeout
            try {
                int result = phaser.awaitAdvanceInterruptibly(
                    phaser.getPhase(), 5, TimeUnit.SECONDS);
                System.out.println("Phaser phase when returned: " + result);
            } catch (java.util.concurrent.TimeoutException e) {
                System.out.println("Timeout waiting for phaser completion");
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Phaser with advancing callback
     * Custom actions when phases complete
     */
    public static class PhaserWithCompletion extends Phaser {
        private final AtomicInteger phaseCount = new AtomicInteger(0);

        public PhaserWithCompletion(int parties) {
            super(parties);
        }

        // Override onAdvance to execute custom logic
        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            int currentPhase = phaseCount.incrementAndGet();
            System.out.println("\n✅ PHASE " + phase + " COMPLETED " +
                "(Parties: " + registeredParties + ", Total phases: " + currentPhase + ")");
            
            // Return true to terminate the phaser
            return registeredParties == 0;
        }
    }

    /**
     * ✅ BEST PRACTICE: Wave synchronization pattern
     * Parties advance in waves, controlled by phases
     */
    public static class WaveSynchronization {
        private final Phaser phaser;
        private final int waves;

        public WaveSynchronization(int numWorkers, int waves) {
            this.phaser = new Phaser(numWorkers + 1); // +1 for main thread
            this.waves = waves;
        }

        public void executeWaves() throws InterruptedException {
            System.out.println("Starting wave synchronization (" + waves + " waves)");

            Thread[] workers = new Thread[phaser.getRegisteredParties() - 1];

            for (int i = 0; i < workers.length; i++) {
                final int workerId = i;
                workers[i] = new Thread(() -> {
                    try {
                        for (int wave = 0; wave < waves; wave++) {
                            long workTime = (long)(Math.random() * 1000) + 300;
                            System.out.println("[Wave " + wave + "] Worker-" + workerId + 
                                " processing (" + workTime + "ms)");
                            Thread.sleep(workTime);
                            
                            System.out.println("[Wave " + wave + "] Worker-" + workerId + 
                                " completed, waiting for wave");
                            phaser.arriveAndAwaitAdvance();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "Worker-" + workerId);
            }

            long startTime = System.currentTimeMillis();

            for (Thread w : workers) {
                w.start();
            }

            // Main thread coordinates waves
            for (int wave = 0; wave < waves; wave++) {
                System.out.println("\n🌊 WAVE " + wave + " STARTING");
                Thread.sleep(300);
                
                System.out.println("🌊 WAVE " + wave + " COORDINATING");
                phaser.arriveAndAwaitAdvance();
            }

            // Wait for all workers
            for (Thread w : workers) {
                w.join();
            }

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("\n✅ All waves completed in " + duration + "ms");
        }
    }

    /**
     * ✅ BEST PRACTICE: Timeout-aware phaser usage
     */
    public static class TimeoutPhaserExample {
        private final Phaser phaser;
        private final int timeoutSeconds;

        public TimeoutPhaserExample(int parties, int timeoutSeconds) {
            this.phaser = new Phaser(parties);
            this.timeoutSeconds = timeoutSeconds;
        }

        public void executeWithTimeout() throws InterruptedException {
            Thread slowWorker = new Thread(() -> {
                try {
                    System.out.println("Slow worker: Taking long time...");
                    Thread.sleep(5000); // 5 seconds
                    System.out.println("Slow worker: Advancing");
                    phaser.arriveAndAwaitAdvance();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "SlowWorker");

            Thread fastWorker = new Thread(() -> {
                try {
                    System.out.println("Fast worker: Quick work...");
                    Thread.sleep(500);
                    System.out.println("Fast worker: Advancing");
                    phaser.arriveAndAwaitAdvance();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "FastWorker");

            slowWorker.start();
            fastWorker.start();

            try {
                System.out.println("Main: Waiting for phase advancement (timeout: " + 
                    timeoutSeconds + "s)");
                phaser.awaitAdvanceInterruptibly(0, timeoutSeconds, TimeUnit.SECONDS);
                System.out.println("Main: Phase advanced successfully");
            } catch (java.util.concurrent.TimeoutException e) {
                System.out.println("Main: Timeout waiting for workers!");
            }

            slowWorker.join();
            fastWorker.join();
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== PHASER DEMO ===\n");

        // Demo 1: Multi-Phase Execution
        System.out.println("--- Demo 1: Multi-Phase Execution ---");
        demoMultiPhase();

        // Demo 2: Dynamic Party Registration
        System.out.println("\n--- Demo 2: Dynamic Party Registration ---");
        demoDynamicParties();

        // Demo 3: Wave Synchronization
        System.out.println("\n--- Demo 3: Wave Synchronization ---");
        demoWaves();

        // Demo 4: Timeout-aware
        System.out.println("\n--- Demo 4: Timeout-Aware Phaser ---");
        demoTimeout();
    }

    private static void demoMultiPhase() throws InterruptedException {
        MultiPhaseExecution execution = new MultiPhaseExecution(3, 3);
        execution.execute();
    }

    private static void demoDynamicParties() throws InterruptedException {
        DynamicPartyPhaser dynamicPhaser = new DynamicPartyPhaser();

        // Dynamically register workers
        dynamicPhaser.registerWorker("Task-A");
        Thread.sleep(500);
        dynamicPhaser.registerWorker("Task-B");
        Thread.sleep(500);
        dynamicPhaser.registerWorker("Task-C");

        dynamicPhaser.coordinatePhases();
    }

    private static void demoWaves() throws InterruptedException {
        WaveSynchronization waves = new WaveSynchronization(3, 4);
        waves.executeWaves();
    }

    private static void demoTimeout() throws InterruptedException {
        TimeoutPhaserExample timeoutExample = new TimeoutPhaserExample(2, 2);
        timeoutExample.executeWithTimeout();
    }
}
