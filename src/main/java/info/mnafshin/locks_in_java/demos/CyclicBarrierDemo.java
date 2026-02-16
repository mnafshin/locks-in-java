package info.mnafshin.locks_in_java.demos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demo: CyclicBarrier
 * 
 * Use Case: Reusable synchronization point where threads wait for each other
 * Use Case: Iterative algorithms where all threads need to sync between iterations
 * Best For: Matrix operations, game rounds, batch processing
 */
public class CyclicBarrierDemo {

    /**
     * ✅ BEST PRACTICE: Barrier Action executed when all parties arrive
     * Demonstrates the barrier action callback
     */
    public static class BarrierWithAction {
        private final CyclicBarrier barrier;
        private final String[] threadStatus;

        public BarrierWithAction(int numThreads) {
            this.threadStatus = new String[numThreads];

            // Barrier action: executed when all parties arrive
            this.barrier = new CyclicBarrier(numThreads, () -> {
                System.out.println("\n=== ALL THREADS REACHED BARRIER ===");
                System.out.println("Barrier Action: Processing synchronized state");
                for (int i = 0; i < threadStatus.length; i++) {
                    System.out.println("  Thread-" + i + ": " + threadStatus[i]);
                }
                System.out.println("Continuing to next phase...\n");
            });
        }

        public void executeWithBarrier(int numThreads) throws InterruptedException {
            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    try {
                        for (int phase = 0; phase < 3; phase++) {
                            // Do work
                            threadStatus[threadId] = "Phase " + phase + " completed";
                            System.out.println("[Thread-" + threadId + "] Completed phase " + phase);

                            // Wait for others
                            System.out.println("[Thread-" + threadId + "] Waiting at barrier...");
                            barrier.await();
                            System.out.println("[Thread-" + threadId + "] Passed barrier, moving to next phase");

                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (BrokenBarrierException e) {
                        System.out.println("Barrier broken!");
                    }
                }, "Thread-" + threadId);
            }

            for (Thread t : threads) {
                t.start();
            }

