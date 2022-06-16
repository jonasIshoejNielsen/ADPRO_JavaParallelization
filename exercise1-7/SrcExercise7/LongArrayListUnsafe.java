// For week 7
// sestoft@itu.dk * 2015-10-29
// Changes kasper@itu.dk * 2020-10-05

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntToDoubleFunction;
import java.util.stream.*;
import java.util.Arrays;

public class LongArrayListUnsafe {
  public static void main(String[] args) {
    System.out.println("start better-add");
    Mark6("Better add:", i-> testBetter(i) );
    System.out.println("start Stripping-add");
    Mark6("Stripping add:", i-> testStripping(i) );
    System.out.println("start Synchronized-add");
    Mark6("Synchronized add:", i-> testSynchronized(i) );

    System.out.println("");
    System.out.println("");

    System.out.println("start better-get");
    final LongArrayListBetter dal1 = LongArrayListBetter.withElements(42, 7, 9, 13);
    final int size1 = dal1.size();
    Mark6("Better get:", i-> dal1.get(i%size1));
    System.out.println("start Stripping-get");
    final LongArrayListStripping dal2 = LongArrayListStripping.withElements(42, 7, 9, 13);
    final int size2 = dal2.size();
    Mark6("Stripping get:", i-> dal2.get(i%size2));
    System.out.println("start Synchronized-get");
    final LongArrayListSynchronized dal3 = LongArrayListSynchronized.withElements(42, 7, 9, 13);
    final int size3 = dal3.size();
    Mark6("Synchronized get:", i-> dal3.get(i%size3));

    System.out.println("");
    System.out.println("");

    System.out.println("start better-set");
    Mark6("Better set:", i-> dal1.set(i%size1, 1000));
    System.out.println("start Stripping-set");
    Mark6("Stripping set:", i-> dal2.set(i%size2, 1000));
    System.out.println("start Synchronized-set");
    Mark6("Synchronized set:", i-> dal3.set(i%size3, 1000));

  }
  public static double testBetter(int test_size){
    return test(test_size, LongArrayListBetter.withElements(42, 7, 9, 13));
  }
  public static double testStripping(int test_size){
    return test(test_size, LongArrayListStripping.withElements(42, 7, 9, 13));
  }
  public static double testSynchronized(int test_size){
    return test(test_size, LongArrayListSynchronized.withElements(42, 7, 9, 13));
  }

  public static double test(int test_size, LongArrayListInterface dal1){
    int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    var threads = new ArrayList<Thread>();
    for (int x = 0; x < CORE_COUNT; x++) {
      int finalX = x;
      Thread t1 = new Thread( () -> {
        for(int i = (finalX) * test_size / CORE_COUNT; i < (finalX+1) * test_size / CORE_COUNT; i++) {
          dal1.add(i);
        }
      });
      t1.run();
      threads.add(t1);
    }
    try {
      for (var t : threads) t.join();
    } catch (Exception e) {
    }
    return test_size;
  }

  public static double Mark6(String msg, IntToDoubleFunction f) {
    int n = 10, count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    do {
      count *= 2;
      st = sst = 0.0;
      for (int j=0; j<n; j++) {
        Timer t = new Timer();
        for (int i=0; i<count; i++)
          dummy += f.applyAsDouble(i);
        runningTime = t.check();
        double time = runningTime * 1e9 / count;
        st += time;
        sst += time * time;
        totalCount += count;
      }
      double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
      System.out.printf("%-25s %15.1f ns %10.2f %10d%n", msg, mean, sdev, count);
    } while (runningTime < 0.25 && count < Integer.MAX_VALUE/2);
    return dummy / totalCount;
  }


}

class LongArrayListBetter implements LongArrayListInterface {
  // Invariant: 0 <= size <= items.length
  private final ReadWriteLock lockItems;
  private final Lock rItems;
  private final Lock wItems;

  private final ReadWriteLock lockSize;
  private final Lock rSize;
  private final Lock wSize;

  private long[] items;   //todo array of longAdders
  private int size;

  private LongArrayListBetter() {
    lockItems = new ReentrantReadWriteLock();
    rItems = lockItems.readLock();
    wItems = lockItems.writeLock();
    lockSize = new ReentrantReadWriteLock();
    rSize = lockSize.readLock();
    wSize = lockSize.writeLock();
    reset();
  }
  
