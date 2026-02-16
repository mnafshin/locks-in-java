package info.mnafshin.locks_in_java.demos;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demo: Semaphore
 * 
 * Use Case: Controlling access to a limited pool of resources
 * Best For: Connection pooling, thread pool limits, rate limiting
 */
public class SemaphoreDemo {

    /**
     * ✅ BEST PRACTICE: Database Connection Pool with Semaphore
     * Controls concurrent access to a limited number of connections
     */
    public static class ConnectionPool {
        private final Semaphore semaphore;
        private final java.util.Queue<String> availableConnections;
        private final int poolSize;
        private final AtomicInteger activeConnections = new AtomicInteger(0);

        public ConnectionPool(int poolSize) {
            this.poolSize = poolSize;
            this.semaphore = new Semaphore(poolSize);
            this.availableConnections = new java.util.LinkedList<>();
            
            // Initialize pool with connection strings
            for (int i = 1; i <= poolSize; i++) {
                availableConnections.offer("Connection-" + i);
            }
        }

        public String acquireConnection(long timeoutMs) throws InterruptedException {
            if (semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                String connection = availableConnections.poll();
                activeConnections.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + 
                    " acquired " + connection + 
                    " (Active: " + activeConnections.get() + "/" + poolSize + ")");
                return connection;
            } else {
                System.out.println(Thread.currentThread().getName() + 
                    " failed to get connection within timeout");
                return null;
            }
        }

