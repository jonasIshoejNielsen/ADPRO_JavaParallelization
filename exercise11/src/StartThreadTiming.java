import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

class StartThreadTiming {
    public static void main(String[] args){
        int repeat = 1000;

        timeFunc(10);
        timeFunc(20);
        timeFunc(50);
        timeFunc(100);
        timeFunc(500);
        timeFunc(1000);
    }

    public static void timeFunc(int repeat){
        ConcurrentLinkedQueue<Long> list = new ConcurrentLinkedQueue<Long>();
        CountDownLatch finish = new CountDownLatch(repeat);

        for(int i = 0; i < repeat; i++){
            final long startTime = System.nanoTime();
            Thread t = new Thread( ()->{
                long time = System.nanoTime() - startTime;
                list.add(time);
                finish.countDown();
            });
            t.start();
            t.join();
        }

        try {
            finish.await();
        } catch (InterruptedException ex) {}

        long sum = 0;
        for (long l : list)
        {
            sum += l;
        }

        System.out.printf("Time to start %,d" + " with " + repeat + " repeats\n", sum/repeat);
    }
}