package info.mnafshin.locks_in_java.demos;

import java.util.Scanner;

/**
 * Main Demo Launcher
 * 
 * This class provides an interactive menu to run demonstrations of various
 * synchronization primitives in Java.
 * 
 * Each demo showcases best practices and practical use cases for:
 * - Intrinsic Locks (synchronized)
 * - ReentrantLock
 * - ReadWriteLock
 * - Semaphore
 * - StampedLock
 * - CountDownLatch
 * - CyclicBarrier
 * - Phaser
 */
public class DemoLauncher {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("\nEnter choice (1-9): ");
            
            try {
                int choice = scanner.nextInt();
                
                System.out.println("\n" + "=".repeat(60) + "\n");
                
                switch (choice) {
                    case 1:
                        System.out.println("Running INTRINSIC LOCK DEMO...\n");
                        IntrinsicLockDemo.main(new String[0]);
                        break;
                    
                    case 2:
                        System.out.println("Running REENTRANT LOCK DEMO...\n");
                        ReentrantLockDemo.main(new String[0]);
                        break;
                    
                    case 3:
                        System.out.println("Running READ-WRITE LOCK DEMO...\n");
                        ReadWriteLockDemo.main(new String[0]);
                        break;
                    
                    case 4:
                        System.out.println("Running SEMAPHORE DEMO...\n");
                        SemaphoreDemo.main(new String[0]);
                        break;
                    
                    case 5:
                        System.out.println("Running STAMPED LOCK DEMO...\n");
                        StampedLockDemo.main(new String[0]);
                        break;
                    
                    case 6:
                        System.out.println("Running COUNT DOWN LATCH DEMO...\n");
                        CountDownLatchDemo.main(new String[0]);
                        break;
                    
                    case 7:
                        System.out.println("Running CYCLIC BARRIER DEMO...\n");
                        CyclicBarrierDemo.main(new String[0]);
                        break;
                    
                    case 8:
                        System.out.println("Running PHASER DEMO...\n");
                        PhaserDemo.main(new String[0]);
                        break;
                    
                    case 9:
                        System.out.println("Exiting Demo Launcher...");
                        running = false;
                        break;
                    
                    default:
                        System.out.println("❌ Invalid choice! Please enter a number between 1 and 9.");
                        continue;
                }
                
                if (running && choice >= 1 && choice <= 8) {
                    System.out.println("\n" + "=".repeat(60));
                    System.out.print("\nPress Enter to continue to menu...");
                    scanner.nextLine(); // Clear newline
                    scanner.nextLine(); // Wait for input
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                scanner.nextLine(); // Clear invalid input
            }
        }
        
        scanner.close();
        System.out.println("Thank you for using the Demo Launcher!");
    }
    
    private static void printMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║   JAVA SYNCHRONIZATION MECHANISMS DEMO LAUNCHER    ║");
        System.out.println("╠════════════════════════════════════════════════════╣");
        System.out.println("║                                                    ║");
        System.out.println("║  1. Intrinsic Locks (synchronized)                 ║");
        System.out.println("║     - Bank accounts, counters, caches              ║");
        System.out.println("║                                                    ║");
        System.out.println("║  2. ReentrantLock                                  ║");
        System.out.println("║     - Timeout support, conditions, fair scheduling ║");
        System.out.println("║                                                    ║");
        System.out.println("║  3. ReadWriteLock / ReentrantReadWriteLock        ║");
        System.out.println("║     - Read-heavy workloads, concurrent readers     ║");
        System.out.println("║                                                    ║");
        System.out.println("║  4. Semaphore                                      ║");
        System.out.println("║     - Connection pooling, rate limiting            ║");
        System.out.println("║                                                    ║");
        System.out.println("║  5. StampedLock                                    ║");
        System.out.println("║     - High-performance, optimistic reads           ║");
        System.out.println("║                                                    ║");
        System.out.println("║  6. CountDownLatch                                 ║");
        System.out.println("║     - One-time synchronization points              ║");
        System.out.println("║                                                    ║");
        System.out.println("║  7. CyclicBarrier                                  ║");
        System.out.println("║     - Reusable barrier, iterative algorithms       ║");
        System.out.println("║                                                    ║");
        System.out.println("║  8. Phaser                                         ║");
        System.out.println("║     - Multi-phase, dynamic parties                 ║");
        System.out.println("║                                                    ║");
        System.out.println("║  9. Exit                                           ║");
        System.out.println("║                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }
}