        public void releaseConnection(String connection) {
            if (connection != null) {
                availableConnections.offer(connection);
                activeConnections.decrementAndGet();
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + 
                    " released " + connection);
            }
        }

        public int getAvailableConnections() {
            return semaphore.availablePermits();
        }
    }

    /**
     * ✅ BEST PRACTICE: Binary Semaphore (Mutex)
     * Acts as a lock controlling access to a single resource
     */
    public static class BinarySemaphoreLock {
        private final Semaphore mutex = new Semaphore(1); // Binary semaphore
        private int counter = 0;

        public void increment() throws InterruptedException {
            mutex.acquire();
            try {
                int temp = counter;
                Thread.sleep(10); // Simulate work
                counter = temp + 1;
            } finally {
                mutex.release();
            }
        }

        public int getCounter() throws InterruptedException {
            mutex.acquire();
            try {
                return counter;
            } finally {
                mutex.release();
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Rate Limiter using Semaphore
     * Controls the rate at which operations can be performed
     */
    public static class RateLimiter {
        private final Semaphore semaphore;
        private final long refillIntervalMs;
        private final int permitsPerInterval;

        public RateLimiter(int permitsPerSecond) {
            this.semaphore = new Semaphore(permitsPerSecond);
            this.permitsPerInterval = permitsPerSecond;
            this.refillIntervalMs = 1000; // 1 second

            // Background thread to refill permits
            Thread refillThread = new Thread(this::refillPermits);
            refillThread.setDaemon(true);
            refillThread.start();
        }

        public boolean allowRequest() throws InterruptedException {
            return semaphore.tryAcquire(1, TimeUnit.MILLISECONDS);
        }

        public void allowRequestBlocking() throws InterruptedException {
            semaphore.acquire();
        }

        private void refillPermits() {
            while (true) {
                try {
                    Thread.sleep(refillIntervalMs);
                    int available = semaphore.availablePermits();
                    if (available < permitsPerInterval) {
                        int toAdd = permitsPerInterval - available;
                        semaphore.release(toAdd);
                        System.out.println("[RateLimiter] Refilled " + toAdd + " permits");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        public int getAvailablePermits() {
            return semaphore.availablePermits();
        }
    }

    /**
     * ✅ BEST PRACTICE: Thread Pool with Semaphore Control
     * Limits the number of threads that can execute concurrently
     */
    public static class SemaphoreBasedThreadPool {
        private final Semaphore workerSemaphore;
        private final java.util.concurrent.ExecutorService executor;
        private final AtomicInteger submittedTasks = new AtomicInteger(0);
        private final AtomicInteger completedTasks = new AtomicInteger(0);

        public SemaphoreBasedThreadPool(int maxConcurrentTasks) {
            this.workerSemaphore = new Semaphore(maxConcurrentTasks);
            this.executor = java.util.concurrent.Executors.newFixedThreadPool(maxConcurrentTasks * 2);
        }

        public void submitTask(String taskName) throws InterruptedException {
            workerSemaphore.acquire();
            submittedTasks.incrementAndGet();
            
            executor.submit(() -> {
                try {
                    System.out.println("[TASK] " + taskName + " started " + 
                        "(Active: " + (submittedTasks.get() - completedTasks.get()) + ")");
                    
                    // Simulate work
                    Thread.sleep((long)(Math.random() * 2000));
                    System.out.println("[TASK] " + taskName + " completed");
                    completedTasks.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    workerSemaphore.release();
                }
            });
        }

        public void shutdown() throws InterruptedException {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        public int getCurrentActiveTasks() {
            return submittedTasks.get() - completedTasks.get();
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== SEMAPHORE DEMO ===\n");

        // Demo 1: Connection Pool
        System.out.println("--- Demo 1: Connection Pool with Semaphore ---");
        demoConnectionPool();

        // Demo 2: Binary Semaphore as Mutex
        System.out.println("\n--- Demo 2: Binary Semaphore (Mutex) ---");
        demoBinarySemaphore();

        // Demo 3: Rate Limiter
        System.out.println("\n--- Demo 3: Rate Limiter ---");
        demoRateLimiter();

        // Demo 4: Thread Pool Control
        System.out.println("\n--- Demo 4: Thread Pool with Semaphore ---");
        demoThreadPoolControl();
    }

    private static void demoConnectionPool() throws InterruptedException {
        ConnectionPool pool = new ConnectionPool(3); // Only 3 connections available

        Thread[] threads = new Thread[7];

        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                try {
                    String conn = pool.acquireConnection(2000); // 2 second timeout
                    if (conn != null) {
                        // Use connection
                        System.out.println(Thread.currentThread().getName() + 
                            " using " + conn);
                        Thread.sleep((long)(Math.random() * 1000));
                        pool.releaseConnection(conn);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Client-" + threadNum);
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("All clients completed. Available connections: " + 
            pool.getAvailableConnections());
    }

    private static void demoBinarySemaphore() throws InterruptedException {
        BinarySemaphoreLock counter = new BinarySemaphoreLock();

        Thread[] threads = new Thread[5];

        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    try {
                        counter.increment();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                System.out.println("Thread-" + threadNum + " completed 100 increments");
            }, "Worker-" + i);
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        try {
            System.out.println("Final counter value: " + counter.getCounter() + 
                " (Expected: " + (threads.length * 100) + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void demoRateLimiter() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(5); // 5 requests per second

        System.out.println("Starting rate-limited requests (max 5 per second)...");
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= 12; i++) {
            final int requestNum = i;
            new Thread(() -> {
                try {
                    limiter.allowRequestBlocking();
                    long timeElapsed = System.currentTimeMillis() - startTime;
                    System.out.println("[Request] " + requestNum + 
                        " allowed at " + timeElapsed + "ms (Available: " + 
                        limiter.getAvailablePermits() + ")");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            Thread.sleep(100); // Spread out requests
        }

        // Wait for all requests to complete
        Thread.sleep(3000);
    }

    private static void demoThreadPoolControl() throws InterruptedException {
        SemaphoreBasedThreadPool pool = new SemaphoreBasedThreadPool(3); // Max 3 concurrent

        System.out.println("Submitting 8 tasks with max 3 concurrent...");

        for (int i = 1; i <= 8; i++) {
            pool.submitTask("Task-" + i);
            System.out.println("Submitted Task-" + i + " (Active: " + 
                pool.getCurrentActiveTasks() + ")");
            Thread.sleep(200);
        }

        // Wait for all tasks
        Thread.sleep(5000);
        System.out.println("All tasks completed");

        pool.shutdown();
    }
}