  public static LongArrayListBetter withElements(long... initialValues){
    LongArrayListBetter list = new LongArrayListBetter();
    for (long l : initialValues) list.add( l );
    return list;
  }
  
  // reset me to initial 
  public void reset(){
    wItems.lock();
    wSize.lock();
    try {
      items = new long[2];
      size = 0;
    }
    finally {
      wSize.unlock();
      wItems.unlock();
    }
  }

  // Number of items in the double list
  public int size() {
    rSize.lock();
    try {
      return size;
    }
    finally {
      rSize.unlock();
    }

  }

  // Return item number i
  public long get(int i) {
    rItems.lock();
    rSize.lock();
    try {
      if (0 <= i && i < size)
        return items[i];
      else
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }finally {
      rSize.unlock();
      rItems.unlock();
    }
  }

    // Replace item number i, if any, with x
  public long set(int i, long x) {
    wItems.lock();
    try{
      int size2 = size();           //locks on: rSize
      if (0 <= i && i < size2) {
        long old = items[i];
        items[i] = x;
        return old;
      }
      else
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }finally {
      wItems.unlock();
    }
  }

  // Add item x to end of list
  public LongArrayListBetter add(long x) {
    long[] newItems = new long[0];
    int size2 = 0;
    rItems.lock();
    try {
      size2 = size();           //locks on: rSize
      newItems = items;
      if (size == items.length) {
        newItems = new long[items.length * 2];
        for (int i = 0; i < items.length; i++)
          newItems[i] = items[i];
      }
    }
    finally {
      rItems.unlock();
      wItems.lock();
      try{
        items = newItems;
        items[size2] = x;
      }finally {
        wItems.unlock();
      }
      wSize.lock();
      try{
        size++;
      }finally {
        wSize.unlock();
      }
      return this;
    }
  }


  // The long list formatted as eg "[3, 4]"
  // Just messing with stream joining - not part of solution
  public String toString() {
    var stream = LongStream.empty();
    rItems.lock();
    rSize.lock();
    try{
        stream = Arrays.stream(items, 0, size);
    } finally {
      rSize.unlock();
      rItems.unlock();

    }
    return stream
            .mapToObj( Long::toString )
            .collect(Collectors.joining(", ", "[", "]"));
  }
}





//old
class LongArrayListSynchronized implements LongArrayListInterface {
  // Invariant: 0 <= size <= items.length
  private long[] items;
  private int size;

  public LongArrayListSynchronized() {
    reset();
  }

  public static LongArrayListSynchronized withElements(long... initialValues){
    LongArrayListSynchronized list = new LongArrayListSynchronized();
    for (long l : initialValues) list.add( l );
    return list;
  }

  // reset me to initial
  public synchronized void reset(){
    items = new long[2];
    size = 0;
  }

  // Number of items in the double list
  public synchronized int size() {
    return size;
  }

  // Return item number i
  public synchronized long get(int i) {
    if (0 <= i && i < size)
      return items[i];
    else
      throw new IndexOutOfBoundsException(String.valueOf(i));
  }

  // Replace item number i, if any, with x
  public synchronized long set(int i, long x) {
    if (0 <= i && i < size) {
      long old = items[i];
      items[i] = x;
      return old;
    } else
      throw new IndexOutOfBoundsException(String.valueOf(i));
  }

  // Add item x to end of list
  public synchronized LongArrayListSynchronized add(long x) {
    if (size == items.length) {
      long[] newItems = new long[items.length * 2];
      for (int i=0; i<items.length; i++)
        newItems[i] = items[i];
      items = newItems;
    }
    items[size] = x;
    size++;
    return this;
  }


  // The long list formatted as eg "[3, 4]"
  // Just messing with stream joining - not part of solution
  public synchronized String toString() {
    return Arrays.stream(items, 0,size)
            .mapToObj( Long::toString )
            .collect(Collectors.joining(", ", "[", "]"));
  }
}


class LongArrayListStripping implements LongArrayListInterface {
  // Invariant: 0 <= size <= items.length
  private long[] items;
  private int[] sizes;
  private static final int N_LOCKS = 16;
  private final Object[] locks;

