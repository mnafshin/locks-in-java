# Java Synchronization Mechanisms - Interactive Demos

This directory contains comprehensive, runnable demonstrations of Java's various synchronization primitives with best practices and real-world scenarios.

## 📁 Demo Files

Each demo file contains multiple example classes showcasing different use cases and best practices:

### 1. **IntrinsicLockDemo.java**
   - Simple synchronization using `synchronized` keyword
   - **Examples:**
     - `BankAccountWithSync`: Bank account with deposit/withdraw operations
     - `ThreadSafeCounter`: Counter with manual synchronization
     - `SimpleCache`: Thread-safe cache implementation
   - **Use Case:** Simple mutual exclusion, uncontended locks
   - **Run:** `java info.mnafshin.locks_in_java.demos.IntrinsicLockDemo`

### 2. **ReentrantLockDemo.java**
   - Flexible explicit locking with advanced features
   - **Examples:**
     - `BankAccountWithTimeout`: Lock with timeout capability
     - `ProducerConsumerQueue`: Producer-consumer with conditions
     - `InterruptibleTask`: Interruptible lock operations
     - `FairLockExample`: Fair scheduling to prevent starvation
   - **Use Case:** Timeout support, interruptibility, producer-consumer patterns
   - **Run:** `java info.mnafshin.locks_in_java.demos.ReentrantLockDemo`

### 3. **ReadWriteLockDemo.java**
   - Optimized for read-heavy workloads
   - **Examples:**
     - `CachedData`: Multiple concurrent readers
     - `ConfigCache`: Configuration caching with lock downgrading
     - `PerformanceComparison`: ReadWriteLock vs synchronized comparison
   - **Use Case:** Read-heavy scenarios, concurrent reads with exclusive writes
   - **Run:** `java info.mnafshin.locks_in_java.demos.ReadWriteLockDemo`

### 4. **SemaphoreDemo.java**
   - Resource pooling and capacity control
   - **Examples:**
     - `ConnectionPool`: Database connection pool (3 connections max)
     - `BinarySemaphoreLock`: Binary semaphore used as mutex
     - `RateLimiter`: Controlling operation rate
     - `SemaphoreBasedThreadPool`: Thread pool with concurrent task limits
   - **Use Case:** Resource pooling, rate limiting, concurrent access control
   - **Run:** `java info.mnafshin.locks_in_java.demos.SemaphoreDemo`

### 5. **StampedLockDemo.java**
   - High-performance with optimistic reads
   - **Examples:**
     - `OptimizedPoint`: Optimistic read with fallback to pessimistic
     - `StampedCache`: Cache with optimistic reads
     - `VersionedData`: Version tracking with stamps
   - **Use Case:** Read-heavy, low-contention scenarios; high-performance requirements
   - **⚠️ Warning:** Use only when profiling shows performance gains
   - **Run:** `java info.mnafshin.locks_in_java.demos.StampedLockDemo`

### 6. **CountDownLatchDemo.java**
   - One-time synchronization barrier
   - **Examples:**
     - `ParallelTaskExecutor`: Waiting for multiple tasks to complete
     - `RaceStartGate`: Start signal for synchronized thread execution
     - `DataProcessingPipeline`: Multi-stage pipeline with synchronization
     - `TimeoutAwareSync`: Timeout-aware waiting pattern
   - **Use Case:** Startup synchronization, parallel task execution
   - **Limitation:** Cannot be reused (one-time only)
   - **Run:** `java info.mnafshin.locks_in_java.demos.CountDownLatchDemo`

### 7. **CyclicBarrierDemo.java**
   - Reusable synchronization barrier
   - **Examples:**
     - `BarrierWithAction`: Barrier action executed when all arrive
     - `MatrixIterativeProcessor`: Iterative algorithm synchronization
     - `GameRoundCoordinator`: Game rounds with player synchronization
     - `RobustBarrierExample`: Exception handling with broken barriers
   - **Use Case:** Iterative algorithms, game rounds, batch processing
   - **Run:** `java info.mnafshin.locks_in_java.demos.CyclicBarrierDemo`

### 8. **PhaserDemo.java**
   - Most flexible multi-phase synchronization
   - **Examples:**
     - `MultiPhaseExecution`: Different phases with dynamic participation
     - `DynamicPartyPhaser`: Dynamic party registration/deregistration
     - `WaveSynchronization`: Wave-based synchronization pattern
     - `TimeoutPhaserExample`: Timeout-aware phase advancement
   - **Use Case:** Multi-phase execution, dynamic thread counts, advanced scenarios
   - **Run:** `java info.mnafshin.locks_in_java.demos.PhaserDemo`

## 🚀 Running the Demos

### Option 1: Interactive Menu Launcher
```bash
java info.mnafshin.locks_in_java.demos.DemoLauncher
```

This provides an interactive menu to select and run individual demos.

### Option 2: Run Individual Demo
```bash
java info.mnafshin.locks_in_java.demos.IntrinsicLockDemo
java info.mnafshin.locks_in_java.demos.ReentrantLockDemo
# etc...
```

### Option 3: Build and Run with Gradle
```bash
./gradlew build
./gradlew run --args "info.mnafshin.locks_in_java.demos.DemoLauncher"
```

## 📋 Best Practices Summary

### ✅ DO's

1. **Always Release Locks**
   ```java
   lock.lock();
   try {
       // Critical section
   } finally {
       lock.unlock(); // Always in finally
   }
   ```

2. **Use Try-Finally Pattern**
   - Ensures locks are released even if an exception occurs
   - Prevents deadlocks and resource leaks

