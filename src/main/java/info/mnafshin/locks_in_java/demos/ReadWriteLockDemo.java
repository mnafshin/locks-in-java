package info.mnafshin.locks_in_java.demos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Demo: ReadWriteLock / ReentrantReadWriteLock
 * 
 * Use Case: Read-heavy workloads where multiple readers don't interfere
 * Best For: Caches, configuration objects, data structures with frequent reads
 */
public class ReadWriteLockDemo {

    /**
     * ✅ BEST PRACTICE: Cached Data with ReadWriteLock
     * Multiple readers can access simultaneously, writers get exclusive access
     */
    public static class CachedData {
        private String data;
        private long lastUpdated;
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        public CachedData(String initialData) {
            this.data = initialData;
            this.lastUpdated = System.currentTimeMillis();
        }

        // Read operation - multiple threads can read simultaneously
        public String read() {
            lock.readLock().lock();
            try {
                System.out.println("[READ] " + Thread.currentThread().getName() + 
                    " reading: " + data + " at " + LocalDateTime.now().format(formatter));
                // Simulate read processing
                Thread.sleep(100);
                return data;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                lock.readLock().unlock();
            }
        }

        // Write operation - exclusive access
        public void write(String newData) {
            lock.writeLock().lock();
            try {
                System.out.println("[WRITE] " + Thread.currentThread().getName() + 
                    " writing: " + newData + " at " + LocalDateTime.now().format(formatter));
                Thread.sleep(200); // Simulate write processing
                data = newData;
                lastUpdated = System.currentTimeMillis();
                System.out.println("[WRITE] " + Thread.currentThread().getName() + " completed write");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();
            }
        }

        public long getLastUpdated() {
            lock.readLock().lock();
            try {
                return lastUpdated;
            } finally {
                lock.readLock().unlock();
            }
        }

        public int getReadLockCount() {
            return ((ReentrantReadWriteLock) lock).getReadLockCount();
        }

        public boolean isWriteLocked() {
            return ((ReentrantReadWriteLock) lock).isWriteLocked();
        }
    }

    /**
     * ✅ BEST PRACTICE: Configuration Cache with Lock Downgrading
     * Demonstrates converting write lock to read lock (downgrade)
     */
    public static class ConfigCache {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private java.util.Map<String, String> config = new java.util.HashMap<>();

        public String getConfig(String key) {
            lock.readLock().lock();
            try {
                return config.get(key);
            } finally {
                lock.readLock().unlock();
            }
        }

        public void setConfig(String key, String value) {
            lock.writeLock().lock();
            try {
                config.put(key, value);
                System.out.println("Config updated: " + key + " = " + value);
            } finally {
                lock.writeLock().unlock();
            }
        }

        // Lock downgrade: convert write lock to read lock
        public void updateAndVerify(String key, String value) {
            lock.writeLock().lock();
            try {
                config.put(key, value);
                System.out.println("Config updated: " + key + " = " + value);

                // Acquire read lock before releasing write lock
                lock.readLock().lock();
            } finally {
                lock.writeLock().unlock(); // Release write lock, keep read lock
            }

            try {
                // Verify the update with read lock
                String currentValue = config.get(key);
                System.out.println("Verified: " + key + " = " + currentValue);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Performance comparison between synchronized and ReadWriteLock
     */
    public static class PerformanceComparison {
        private String data = "initial";

        // Using ReentrantReadWriteLock
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

        public String readWithRWLock() {
            rwLock.readLock().lock();
            try {
                return data;
            } finally {
                rwLock.readLock().unlock();
            }
        }

        public void writeWithRWLock(String newData) {
            rwLock.writeLock().lock();
            try {
                data = newData;
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        // Using synchronized
        public synchronized String readWithSync() {
            return data;
        }

        public synchronized void writeWithSync(String newData) {
            data = newData;
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== READ-WRITE LOCK DEMO ===\n");

        // Demo 1: Concurrent Readers
        System.out.println("--- Demo 1: Multiple Readers with Exclusive Writers ---");
        demoConcurrentReaders();

        // Demo 2: Lock Downgrading
        System.out.println("\n--- Demo 2: Lock Downgrading ---");
        demoLockDowngrading();

        // Demo 3: Performance Comparison
        System.out.println("\n--- Demo 3: Performance Comparison ---");
        demoPerformanceComparison();
    }

    private static void demoConcurrentReaders() throws InterruptedException {
        CachedData cache = new CachedData("Initial Value");

        // Create multiple reader threads
        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            final int readerId = i;
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 2; j++) {
                    cache.read();
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Reader-" + readerId);
        }

        // Create writer thread
        Thread writer = new Thread(() -> {
            try {
                Thread.sleep(50);
                cache.write("Updated Value 1");
                Thread.sleep(150);
                cache.write("Updated Value 2");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Writer");

        // Start all threads
        for (Thread reader : readers) {
            reader.start();
        }
        writer.start();

        // Wait for completion
        for (Thread reader : readers) {
            reader.join();
        }
        writer.join();

        System.out.println("Concurrent reads completed successfully!");
    }

    private static void demoLockDowngrading() throws InterruptedException {
        ConfigCache configCache = new ConfigCache();

        Thread t1 = new Thread(() -> {
            configCache.updateAndVerify("database.url", "jdbc:mysql://localhost:3306/db");
        }, "ConfigUpdater-1");

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                String url = configCache.getConfig("database.url");
                System.out.println("Reader sees: " + url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ConfigReader-1");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

    private static void demoPerformanceComparison() throws InterruptedException {
        PerformanceComparison perfTest = new PerformanceComparison();

        // Test ReadWriteLock with 95% reads
        System.out.println("Testing ReadWriteLock (95% reads):");
        long startRWLock = System.currentTimeMillis();
        testReadWriteLock(perfTest);
        long durationRWLock = System.currentTimeMillis() - startRWLock;

        // Test Synchronized with 95% reads
        System.out.println("\nTesting Synchronized (95% reads):");
        long startSync = System.currentTimeMillis();
        testSynchronized(perfTest);
        long durationSync = System.currentTimeMillis() - startSync;

        System.out.println("\n--- Performance Results ---");
        System.out.println("ReadWriteLock: " + durationRWLock + "ms");
        System.out.println("Synchronized: " + durationSync + "ms");
        if (durationRWLock < durationSync) {
            System.out.println("ReadWriteLock is " + ((100 * durationSync / durationRWLock) - 100) + 
                "% faster for this read-heavy workload");
        } else {
            System.out.println("Synchronized is " + ((100 * durationRWLock / durationSync) - 100) + 
                "% faster");
        }
    }

    private static void testReadWriteLock(PerformanceComparison test) throws InterruptedException {
        Thread[] threads = new Thread[20];

        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    if (j % 20 == 0) { // 5% writes
                        test.writeWithRWLock("value-" + threadNum);
                    } else { // 95% reads
                        test.readWithRWLock();
                    }
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    private static void testSynchronized(PerformanceComparison test) throws InterruptedException {
        Thread[] threads = new Thread[20];

        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    if (j % 20 == 0) { // 5% writes
                        test.writeWithSync("value-" + threadNum);
                    } else { // 95% reads
                        test.readWithSync();
                    }
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }
}
