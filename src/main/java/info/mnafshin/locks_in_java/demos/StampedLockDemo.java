package info.mnafshin.locks_in_java.demos;

import java.util.concurrent.locks.StampedLock;

/**
 * Demo: StampedLock
 * 
 * Use Case: High-performance read scenarios with low contention
 * Best For: Frequently-read, infrequently-written data structures
 * WARNING: Use only when profiling shows performance gains - complex API
 */
public class StampedLockDemo {

    /**
     * ✅ BEST PRACTICE: Optimistic Read with Fallback
     * Fast path uses optimistic read, falls back to pessimistic on conflict
     */
    public static class OptimizedPoint {
        private double x, y;
        private final StampedLock lock = new StampedLock();

        public OptimizedPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        // Optimistic read - fast path without acquiring lock
        public double distanceFromOriginOptimistic() {
            long stamp = lock.tryOptimisticRead();
            double currentX = x;
            double currentY = y;

            if (!lock.validate(stamp)) {
                // Conflict detected, fall back to pessimistic read
                System.out.println(Thread.currentThread().getName() + 
                    " optimistic read failed, falling back to pessimistic");
                stamp = lock.readLock();
                try {
                    currentX = x;
                    currentY = y;
                } finally {
                    lock.unlockRead(stamp);
                }
            }

            return Math.sqrt(currentX * currentX + currentY * currentY);
        }

        // Pessimistic read - always acquires lock
        public double distanceFromOriginPessimistic() {
            long stamp = lock.readLock();
            try {
                return Math.sqrt(x * x + y * y);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        // Write operation - exclusive access
        public void move(double deltaX, double deltaY) {
            long stamp = lock.writeLock();
            try {
                System.out.println(Thread.currentThread().getName() + 
                    " acquiring write lock");
                x += deltaX;
                y += deltaY;
                System.out.println(Thread.currentThread().getName() + 
                    " write complete: (" + x + ", " + y + ")");
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        public void get(double[] result) {
            long stamp = lock.readLock();
            try {
                result[0] = x;
                result[1] = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: High-Contention Cache using StampedLock
     * Shows when optimistic reads provide significant benefits
     */
    public static class StampedCache {
        private final StampedLock lock = new StampedLock();
        private final java.util.Map<String, String> cache = new java.util.HashMap<>();

        public String getOptimistic(String key) {
            long stamp = lock.tryOptimisticRead();
            String value = cache.get(key);

            if (!lock.validate(stamp)) {
                // Read conflict, retry with pessimistic read
                stamp = lock.readLock();
                try {
                    value = cache.get(key);
                } finally {
                    lock.unlockRead(stamp);
                }
            }

            return value;
        }

        public void put(String key, String value) {
            long stamp = lock.writeLock();
            try {
                cache.put(key, value);
                System.out.println(Thread.currentThread().getName() + 
                    " cached: " + key + " = " + value);
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        public int size() {
            long stamp = lock.tryOptimisticRead();
            int size = cache.size();

            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    size = cache.size();
                } finally {
                    lock.unlockRead(stamp);
                }
            }

            return size;
        }
    }

    /**
     * ✅ BEST PRACTICE: Version control with StampedLock
     * Track versions to detect concurrent modifications
     */
    public static class VersionedData {
        private final StampedLock lock = new StampedLock();
        private long version = 0;
        private String data = "";

        public String readValue() {
            long stamp = lock.tryOptimisticRead();
            String result = data;
            long readVersion = version;

            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    result = data;
                    readVersion = version;
                } finally {
                    lock.unlockRead(stamp);
                }
            }

            System.out.println(Thread.currentThread().getName() + 
                " read value (v" + readVersion + "): " + result);
            return result;
        }

        public void writeValue(String newData) {
            long stamp = lock.writeLock();
            try {
                data = newData;
                version++;
                System.out.println(Thread.currentThread().getName() + 
                    " wrote value (v" + version + "): " + newData);
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== STAMPED LOCK DEMO ===\n");

        // Demo 1: Optimistic Read
        System.out.println("--- Demo 1: Optimistic Read with Fallback ---");
        demoOptimisticRead();

        // Demo 2: Cache with Optimistic Reads
        System.out.println("\n--- Demo 2: Stamped Cache ---");
        demodemoStampedCache();

        // Demo 3: Version Control
        System.out.println("\n--- Demo 3: Versioned Data ---");
        demoVersionedData();
    }

    private static void demoOptimisticRead() throws InterruptedException {
        OptimizedPoint point = new OptimizedPoint(3, 4);

        Thread writer = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    point.move(1, 1);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Writer");

        Thread readerOptimistic = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                double distance = point.distanceFromOriginOptimistic();
                System.out.println(Thread.currentThread().getName() + 
                    " optimistic distance: " + distance);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Reader-Optimistic");

        Thread readerPessimistic = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                double distance = point.distanceFromOriginPessimistic();
                System.out.println(Thread.currentThread().getName() + 
                    " pessimistic distance: " + distance);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Reader-Pessimistic");

        writer.start();
        readerOptimistic.start();
        readerPessimistic.start();

        writer.join();
        readerOptimistic.join();
        readerPessimistic.join();
    }

    private static void demodemoStampedCache() throws InterruptedException {
        StampedCache cache = new StampedCache();

        Thread writer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                cache.put("key-" + i, "value-" + i);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Writer");

        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            final int readerId = i;
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 4; j++) {
                    String value = cache.getOptimistic("key-" + j);
                    System.out.println(Thread.currentThread().getName() + 
                        " got: " + value + " (Cache size: " + cache.size() + ")");
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Reader-" + readerId);
        }

        writer.start();
        for (Thread r : readers) {
            r.start();
        }

        writer.join();
        for (Thread r : readers) {
            r.join();
        }
    }

    private static void demoVersionedData() throws InterruptedException {
        VersionedData versionedData = new VersionedData();

        Thread writer = new Thread(() -> {
            try {
                versionedData.writeValue("Value-1");
                Thread.sleep(300);
                versionedData.writeValue("Value-2");
                Thread.sleep(300);
                versionedData.writeValue("Value-3");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Writer");

        Thread reader = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                versionedData.readValue();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Reader");

        writer.start();
        reader.start();

        writer.join();
        reader.join();
    }
}