3. **Consistent Lock Ordering**
   ```java
   if (lock1.getId() < lock2.getId()) {
       lock1.lock();
       lock2.lock();
   } else {
       lock2.lock();
       lock1.lock();
   }
   ```

4. **Match Synchronization to Workload**
   - Use `synchronized` for simple cases
   - Use `ReadWriteLock` for read-heavy workloads
   - Use `Semaphore` for resource pooling
   - Use `Phaser` for complex multi-phase scenarios

5. **Handle Timeouts and Interruption**
   ```java
   if (lock.tryLock(5, TimeUnit.SECONDS)) {
       try {
           // Critical section
       } finally {
           lock.unlock();
       }
   }
   ```

6. **Use Condition Variables**
   - For complex synchronization patterns
   - Producer-consumer scenarios
   - More flexible than `wait()`/`notify()`

7. **Profile Before Optimizing**
   - StampedLock complexity may not be worth it
   - Measure performance impact

### ❌ DON'Ts

1. **Don't Hold Locks During Expensive Operations**
   - Increases contention
   - May cause other threads to block

2. **Don't Nest Locks Without Careful Planning**
   - Risk of deadlock
   - Use consistent ordering

3. **Don't Forget try-finally**
   ```java
   // ❌ BAD
   lock.lock();
   // Critical section - lock might not be released on exception
   lock.unlock();
   ```

4. **Don't Use StampedLock Unless Profiled**
   - Complex API with subtle semantics
   - Only use when performance measurements justify it

5. **Don't Mix Synchronized and Locks on Same Object**
   - Can cause unexpected behavior
   - Choose one approach and stick with it

6. **Don't Ignore InterruptedException**
   ```java
   // ❌ BAD
   try {
       lock.lockInterruptibly();
   } catch (InterruptedException e) {
       // Swallowing exception silently
   }
   
   // ✅ GOOD
   try {
       lock.lockInterruptibly();
   } catch (InterruptedException e) {
       Thread.currentThread().interrupt();
       throw e; // or handle appropriately
   }
   ```

## 🎯 Quick Selection Guide

| Scenario | Solution | Why |
|----------|----------|-----|
| Simple mutual exclusion | `synchronized` | Simple, efficient, built-in |
| Need timeout | `ReentrantLock` | Flexible, supports timeout |
| Many readers, few writers | `ReadWriteLock` | Concurrent reads, exclusive writes |
| Limit resource access | `Semaphore` | Simple resource pooling |
| Very high-performance reads | `StampedLock` | Optimistic reads (if profiled) |
| Wait for task completion | `CountDownLatch` | Simple, one-time use |
| Barrier between iterations | `CyclicBarrier` | Reusable, supports actions |
| Dynamic multi-phase sync | `Phaser` | Most flexible option |

## 📊 Performance Characteristics

- **Intrinsic Locks**: ⚡⚡⚡ (Very Fast) - Optimized by JVM
- **ReentrantLock**: ⚡⚡ (Fast) - Slight overhead for features
- **ReadWriteLock**: ⚡⚡ (Fast) - Scales with reader count
- **Semaphore**: ⚡⚡⚡ (Very Fast) - Simple counting
- **StampedLock**: ⚡⚡⚡⚡ (Fastest) - Complex but optimized
- **CountDownLatch**: ⚡⚡⚡ (Very Fast) - Lightweight
- **CyclicBarrier**: ⚡⚡⚡ (Very Fast) - Lightweight
- **Phaser**: ⚡⚡⚡ (Very Fast) - Dynamic but efficient

## 🔍 Understanding the Demos

Each demo file follows a similar structure:

1. **Example Classes**: Practical implementations with best practices
2. **Main Method**: Orchestrates multiple demo scenarios
3. **Supporting Methods**: Individual demo functions with explanations

Example structure:
```java
public class XxxDemo {
    public static class ExampleClass1 {
        // ✅ BEST PRACTICE: Implementation
    }
    
    public static class ExampleClass2 {
        // ✅ BEST PRACTICE: Another implementation
    }
    
    public static void main(String[] args) {
        demoScenario1();
        demoScenario2();
        // ...
    }
    
    private static void demoScenario1() {
        // Real-world scenario demonstration
    }
}
```

## 📚 Additional Resources

- **Java Concurrency in Practice** (Book by Brian Goetz)
- **Java API Docs**: `java.util.concurrent` package
- **Oracle Java Tutorials**: Concurrency documentation
- **Blog Post**: See `blog_post.md` for detailed explanations

## 💡 Tips for Learning

1. **Start Simple**: Begin with Intrinsic Locks and progress to Phaser
2. **Modify and Experiment**: Change thread counts, delays, workloads
3. **Observe Output**: Pay attention to timing and synchronization points
4. **Add Logging**: Insert additional System.out statements to trace execution
5. **Use a Profiler**: Measure actual performance in your application

## 🐛 Common Issues

### Issue: Deadlock
**Cause**: Locks acquired in inconsistent order
**Solution**: Always acquire locks in the same order

### Issue: Thread Starvation
**Cause**: Some threads never get lock
**Solution**: Use fair lock, switch to ReadWriteLock, or use Phaser

### Issue: Lock Not Released
**Cause**: Missing finally block or exception in critical section
**Solution**: Always use try-finally pattern

### Issue: Poor Performance with ReadWriteLock
**Cause**: Workload is balanced read/write, not read-heavy
**Solution**: Profile first, might need simple ReentrantLock instead

## 📝 License

These demos are part of the learning resource project.

---

**Happy Learning! 🚀**
