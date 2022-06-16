

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class oneDotOneDotBoundedBuffer {

    public static void main(String[] args) {
        int bufferSize = 100000;
        int threadCount = 200;
        OurBoundedBuffer<Integer> lst = OurBoundedBuffer.FactoryMethod(bufferSize);

        ConcurrentLinkedQueue<Thread> threads=new ConcurrentLinkedQueue<>();
        for(int i=0;i<threadCount*2; i++){
            Thread t1=new Thread(()->{
                for(int x=0;x<2*bufferSize/threadCount; x++){
                    lst.insert(x);
                }
            });
            threads.add(t1);
        }
        for(int i=0;i<threadCount; i++){
            Thread t1=new Thread(()->{
                for(int x=0;x<3*bufferSize/threadCount; x++){
                    lst.take();
                }
            });
            threads.add(t1);
        }
        for(Thread thread:threads){thread.start();}
        try{
            for(Thread thread:threads){thread.join();}
        }
        catch(InterruptedException exn){
            System.out.println("Some thread was interrupted");
        }
        System.out.println(lst.size() + " = "+ bufferSize +" , sem: " + lst.permitsLeft() + " , timesBlockedFull: "+ lst.timesBlockedFull()  + " , timesBlockedEmpty: "+ lst.timesBlockedEmpty());

    }
}




interface BoundedBuffer<T> {
    void insert(T elem);
    T take();
}

class OurBoundedBuffer<T> implements BoundedBuffer {
    private final List<T> lst;
    private final Semaphore sem;
    private final AtomicInteger timesBlockedFull;
    private final AtomicInteger timesBlockedEmpty;

    private OurBoundedBuffer(int bufferSize) {
        this.sem                = new Semaphore(bufferSize, true);
        this.lst                = Collections.synchronizedList(new ArrayList());
        this.timesBlockedFull   = new AtomicInteger(0);
        this.timesBlockedEmpty  = new AtomicInteger(0);
    }
    public static OurBoundedBuffer FactoryMethod(int bufferSize){
        OurBoundedBuffer obj = new OurBoundedBuffer(bufferSize);
        return obj;
    }


    @java.lang.Override
    public void insert(Object elem) {
        boolean waiting = true;
        boolean wasAdded = false;
        while (!wasAdded){
            try {
                //no counting:
                if(waiting) {
                    if(permitsLeft() <= 0){
                        timesBlockedFull.incrementAndGet();
                    }
                    sem.acquire();
                }
                else {//counting:
                    boolean blocked = sem.tryAcquire(0, TimeUnit.SECONDS);
                    if (!blocked) {
                        timesBlockedFull.incrementAndGet();
                        continue;
                    }
                }
                //rest
                wasAdded = lst.add((T)elem);
                if(!wasAdded) sem.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object take() {
        Object item = null;
        boolean wasRemoved = false;
        while (!wasRemoved){
            try{
                item = lst.remove(0);
                wasRemoved = true;
            }
            catch (java.lang.IndexOutOfBoundsException e){
                timesBlockedEmpty.incrementAndGet();
                continue;
            }
            sem.release();
        }
        return item;
    }

    public int size(){
        return lst.size();
    }
    public int permitsLeft(){
        return sem.availablePermits();
    }
    public int timesBlockedFull(){
        return timesBlockedFull.get();
    }
    public int timesBlockedEmpty(){
        return timesBlockedEmpty.get();
    }
}
