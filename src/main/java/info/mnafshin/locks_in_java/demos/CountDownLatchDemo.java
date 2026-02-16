package info.mnafshin.locks_in_java.demos;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demo: CountDownLatch
 * 
 * Use Case: One-time synchronization points where threads wait for completion
 * Best For: Startup synchronization, parallel task execution, test coordination
 * Limitation: Cannot be reused (one-time only)
 */
public class CountDownLatchDemo {

    /**
     * ✅ BEST PRACTICE: Waiting for multiple tasks to complete
     * Main thread waits for all worker tasks to finish
     */
    public static class ParallelTaskExecutor {
        private final int numWorkers;
        private final CountDownLatch latch;

        public ParallelTaskExecutor(int numWorkers) {
            this.numWorkers = numWorkers;
            this.latch = new CountDownLatch(numWorkers);
        }

        public void executeInParallel(String[] tasks) throws InterruptedException {
            System.out.println("Starting " + numWorkers + " parallel tasks...");
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < numWorkers; i++) {
                final int workerId = i;
                final String task = tasks[i];
                new Thread(() -> {
                    try {
                        System.out.println("[Worker-" + workerId + "] Starting: " + task);
                        Thread.sleep((long)(Math.random() * 2000)); // Simulate work
                        System.out.println("[Worker-" + workerId + "] Completed: " + task);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown(); // Decrement counter
                        System.out.println("[Worker-" + workerId + "] Countdown: " + 
                            latch.getCount());
                    }
                }, "Worker-" + workerId).start();
            }

