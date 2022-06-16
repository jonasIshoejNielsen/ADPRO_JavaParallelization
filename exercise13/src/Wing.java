

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Wing {
    public static void main(String[] args) {
        test(new Wingbuffer());
        test(new WingbufferAtomic());
    }
    public static void test(BoundedBuffer b) {
        Random rand = new Random();
        int sum = 0;
        for (int i = 0; i <rand.nextInt(1000) ; i++) {
            int v = rand.nextInt();
            sum += v;
            b.Enq(v);
        }
        int sum2 = 0;
        Integer element = null;
        do{
            element = b.Deq();
            if(element!= null) sum2 += element;
        }while (element!= null);
        System.out.println(sum ==sum2);
    }
}



class WingStructure<T>{
    private List<T> elements = new ArrayList<>();
    public synchronized int INC(){
        elements.add(null);
        return elements.size() - 1;
    }
    public synchronized T STORE(int i1, T e2){
        var e1 = elements.get(i1);
        elements.set(i1,e2);
        return e1;
    }
    public synchronized T SWAP(int i1){
        var element = elements.get(i1);
        elements.set(i1, null);
        return element;
    }
    public synchronized int SIZE(){
        return elements.size();
    }
}


interface BoundedBuffer {
    void Enq(Integer elem);
    Integer Deq();
}

class Wingbuffer implements BoundedBuffer{
    private final WingStructure<Integer> items = new WingStructure();
    private int iNull = 0;
    @Override
    public void Enq(Integer elem) {
        var i = items.INC();
        items.STORE(i, elem);
    }

    @Override
    public Integer Deq() {
        for (int i = iNull; i < items.SIZE(); i++) {
            var t = items.SWAP(i);
            if (t == null) continue;
            iNull = i;
            return t;
        }
        return null;
    }
}



class WingStructureAtomic{
    private AtomicIntegerArray elements = new AtomicIntegerArray(10000);
    private AtomicIntegerArray nullelements = new AtomicIntegerArray(10000);    //val == 0 means null, vall == 1 means not null

    private AtomicInteger size = new AtomicInteger(0);
    public int INC(){
        return size.getAndIncrement();
    }
    public Integer STORE(int i1, int e2){
        var e1 = elements.get(i1);
        nullelements.set(i1, 1);
        elements.set(i1,e2);
        return e1;
    }
    public Integer SWAP(int i1){
        if (i1 >= size.get()) throw new IndexOutOfBoundsException("On "+i1);
        if (nullelements.get(i1)==0)
            return null;
        var element = elements.get(i1);
        nullelements.set(i1, 0);
        return element;
    }
    public synchronized int SIZE(){
        return size.get();
    }
}


class WingbufferAtomic implements BoundedBuffer{
    private final WingStructureAtomic items = new WingStructureAtomic();
    private int iNull = 0;

    public void Enq(Integer elem) {
        var i = items.INC();
        items.STORE(i, elem);
    }

    public Integer Deq() {
        for (int i = iNull; i < items.SIZE(); i++) {
            var t = items.SWAP(i);
            if (t == null) continue;
            iNull = i;
            return t;
        }
        return null;
    }
}
