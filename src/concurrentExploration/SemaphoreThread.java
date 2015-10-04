package concurrentExploration;

import java.util.concurrent.Semaphore;

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
            for(int i=0; i<15; i++) {
                System.out.println( id );
                Thread.sleep(2000);
                //release more than acquire will increase the count
                semaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);
        for(int i=0; i<5; i++) {
            SemaphoreThread thread = new SemaphoreThread(semaphore, i);
            thread.start();
        }
    }
}
