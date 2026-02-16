package info.mnafshin.locks_in_java.demos;

/**
 * Demo: Intrinsic Locks (synchronized keyword)
 * 
 * Use Case: Simple mutual exclusion without timeout or advanced features
 * Best For: Straightforward synchronization, uncontended locks
 */
public class IntrinsicLockDemo {

    /**
     * ✅ BEST PRACTICE: Bank Account with synchronized methods
     * Simple, clear, and safe for basic synchronization
     */
    public static class BankAccountWithSync {
        private double balance;
        private final String accountId;

        public BankAccountWithSync(String accountId, double initialBalance) {
            this.accountId = accountId;
            this.balance = initialBalance;
        }

        public synchronized void deposit(double amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            double newBalance = balance + amount;
            simulateLatency(); // Simulate processing time
            balance = newBalance;
            System.out.println("[" + accountId + "] Deposited: " + amount + ", Balance: " + balance);
        }

        public synchronized void withdraw(double amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            if (amount > balance) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            double newBalance = balance - amount;
            simulateLatency();
            balance = newBalance;
            System.out.println("[" + accountId + "] Withdrew: " + amount + ", Balance: " + balance);
        }

        public synchronized double getBalance() {
            return balance;
        }

        private void simulateLatency() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * ✅ BEST PRACTICE: Manual synchronization for fine-grained control
     * Use when you need to synchronize only part of the method
     */
    public static class ThreadSafeCounter {
        private int count = 0;
        private final Object lock = new Object(); // Explicit lock object

        public void increment() {
            synchronized (lock) {
                count++;
            }
        }

        public void incrementMultiple(int times) {
            for (int i = 0; i < times; i++) {
                synchronized (lock) {
                    count++;
                }
            }
        }

        public int getCount() {
            synchronized (lock) {
                return count;
            }
        }
    }

    /**
     * ❌ ANTI-PATTERN: Synchronizing on this in a subclassable class
     * Can lead to unexpected synchronization behavior
     */
    public static class BadCounter {
        private int count = 0;

        public synchronized void increment() {
            // ❌ Subclasses can override and break synchronization
            count++;
        }
    }

    /**
     * ✅ BEST PRACTICE: Cache with synchronized access
     * Demonstrates synchronized block for better granularity
     */
    public static class SimpleCache<K, V> {
        private final java.util.Map<K, V> cache = new java.util.HashMap<>();

        public void put(K key, V value) {
            synchronized (cache) {
                cache.put(key, value);
            }
        }

        public V get(K key) {
            synchronized (cache) {
                return cache.get(key);
            }
        }

        public void clear() {
            synchronized (cache) {
                cache.clear();
            }
        }

        public int size() {
            synchronized (cache) {
                return cache.size();
            }
        }
    }

    // Demo execution
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== INTRINSIC LOCK DEMO ===\n");

        // Demo 1: Bank Account
        System.out.println("--- Demo 1: Bank Account with Synchronized Methods ---");
        demoThreadsWaitingForLock();

        // Demo 2: Manual Synchronization
        System.out.println("\n--- Demo 2: Manual Synchronization with Lock Object ---");
        demoManualSync();

        // Demo 3: Simple Cache
        System.out.println("\n--- Demo 3: Thread-Safe Cache ---");
        demoCacheAccess();
    }

    private static void demoThreadsWaitingForLock() throws InterruptedException {
        BankAccountWithSync account = new BankAccountWithSync("001", 1000);

        Thread depositor = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                account.deposit(100);
            }
        }, "Depositor");

        Thread withdrawer = new Thread(() -> {
            for (int i = 0; i < 2; i++) {
                try {
                    account.withdraw(150);
                } catch (IllegalArgumentException e) {
                    System.out.println("Withdrawal failed: " + e.getMessage());
                }
            }
        }, "Withdrawer");

        depositor.start();
        withdrawer.start();

        depositor.join();
        withdrawer.join();

        System.out.println("Final Balance: " + account.getBalance());
    }

    private static void demoManualSync() throws InterruptedException {
        ThreadSafeCounter counter = new ThreadSafeCounter();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        }, "Thread-2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Counter value (should be 2000): " + counter.getCount());
    }

    private static void demoCacheAccess() throws InterruptedException {
        SimpleCache<String, String> cache = new SimpleCache<>();

        Thread writer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                cache.put("key-" + i, "value-" + i);
                System.out.println("Cached: key-" + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Writer");

        Thread reader = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            for (int i = 0; i < 5; i++) {
                String value = cache.get("key-" + i);
                System.out.println("Retrieved: key-" + i + " = " + value + ", Cache size: " + cache.size());
                try {
                    Thread.sleep(100);
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
