package info.mnafshin.locks_in_java.demos;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demo: ReentrantLock
 * 
 * Use Case: When you need timeouts, interruptibility, or condition variables
 * Best For: Complex synchronization scenarios, producer-consumer patterns
 */
public class ReentrantLockDemo {

    /**
     * ✅ BEST PRACTICE: Bank Account with ReentrantLock and timeout
     * Demonstrates timeout capability and proper try-finally pattern
     */
    public static class BankAccountWithTimeout {
        private double balance;
        private final String accountId;
        private final ReentrantLock lock = new ReentrantLock();

        public BankAccountWithTimeout(String accountId, double initialBalance) {
            this.accountId = accountId;
            this.balance = initialBalance;
        }

        public boolean withdrawWithTimeout(double amount, long timeoutMs) throws InterruptedException {
            if (lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                try {
                    if (amount <= 0 || amount > balance) {
                        return false;
                    }
                    balance -= amount;
                    System.out.println("[" + accountId + "] Withdrew: " + amount + ", Balance: " + balance);
                    return true;
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println("[" + accountId + "] Failed to acquire lock within timeout");
                return false;
            }
        }

        public double getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Producer-Consumer with Condition Variables
     * Demonstrates the power of Condition for complex synchronization
     */
    public static class ProducerConsumerQueue<T> {
        private final java.util.Queue<T> queue = new java.util.LinkedList<>();
        private final int capacity;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        public ProducerConsumerQueue(int capacity) {
            this.capacity = capacity;
        }

        public void produce(T item) throws InterruptedException {
            lock.lock();
            try {
                while (queue.size() >= capacity) {
                    System.out.println("Queue full, producer waiting...");
                    notFull.await(); // Wait until queue has space
                }
                queue.offer(item);
                System.out.println("Produced: " + item + ", Queue size: " + queue.size());
                notEmpty.signalAll(); // Wake up consumers
            } finally {
                lock.unlock();
            }
        }

        public T consume() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    System.out.println("Queue empty, consumer waiting...");
                    notEmpty.await(); // Wait until queue has items
                }
                T item = queue.poll();
                System.out.println("Consumed: " + item + ", Queue size: " + queue.size());
                notFull.signalAll(); // Wake up producers
                return item;
            } finally {
                lock.unlock();
            }
        }

        public int size() {
            lock.lock();
            try {
                return queue.size();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Interruptible Operation
     * Shows how to handle InterruptedException properly
     */
    public static class InterruptibleTask {
        private final ReentrantLock lock = new ReentrantLock();
        private volatile boolean running = false;

        public void executeInterruptibly() throws InterruptedException {
            try {
                lock.lockInterruptibly(); // Can be interrupted while waiting
                try {
                    running = true;
                    System.out.println("Task started");
                    for (int i = 0; i < 5; i++) {
                        System.out.println("Doing work: " + i);
                        Thread.sleep(500);
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException("Task was interrupted");
                        }
                    }
                    System.out.println("Task completed");
                } finally {
                    lock.unlock();
                    running = false;
                }
            } catch (InterruptedException e) {
                System.out.println("Task interrupted");
                Thread.currentThread().interrupt(); // Re-interrupt the thread
                throw e;
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Fair Lock to prevent starvation
     * Demonstrates fair scheduling for better performance with many threads
     */
    public static class FairLockExample {
        private int counter = 0;
        private final ReentrantLock fairLock = new ReentrantLock(true); // Fair lock

        public void increment() {
            fairLock.lock();
            try {
                int localCounter = counter;
                Thread.yield(); // Increase contention likelihood
                counter = localCounter + 1;
            } finally {
                fairLock.unlock();
            }
        }

        public int getCounter() {
            fairLock.lock();
            try {
                return counter;
            } finally {
                fairLock.unlock();
            }
        }

        public int getQueueLength() {
            return fairLock.getQueueLength();
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== REENTRANT LOCK DEMO ===\n");

        // Demo 1: Timeout capability
        System.out.println("--- Demo 1: Withdrawal with Timeout ---");
        demoTimeoutCapability();

        // Demo 2: Producer-Consumer
        System.out.println("\n--- Demo 2: Producer-Consumer with Conditions ---");
        demoProducerConsumer();

        // Demo 3: Fair Lock
        System.out.println("\n--- Demo 3: Fair Lock ---");
        demoFairLock();
    }

    private static void demoTimeoutCapability() throws InterruptedException {
        BankAccountWithTimeout account = new BankAccountWithTimeout("ACC-001", 1000);

        Thread t1 = new Thread(() -> {
            try {
                account.withdrawWithTimeout(200, 5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Client-1");

        Thread t2 = new Thread(() -> {
            try {
                // Hold lock for 3 seconds
                Thread.sleep(1000);
                account.withdrawWithTimeout(300, 2000); // Will timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Client-2");

        t2.start();
        Thread.sleep(500); // Let t2 start first
        t1.start();

        t1.join();
        t2.join();

        System.out.println("Final balance: " + account.getBalance());
    }

    private static void demoProducerConsumer() throws InterruptedException {
        ProducerConsumerQueue<Integer> queue = new ProducerConsumerQueue<>(3);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    queue.produce(i);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(200);
                for (int i = 0; i < 5; i++) {
                    queue.consume();
                    Thread.sleep(800);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
    }

    private static void demoFairLock() throws InterruptedException {
        FairLockExample fairExample = new FairLockExample();
        Thread[] threads = new Thread[10];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    fairExample.increment();
                }
                System.out.println("Thread-" + threadNum + " completed, Queue length: " + fairExample.getQueueLength());
            }, "Worker-" + i);
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Final counter: " + fairExample.getCounter() + " (Expected: " + (threads.length * 100) + ")");
        System.out.println("Execution time: " + duration + "ms");
    }
}
