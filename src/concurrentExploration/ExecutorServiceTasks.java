package concurrentExploration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTasks {
    private static int totalSum = 0;

    public static void main(String[] args) {
        runningTask1();
        runningTask2();
        runningTask3();
        runningTask4();
    }

    public static void runningTask1() {
        totalSum = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future> taskList = new ArrayList<Future>();
        for(int i=0; i<1000; i++) {
            Future future = executorService.submit(new CountingTask(i));
            taskList.add(future);
        }
        for(Future future: taskList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        System.out.println(totalSum);
    }

    public static void runningTask2() {
        int sum = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<Integer>> taskList = new ArrayList<Future<Integer>>();
        for(int i=0; i<1000; i++) {
            Future<Integer> future = executorService.submit(new CountingMission(i));
            taskList.add(future);
        }
        for(Future<Integer> future: taskList) {
            try {
                sum += future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        System.out.println(sum);
    }

    public static void runningTask3() {
        int sum = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Set<Callable<Integer>> tasks = new HashSet<Callable<Integer>>();
        for(int i=0; i<1000; i++) {
            tasks.add(new CountingMission(i));
        }
        List<Future<Integer>> taskList;
        try {
            taskList = executorService.invokeAll(tasks);
            for(Future<Integer> future: taskList) {
                try {
                    sum += future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        executorService.shutdown();
        System.out.println(sum);
    }

    public static void runningTask4() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
        final ScheduledFuture<?> dogBeeperHandle = executorService.scheduleAtFixedRate(new Beeper("Woof"), 8, 2, TimeUnit.SECONDS);

        //set to cancel the dog beep after 1 minutes
        executorService.schedule(new Runnable() {
            public void run() {
                dogBeeperHandle.cancel(true);
            }
        }, 1, TimeUnit.MINUTES);

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.scheduleWithFixedDelay(new Beeper("Miaow"), 3, 1, TimeUnit.SECONDS);

        //shut down in 2 minutes
        executorService.schedule(new Runnable() {
            public void run() {
                executorService.shutdown();
            }
        }, 2, TimeUnit.MINUTES);
    }

    public static class CountingTask implements Runnable {
        int count;
        public CountingTask(int count) {
            this.count = count;
        }

        @Override
        public void run() {
            synchronized(ExecutorServiceTasks.class) {
                totalSum += count;
            }
        }
    }

    public static class CountingMission implements Callable<Integer> {
        int count;
        public CountingMission(int count) {
            this.count = count;
        }
        @Override
        public Integer call() throws Exception {
            return count;
        }

    }

    public static class Beeper implements Runnable {
        String msg;
        public Beeper(String msg) {
            this.msg = msg;
        }
        public void run() {
            System.out.println(msg);
        }
    }
}
