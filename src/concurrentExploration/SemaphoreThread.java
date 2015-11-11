package concurrentExploration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreThread extends Thread {
    Semaphore semaphore;
    int id;
    public SemaphoreThread (Semaphore semaphore, int id) {
        this.semaphore = semaphore;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            for(int i=0; i<2; i++) {
                System.out.println( id + " - " + Thread.currentThread().getName() );
                Thread.sleep(1500);
                //release more than acquire will increase the count
                semaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);
        List<Thread> threadList = new ArrayList<Thread>();
        for(int i=0; i<5; i++) {
            SemaphoreThread thread = new SemaphoreThread(semaphore, i);
            thread.start();
            threadList.add(thread);
        }

        for(Thread thread: threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("current available permits: " + semaphore.availablePermits());

        Runnable task = () -> {
            boolean permit = false;
            try {
                permit = semaphore.tryAcquire(1, TimeUnit.SECONDS);
                if(permit) {
                    System.out.println("Semaphore acquired");
                    TimeUnit.SECONDS.sleep(3);
                } else {
                    System.out.println("Failed to acquire the semaphore");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(permit) {
                    semaphore.release();
                }
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for(int i=0; i<10; i++) {
            executor.submit(task);
        }
        executor.shutdown();
    }
}