            for (Thread t : threads) {
                t.join();
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Iterative Matrix Operations
     * Each row of workers synchronize after each iteration
     */
    public static class MatrixIterativeProcessor {
        private final int numWorkers;
        private final CyclicBarrier barrier;
        private final AtomicInteger iterationCount = new AtomicInteger(0);

        public MatrixIterativeProcessor(int numWorkers) {
            this.numWorkers = numWorkers;
            this.barrier = new CyclicBarrier(numWorkers, () -> {
                int iteration = iterationCount.incrementAndGet();
                System.out.println(">>> Iteration " + iteration + " complete! All threads synchronized");
            });
        }

        public void processIteratively() throws InterruptedException {
            Thread[] workers = new Thread[numWorkers];

            for (int i = 0; i < numWorkers; i++) {
                final int workerId = i;
                workers[i] = new Thread(() -> {
                    try {
                        for (int iteration = 0; iteration < 3; iteration++) {
                            // Simulate work
                            double computationTime = Math.random() * 1000;
                            System.out.println("[Worker-" + workerId + "] " +
                                "Iteration " + iteration + " work time: " + 
                                String.format("%.0f", computationTime) + "ms");
                            
                            Thread.sleep((long)computationTime);

                            // Synchronize with other workers
                            System.out.println("[Worker-" + workerId + "] " +
                                "Waiting for other workers...");
                            barrier.await();

                            System.out.println("[Worker-" + workerId + "] " +
                                "All synchronized, continuing...");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (BrokenBarrierException e) {
                        System.out.println("Barrier broken!");
                    }
                }, "Worker-" + workerId);
            }

            long startTime = System.currentTimeMillis();

            for (Thread w : workers) {
                w.start();
            }

            for (Thread w : workers) {
                w.join();
            }

            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("Total processing time: " + totalTime + "ms");
        }
    }

    /**
     * ✅ BEST PRACTICE: Game Round Coordinator
     * Players wait for each other between game rounds
     */
    public static class GameRoundCoordinator {
        private final CyclicBarrier roundBarrier;
        private final int numPlayers;
        private final AtomicInteger roundNumber = new AtomicInteger(0);
        private final AtomicInteger winnerCount = new AtomicInteger(0);

        public GameRoundCoordinator(int numPlayers) {
            this.numPlayers = numPlayers;
            this.roundBarrier = new CyclicBarrier(numPlayers, () -> {
                int round = roundNumber.incrementAndGet();
                System.out.println("\n╔═══════════════════════════════╗");
                System.out.println("║ ROUND " + round + " COMPLETE - SYNCING ║");
                System.out.println("║ Players ready for next round    ║");
                System.out.println("╚═══════════════════════════════╝\n");
            });
        }

        public void playGame(int rounds) throws InterruptedException {
            Thread[] players = new Thread[numPlayers];

            for (int i = 0; i < numPlayers; i++) {
                final int playerId = i;
                players[i] = new Thread(() -> {
                    try {
                        System.out.println("🎮 Player-" + playerId + " joined the game");

                        for (int round = 1; round <= rounds; round++) {
                            // Play this round
                            long playTime = (long)(Math.random() * 2000) + 500;
                            System.out.println("[Round " + round + "] Player-" + playerId + 
                                " playing... (" + playTime + "ms)");
                            Thread.sleep(playTime);

                            // Check for win
                            if (Math.random() < 0.3) { // 30% chance to win this round
                                System.out.println("[Round " + round + "] Player-" + playerId + 
                                    " WINS! 🏆");
                                winnerCount.incrementAndGet();
                            }

                            // Wait for other players
                            System.out.println("[Round " + round + "] Player-" + playerId + 
                                " waiting for others...");
                            roundBarrier.await();
                        }

                        System.out.println("✅ Player-" + playerId + " finished all rounds");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (BrokenBarrierException e) {
                        System.out.println("Game interrupted!");
                    }
                }, "Player-" + playerId);
            }

            for (Thread p : players) {
                p.start();
            }

            for (Thread p : players) {
                p.join();
            }

            System.out.println("\n🎮 GAME OVER!");
            System.out.println("Total wins: " + winnerCount.get());
        }
    }

    /**
     * ✅ BEST PRACTICE: Barrier with exception handling
     * Demonstrates what happens when a thread breaks the barrier
     */
    public static class RobustBarrierExample {
        private final CyclicBarrier barrier;

        public RobustBarrierExample(int parties) {
            this.barrier = new CyclicBarrier(parties);
        }

        public void demonstrateException() throws InterruptedException {
            Thread normal = new Thread(() -> {
                try {
                    System.out.println("Normal thread: Reaching barrier");
                    barrier.await();
                    System.out.println("Normal thread: Passed barrier");
                } catch (InterruptedException | BrokenBarrierException e) {
                    System.out.println("Normal thread: Barrier broken - " + e.getClass().getSimpleName());
                }
            }, "Normal");

            Thread faulty = new Thread(() -> {
                try {
                    System.out.println("Faulty thread: Working...");
                    Thread.sleep(500);
                    System.out.println("Faulty thread: Throwing exception before barrier");
                    throw new RuntimeException("Simulated error");
                } catch (RuntimeException e) {
                    System.out.println("Faulty thread: " + e.getMessage());
                    // Don't reach the barrier - it will be broken
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Faulty");

            normal.start();
            faulty.start();

            normal.join();
            faulty.join();

            System.out.println("Barrier is " + (barrier.isBroken() ? "BROKEN" : "INTACT"));
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CYCLIC BARRIER DEMO ===\n");

        // Demo 1: Barrier with Action
        System.out.println("--- Demo 1: Barrier with Action ---");
        demoBarrierWithAction();

        // Demo 2: Iterative Processing
        System.out.println("\n--- Demo 2: Iterative Matrix Processing ---");
        demoIterativeProcessing();

        // Demo 3: Game Coordinator
        System.out.println("\n--- Demo 3: Game Round Coordinator ---");
        demoGameRounds();

        // Demo 4: Broken Barrier
        System.out.println("\n--- Demo 4: Barrier Exception Handling ---");
        demoBrokenBarrier();
    }

    private static void demoBarrierWithAction() throws InterruptedException {
        BarrierWithAction demo = new BarrierWithAction(3);
        demo.executeWithBarrier(3);
    }

    private static void demoIterativeProcessing() throws InterruptedException {
        MatrixIterativeProcessor processor = new MatrixIterativeProcessor(4);
        processor.processIteratively();
    }

    private static void demoGameRounds() throws InterruptedException {
        GameRoundCoordinator game = new GameRoundCoordinator(3);
        game.playGame(2);
    }

    private static void demoBrokenBarrier() throws InterruptedException {
        RobustBarrierExample example = new RobustBarrierExample(2);
        example.demonstrateException();
    }
}
