package concurrentExploration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class LockExplr {
    private static int TIME_TO_SLEEP = 10;

    public static void main(String[] args) {
        try {
            runningTask1();
            TimeUnit.SECONDS.sleep(30);
            runningTask2();
            TimeUnit.SECONDS.sleep(30);
            runningTask3();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runningTask1() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        ReentrantLock reentrantLock = new ReentrantLock();

        executor.submit(() -> {
            reentrantLock.lock();
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }
        });

        TimeUnit.SECONDS.sleep(1);

        executor.submit(() -> {
           System.out.println("Is locked: " + reentrantLock.isLocked());
           System.out.println("Is held by current thread: " + reentrantLock.isHeldByCurrentThread());
           boolean locked = reentrantLock.tryLock();
           System.out.println("Lock acuqired at the first time: " + locked);
           if(!locked) {
               try {
                locked = reentrantLock.tryLock(8, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
               System.out.println("Lock acuqired at the second time: " + locked);
           }
        });

        executor.shutdown();
    }

    private static class Val {
        int val = 0;
    }

    public static void runningTask2() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Val val = new Val();
        ReadWriteLock rwLock = new ReentrantReadWriteLock();

        Runnable readTask = () -> {
            rwLock.readLock().lock();
            System.out.println("reading the value: " + val.val);
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("exiting the reading thread: " + Thread.currentThread().getName());
                rwLock.readLock().unlock();
            }
        };

        Runnable writeTask = () -> {
            rwLock.writeLock().lock();
            int value = (int) (Math.random() * 10) + 1;
            val.val = value;
            System.out.println("writing the value: " + val.val);
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("exiting the writing thread: " + Thread.currentThread().getName());
                rwLock.writeLock().unlock();
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);
        TimeUnit.SECONDS.sleep(1);
        executor.submit(writeTask);
        executor.submit(writeTask);
        executor.submit(readTask);
        executor.shutdown();
    }

    public static void runningTask3() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Val val = new Val();
        StampedLock stampedLock = new StampedLock(); //not reentrant

        Runnable readTask = () -> {
            long stamp = stampedLock.readLock();
            System.out.println("reading the value: " + val.val);
            try {
                TimeUnit.SECONDS.sleep(TIME_TO_SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("exiting the reading thread: " + Thread.currentThread().getName());
                stampedLock.unlockRead(stamp);
            }
        };

        Runnable writeTask = () -> {
            long stamp = stampedLock.writeLock();
            int value = (int) (Math.random() * 10) + 1;
            val.val = value;
            System.out.println("writing the value: " + val.val);
            try {
                TimeUnit.SECONDS.sleep(TIME_TO_SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("exiting the writing thread: " + Thread.currentThread().getName());
                stampedLock.unlockWrite(stamp);
            }
        };

        Runnable optimisticReadTask = () -> {
            //optimistic lock doesn't prevent other threads to obtains a write lock immediately
            long stamp = stampedLock.tryOptimisticRead();
            try {
                System.out.println("valid optimisitic lock: " + stampedLock.validate(stamp));
                TimeUnit.SECONDS.sleep(TIME_TO_SLEEP/2);
                System.out.println("valid optimisitic lock: " + stampedLock.validate(stamp));
                TimeUnit.SECONDS.sleep(TIME_TO_SLEEP/2);
                System.out.println("valid optimisitic lock: " + stampedLock.validate(stamp));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("exiting the optimistic reading thread: " + Thread.currentThread().getName());
                stampedLock.unlock(stamp);
            } 
        };

        Runnable readConvertToWriteTask = () -> {
            long stamp = stampedLock.readLock();
            System.out.println("reading the value: " + val.val);
            try {
                TimeUnit.SECONDS.sleep(TIME_TO_SLEEP);
                stamp = stampedLock.tryConvertToWriteLock(stamp);
                if(stamp == 0L) {
                    System.out.println("Cannot convert to write lock");
                    stamp = stampedLock.writeLock();
                }
                int value = (int) (Math.random() * 10) + 1;
                val.val = value;
                System.out.println("readConvertToWriteTask writing the value: " + val.val);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("exiting the readConvertToWriteTask thread: " + Thread.currentThread().getName());
                stampedLock.unlock(stamp);
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);
        TimeUnit.SECONDS.sleep(1);
        executor.submit(readConvertToWriteTask);
        executor.submit(optimisticReadTask);
        executor.submit(writeTask);
        executor.submit(writeTask);
        executor.submit(readTask);
        executor.shutdown();
    }
}
