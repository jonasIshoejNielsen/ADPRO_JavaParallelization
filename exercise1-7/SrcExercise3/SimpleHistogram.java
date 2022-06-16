// For week 3
// sestoft@itu.dk * 2014-09-04
// thdy@itu.dk * 2019
// kasper@itu.dk * 2020


import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.LongAdder;


public class SimpleHistogram {
    public static void main(String[] args) {
        final int range = 5_000_000;
        final int threadCount = 100;
        //final Histogram histogram = new Histogram2( ((int) Math.sqrt(range)) +1);
        //final Histogram histogram = Histogram3.create(((int) Math.sqrt(range)) +1);
        //final Histogram histogram = Histogram4.create(((int) Math.sqrt(range)) +1);
        final Histogram histogram = Histogram5.create(((int) Math.sqrt(range)) +1);


        ConcurrentLinkedQueue<Thread> threads=new ConcurrentLinkedQueue<>();
        int threadRange =  range/threadCount;
        for(int i=0;i<threadCount; i++){
            final int index = i;
            Thread t1=new Thread(()->{
                for(int p=threadRange*index; p<threadRange*(index+1); p++){
                    histogram.increment(countFactors(p));
                }
            });
            threads.add(t1);
            t1.start();
        }
        try{
            for(Thread thread:threads){thread.join();}
        }
        catch(InterruptedException exn){
            System.out.println("Some thread was interrupted");
        }


        dump(histogram);
    }

    public static void dump(Histogram histogram) {
        for (int bin = 0; bin < histogram.getSpan(); bin++) {
            var v =histogram.getCount(bin);
            if (v>0) System.out.printf("%4d: %9d%n", bin, v);
        }
        System.out.printf("      %9d%n", histogram.getTotal() );
        System.out.println( Arrays.toString(histogram.getBins()) );
    }


    public static int countFactors(int p) {
        if (p < 2)
            return 0;
        int factorCount = 1, k = 2;
        while (p >= k * k) {
            if (p % k == 0) {
                factorCount++;
                p /= k;
            } else
                k++;
        }
        return factorCount;
    }

}

interface Histogram {
    public void increment(int bin);
    public int getCount(int bin);
    public float getPercentage(int bin);
    public int getSpan();
    public int getTotal();
    public int[] getBins();
}
class Histogram1 implements Histogram {
    private int[] counts;
    private int total=0;

    public Histogram1(int span) {
        this.counts = new int[span];
    }

    public void increment(int bin) {
        counts[bin] = counts[bin] + 1;
        total++;
    }

    public int getCount(int bin) {
        return counts[bin];
    }
    
    public float getPercentage(int bin){
      return getCount(bin) / getTotal() * 100;
    }

    public int getSpan() {
        return counts.length;
    }
    
    public int getTotal(){
      return total;
    }

    public int[] getBins(){return counts;}
}


class Histogram2 implements Histogram {
    private final int[] counts;
    private int total=0;

    public Histogram2(int span) {
        this.counts = new int[span];
    }

    public synchronized void increment(int bin) {
        counts[bin] = counts[bin] + 1;
        total++;
    }

    public synchronized int getCount(int bin) {
        return counts[bin];
    }

    public float getPercentage(int bin){
        return getCount(bin) / getTotal() * 100;
    }

    public int getSpan() {
        return counts.length;
    }

    public synchronized int getTotal(){
        return total;
    }

    public synchronized int[] getBins() {
        int[] result = new int[getSpan()];
        for (int i = 0; i < getSpan(); i++) {
            result[i] = counts[i];
        }
        return result;
    }
}

class Histogram3 implements Histogram {
    private final AtomicInteger[] counts;
    private AtomicInteger total=new AtomicInteger(0);

    private Histogram3(int span) {
        this.counts = new AtomicInteger[span];
        for (int i = 0; i < span; i++) {
            this.counts[i] = new AtomicInteger(0);
        }
    }
    public static Histogram3 create(int span){
        Histogram3 obj = new Histogram3(span);
        return obj;
    }

    public void increment(int bin) {
        counts[bin].incrementAndGet();
        total.incrementAndGet();
    }

    public int getCount(int bin) {
        return counts[bin].get();
    }

    public float getPercentage(int bin){
        return getCount(bin) / getTotal() * 100;
    }

    public int getSpan() {
        return counts.length;
    }

    public int getTotal(){
        return total.get();
    }

    public int[] getBins() {
        int[] result = new int[getSpan()];
        for (int i = 0; i < getSpan(); i++) {
            result[i] = counts[i].get();
        }
        return result;
    }
}


class Histogram4 implements Histogram {
    private final AtomicIntegerArray counts;
    private AtomicInteger total=new AtomicInteger(0);

    private Histogram4(int span) {
        this.counts = new AtomicIntegerArray(span);
        /*for (int i = 0; i < span; i++) {
            this.counts.set(i, 0);
        }*/
    }
    public static Histogram4 create(int span){
        Histogram4 obj = new Histogram4(span);
        return obj;
    }

    public void increment(int bin) {
        counts.getAndIncrement(bin);
        total.incrementAndGet();
    }

    public int getCount(int bin) {
        return counts.get(bin);
    }

    public float getPercentage(int bin){
        return getCount(bin) / getTotal() * 100;
    }

    public int getSpan() {
        return counts.length();
    }

    public int getTotal(){
        return total.get();
    }

    public int[] getBins() {
        int[] result = new int[getSpan()];
        for (int i = 0; i < getSpan(); i++) {
            result[i] = counts.get(i);
        }
        return result;
    }
}


class Histogram5 implements Histogram {
    private final LongAdder[] counts;
    private AtomicInteger total=new AtomicInteger(0);

    private Histogram5(int span) {
        this.counts = new LongAdder[span];
        for (int i = 0; i < span; i++) {
            this.counts[i] = new LongAdder();
        }
    }
    public static Histogram5 create(int span){
        Histogram5 obj = new Histogram5(span);
        return obj;
    }

    public void increment(int bin) {
        counts[bin].increment();
        total.incrementAndGet();
    }

    public int getCount(int bin) {
        return counts[bin].intValue();
    }

    public float getPercentage(int bin){
        return getCount(bin) / getTotal() * 100;
    }

    public int getSpan() {
        return counts.length;
    }

    public int getTotal(){
        return total.get();
    }

    public int[] getBins() {
        int[] result = new int[getSpan()];
        for (int i = 0; i < getSpan(); i++) {
            result[i] = getCount(i);
        }
        return result;
    }
}
