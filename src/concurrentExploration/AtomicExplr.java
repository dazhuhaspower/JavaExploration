package concurrentExploration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongBinaryOperator;

/*
 * compare-and-swap operations are usually much faster than synchronizing via locks
 * */
public class AtomicExplr {

    public static void main(String[] args) {
        System.out.println("running task1");
        runningTask1();
        System.out.println("running task2");
        runningTask2();
        System.out.println("running task3");
        runningTask3();
    }

    public static void runningTask1() {
        AtomicInteger atomicInt = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for(int i=0; i<500; i++) {
            executor.submit(atomicInt::incrementAndGet);
        }
        for(int i=0; i<500; i++) {
            executor.submit(() -> {
                atomicInt.updateAndGet(x -> x + 2);
            });
        }
        for(int i=0; i<10; i++) {
            int val = i;
            executor.submit(() -> {
                atomicInt.accumulateAndGet(val, (a, b) -> a + b);
            });
        }
        executor.shutdown();
        try {
            if(executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println(atomicInt.get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        atomicInt.compareAndSet(0, 100);
        System.out.println(atomicInt);
        atomicInt.set(0);
        atomicInt.compareAndSet(0, 100);
        System.out.println(atomicInt);
    }

    public static void runningTask2() {
        //multiple copies are running to reduce the conflicts
        LongAdder adder = new LongAdder();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for(int i=0; i<500; i++) {
            executor.submit(adder::increment);
        }
        executor.shutdown();
        try {
            if(executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println(adder.sumThenReset());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void runningTask3() {
        LongBinaryOperator op = (x, y) -> -x + y;
        LongAccumulator accumulator = new LongAccumulator(op, 3L);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for(int i=0; i<10; i++) {
            int val = i;
            executor.submit(() -> {
                accumulator.accumulate(val);
                System.out.print(accumulator.get() + " ");
            });
        }
        executor.shutdown();
        try {
            if(executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println();
                //@TODO: Error here -- each time the result is different
                System.out.println(accumulator.getThenReset());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