            // Main thread waits here
            System.out.println("Main thread waiting for all workers...");
            boolean completed = latch.await(10, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            if (completed) {
                System.out.println("✅ All tasks completed in " + duration + "ms");
            } else {
                System.out.println("❌ Timeout: Not all tasks completed within 10 seconds");
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Start signal for multiple threads
     * All threads wait until a start signal before executing
     */
    public static class RaceStartGate {
        private final CountDownLatch startSignal;
        private final CountDownLatch endSignal;
        private final int numRacers;

        public RaceStartGate(int numRacers) {
            this.numRacers = numRacers;
            this.startSignal = new CountDownLatch(1); // 1 means not started
            this.endSignal = new CountDownLatch(numRacers); // All must finish
        }

        public void race() throws InterruptedException {
            System.out.println("🚩 Race starting with " + numRacers + " racers...");
            long startTime = System.currentTimeMillis();

            // Create racer threads that wait for signal
            for (int i = 0; i < numRacers; i++) {
                final int racerId = i;
                new Thread(() -> {
                    try {
                        startSignal.await(); // Wait for start signal
                        long raceStart = System.currentTimeMillis();
                        
                        // Run the race
                        long raceTime = (long)(Math.random() * 3000) + 1000; // 1-4 seconds
                        Thread.sleep(raceTime);
                        
                        System.out.println("🏃 Racer-" + racerId + " finished in " + 
                            raceTime + "ms");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endSignal.countDown();
                    }
                }, "Racer-" + i).start();
            }

            // Wait a bit then start the race
            Thread.sleep(500);
            System.out.println("🎬 RACE START!");
            startSignal.countDown(); // Signal all racers to start

            // Wait for all racers to finish
            endSignal.await();
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("🏁 Race completed! Total time: " + duration + "ms");
        }
    }

    /**
     * ✅ BEST PRACTICE: Pipeline with multiple stages
     * Each stage waits for previous stage to complete
     */
    public static class DataProcessingPipeline {
        private final String[] data;
        private final CountDownLatch stage1Latch;
        private final CountDownLatch stage2Latch;
        private final AtomicInteger stage1Count = new AtomicInteger(0);
        private final AtomicInteger stage2Count = new AtomicInteger(0);

        public DataProcessingPipeline(String[] inputData) {
            this.data = inputData;
            this.stage1Latch = new CountDownLatch(inputData.length);
            this.stage2Latch = new CountDownLatch(inputData.length);
        }

        public void process() throws InterruptedException {
            System.out.println("Processing " + data.length + " items through 2 stages...");

            // Stage 1: Transformation
            Thread stage1 = new Thread(() -> {
                for (String item : data) {
                    try {
                        System.out.println("[Stage1] Processing: " + item);
                        Thread.sleep(200);
                        String transformed = item.toUpperCase();
                        System.out.println("[Stage1] Transformed: " + transformed);
                        stage1Count.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        stage1Latch.countDown();
                    }
                }
            }, "Stage1-Worker");

            // Stage 2: Validation (waits for Stage 1)
            Thread stage2 = new Thread(() -> {
                try {
                    System.out.println("[Stage2] Waiting for Stage 1 to complete...");
                    stage1Latch.await();
                    System.out.println("[Stage2] Stage 1 complete! Starting processing...");

                    for (String item : data) {
                        System.out.println("[Stage2] Validating: " + item);
                        Thread.sleep(150);
                        System.out.println("[Stage2] Validated ✓");
                        stage2Count.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    stage2Latch.countDown();
                }
            }, "Stage2-Worker");

            stage1.start();
            stage2.start();

            // Wait for both stages to complete
            stage2Latch.await();
            System.out.println("Pipeline complete!");
            System.out.println("Stage 1 processed: " + stage1Count.get() + " items");
            System.out.println("Stage 2 processed: " + stage2Count.get() + " items");
        }
    }

    /**
     * ✅ BEST PRACTICE: Timeout-aware waiting pattern
     * Handles both success and timeout scenarios
     */
    public static class TimeoutAwareSync {
        private final CountDownLatch latch;
        private final long timeoutSeconds;

        public TimeoutAwareSync(int count, long timeoutSeconds) {
            this.latch = new CountDownLatch(count);
            this.timeoutSeconds = timeoutSeconds;
        }

        public boolean waitForCompletion() throws InterruptedException {
            System.out.println("Waiting up to " + timeoutSeconds + " seconds...");
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            return completed;
        }

        public void markComplete() {
            latch.countDown();
            System.out.println("Task completed. Remaining: " + latch.getCount());
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== COUNTDOWN LATCH DEMO ===\n");

        // Demo 1: Parallel Task Execution
        System.out.println("--- Demo 1: Parallel Task Execution ---");
        demoParallelTasks();

        // Demo 2: Race Start Gate
        System.out.println("\n--- Demo 2: Race Start Gate ---");
        demoRaceStart();

        // Demo 3: Pipeline
        System.out.println("\n--- Demo 3: Multi-Stage Pipeline ---");
        demoPipeline();

        // Demo 4: Timeout Aware
        System.out.println("\n--- Demo 4: Timeout-Aware Synchronization ---");
        demoTimeoutAware();
    }

    private static void demoParallelTasks() throws InterruptedException {
        String[] tasks = {
            "Database Migration",
            "Cache Warm-up",
            "Config Loading",
            "Service Registration"
        };

        ParallelTaskExecutor executor = new ParallelTaskExecutor(tasks.length);
        executor.executeInParallel(tasks);
    }

    private static void demoRaceStart() throws InterruptedException {
        RaceStartGate race = new RaceStartGate(5);
        race.race();
    }

    private static void demoPipeline() throws InterruptedException {
        String[] data = {"apple", "banana", "cherry", "date"};
        DataProcessingPipeline pipeline = new DataProcessingPipeline(data);
        pipeline.process();
    }

    private static void demoTimeoutAware() throws InterruptedException {
        TimeoutAwareSync sync = new TimeoutAwareSync(3, 5);

        // Simulate tasks completing
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            new Thread(() -> {
                try {
                    Thread.sleep(1000 * (taskId + 1));
                    System.out.println("Task-" + taskId + " completed");
                    sync.markComplete();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Task-" + i).start();
        }

        if (sync.waitForCompletion()) {
            System.out.println("✅ All tasks completed successfully!");
        } else {
            System.out.println("❌ Timeout waiting for tasks!");
        }
    }
}