  private void lockAllAndThen(Runnable action) {
    lockAllAndThen(0, action);
  }
  // This method is the one doing the work
  private void lockAllAndThen(int nextStripe, Runnable action) {
    if (nextStripe >= N_LOCKS) action.run();
    else
      synchronized (locks[nextStripe]) {
        lockAllAndThen(nextStripe + 1, action);
      }
  }


  private LongArrayListStripping() {
    locks = new Object[N_LOCKS];
    for (int i = 0; i < N_LOCKS; i++){
      locks[i] = new Object();
    }
    reset();
  }

  public static LongArrayListStripping withElements(long... initialValues){
    LongArrayListStripping list = new LongArrayListStripping();
    for (long l : initialValues) list.add( l );
    return list;
  }

  // reset me to initial
  public void reset(){
    lockAllAndThen(()-> {
      items = new long[2];
      sizes = new int[N_LOCKS];
      for (int i = 0; i < N_LOCKS; i++){
        sizes[i]=0;
      }
    });

  }

  // Number of items in the double list
  public int size() {
    int result = 0;
    for (int i = 0; i<N_LOCKS; i++){
      synchronized(locks[i]) {result+=sizes[i];}
    }
    return result;
  }
  public int sizeExcept(int size, int lock) {
    int result = size;
    for (int i = 0; i<N_LOCKS; i++){
      if (i==lock)
        continue;
      synchronized(locks[i]) {result+=sizes[i];}
    }
    return result;
  }

  // Return item number i
  public long get(int i) {
    int lockId = i%N_LOCKS;
    synchronized (locks[lockId]) {
      if (0 <= i && i < sizeExcept(sizes[lockId], lockId))
          return items[i];
      else
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }
  }

  // Replace item number i, if any, with x
  public long set(int i, long x) {
    int lockId = i%N_LOCKS;
    synchronized (locks[lockId]) {
      if (0 <= i && i < sizeExcept(sizes[lockId], lockId)) {
          long old = items[i];
          items[i] = x;
          return old;
      } else
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }
  }

  // Add item x to end of list
  public LongArrayListStripping add(long x) {
    final int size = size();
    lockAllAndThen(()->{
      if (size == items.length) {
        long[] newItems = new long[items.length * 2];
        for (int i=0; i<items.length; i++)
          newItems[i] = items[i];
        items = newItems;
      }
      items[size] = x;
      sizes[(size+1) %N_LOCKS] ++;    //size++;
    });
    return this;
  }


  // The long list formatted as eg "[3, 4]"
  // Just messing with stream joining - not part of solution
  public String toString() {
    var stream = Arrays.stream(items, 0,size());
    return stream
            .mapToObj( Long::toString )
            .collect(Collectors.joining(", ", "[", "]"));
  }
}


interface LongArrayListInterface {
  void reset();
  int size();
  long get(int i);
  long set(int i, long x);
  LongArrayListInterface add(long x);
  String toString();
}

//old
class LongArrayList implements LongArrayListInterface {
  // Invariant: 0 <= size <= items.length
  private long[] items;
  private int size;

  public LongArrayList() {
    reset();
  }

  public static LongArrayList withElements(long... initialValues){
    LongArrayList list = new LongArrayList();
    for (long l : initialValues) list.add( l );
    return list;
  }

  // reset me to initial
  public void reset(){
    items = new long[2];
    size = 0;
  }

  // Number of items in the double list
  public int size() {
    return size;
  }

  // Return item number i
  public long get(int i) {
    if (0 <= i && i < size)
      return items[i];
    else
      throw new IndexOutOfBoundsException(String.valueOf(i));
  }

  // Replace item number i, if any, with x
  public long set(int i, long x) {
    if (0 <= i && i < size) {
      long old = items[i];
      items[i] = x;
      return old;
    } else
      throw new IndexOutOfBoundsException(String.valueOf(i));
  }

  // Add item x to end of list
  public LongArrayList add(long x) {
    if (size == items.length) {
      long[] newItems = new long[items.length * 2];
      for (int i=0; i<items.length; i++)
        newItems[i] = items[i];
      items = newItems;
    }
    items[size] = x;
    size++;
    return this;
  }


  // The long list formatted as eg "[3, 4]"
  // Just messing with stream joining - not part of solution
  public String toString() {
    return Arrays.stream(items, 0,size)
            .mapToObj( Long::toString )
            .collect(Collectors.joining(", ", "[", "]"));
  }
}
