// For week 7 -- four incomplete implementations of concurrent hash maps
// sestoft@itu.dk * 2014-10-07, 2015-09-25

// Parts of the code are missing.  Your task in the exercises is to
// write the missing parts.

import java.util.*;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;

public class TestStripedMap {
  public static void main(String[] args) {
    SystemInfo();
    //testAllMaps();    // Must be run with: java -ea TestStripedMap
    //12.2.1
   // testAllMapsGood();

    //12.2.2
    //testAllMapsConcurrent(v -> new StripedWriteMap<Integer,String>(77, 7));
    //testAllMapsConcurrent(v -> new StripedMap<Integer,String>(77, 7));
    //testAllMapsConcurrent(v -> new WrapConcurrentHashMap<>());

    //12.2.5
    //testMapCounting();

    //12.3.*
    for (int i = 0; i <10000 ; i++) {
      testAllMapsConcurrent(v -> new StripedMap<>(77, 7));
    }


  }

  private static void timeAllMaps() {
    final int bucketCount = 100_000, lockCount = 32;
    for (int t=1; t<=32; t++) {
      final int threadCount = t;
      Mark7(String.format("%-21s %d", "SynchronizedMap", threadCount),
            i -> timeMap(threadCount, 
                         new SynchronizedMap<Integer,String>(bucketCount)));
      Mark7(String.format("%-21s %d", "StripedMap", threadCount),
            i -> timeMap(threadCount, 
                         new StripedMap<Integer,String>(bucketCount, lockCount)));
      Mark7(String.format("%-21s %d", "StripedWriteMap", threadCount), 
            i -> timeMap(threadCount, 
                         new StripedWriteMap<Integer,String>(lockCount, lockCount)));
      Mark7(String.format("%-21s %d", "WrapConcHashMap", threadCount),
            i -> timeMap(threadCount, 
                         new WrapConcurrentHashMap<Integer,String>()));
    }
  }

  // TO BE HANDED OUT
  private static double timeMap(int threadCount, final OurMap<Integer, String> map) {
    final int iterations = 5_000_000, perThread = iterations / threadCount;
    final int range = 200_000;
    return exerciseMap(threadCount, perThread, range, map);
  }

  // TO BE HANDED OUT
  private static double exerciseMap(int threadCount, int perThread, int range, 
                                    final OurMap<Integer, String> map) {
    Thread[] threads = new Thread[threadCount];
    for (int t=0; t<threadCount; t++) {
      final int myThread = t;
      threads[t] = new Thread(() -> {
        Random random = new Random(37 * myThread + 78);
        for (int i=0; i<perThread; i++) {
          Integer key = random.nextInt(range);
          if (!map.containsKey(key)) {
            // Add key with probability 60%
            if (random.nextDouble() < 0.60) 
              map.put(key, Integer.toString(key));
          } 
          else // Remove key with probability 2% and reinsert
            if (random.nextDouble() < 0.02) {
              map.remove(key);
              map.putIfAbsent(key, Integer.toString(key));
            }
        }
        final AtomicInteger ai = new AtomicInteger();
        map.forEach(new BiConsumer<Integer,String>() {
            public void accept(Integer k, String v) {
              ai.getAndIncrement();
        }});
        // System.out.println(ai.intValue() + " " + map.size());
      });
    }
    for (int t=0; t<threadCount; t++) 
      threads[t].start();
    map.reallocateBuckets();
    try {
      for (int t=0; t<threadCount; t++) 
        threads[t].join();
    } catch (InterruptedException exn) { }
    return map.size();
  }

  private static void exerciseAllMaps() {
    final int bucketCount = 100_000, lockCount = 32, threadCount = 16;
    final int iterations = 1_600_000, perThread = iterations / threadCount;
    final int range = 100_000;
    System.out.println(Mark7(String.format("%-21s %d", "SynchronizedMap", threadCount),
      i -> exerciseMap(threadCount, perThread, range,
                       new SynchronizedMap<Integer,String>(bucketCount))));
    System.out.println(Mark7(String.format("%-21s %d", "StripedMap", threadCount),
      i -> exerciseMap(threadCount, perThread, range,
                       new StripedMap<Integer,String>(bucketCount, lockCount))));
    System.out.println(Mark7(String.format("%-21s %d", "StripedWriteMap", threadCount), 
      i -> exerciseMap(threadCount, perThread, range,
                       new StripedWriteMap<Integer,String>(lockCount, lockCount))));
    System.out.println(Mark7(String.format("%-21s %d", "WrapConcHashMap", threadCount),
      i -> exerciseMap(threadCount, perThread, range,
                       new WrapConcurrentHashMap<Integer,String>())));
  }

  // Very basic sequential functional test of a hash map.  You must
  // run with assertions enabled for this to work, as in 
  //   java -ea TestStripedMap
  private static void testMap(final OurMap<Integer, String> map) {
    System.out.printf("%n%s%n", map.getClass());
    assert map.size() == 0;
    assert !map.containsKey(117);
    assert !map.containsKey(-2);
    assert map.get(117) == null;
    assert map.put(117, "A") == null;
    assert map.containsKey(117);
    assert map.get(117).equals("A");
    assert map.put(17, "B") == null;
    assert map.size() == 2;
    assert map.containsKey(17);
    assert map.get(117).equals("A");
    assert map.get(17).equals("B");
    assert map.put(117, "C").equals("A");
    assert map.containsKey(117);
    assert map.get(117).equals("C");
    assert map.size() == 2;
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
    assert map.remove(117).equals("C");
    assert !map.containsKey(117);
    assert map.get(117) == null;
    assert map.size() == 1;
    assert map.putIfAbsent(17, "D").equals("B");
    assert map.get(17).equals("B");
    assert map.size() == 1;
    assert map.containsKey(17);
    assert map.putIfAbsent(217, "E") == null;
    assert map.get(217).equals("E");
    assert map.size() == 2;
    assert map.containsKey(217);
    assert map.putIfAbsent(34, "F") == null;
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
    map.reallocateBuckets();
    assert map.size() == 3;
    assert map.get(17).equals("B") && map.containsKey(17);
    assert map.get(217).equals("E") && map.containsKey(217);
    assert map.get(34).equals("F") && map.containsKey(34);
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));    
    map.reallocateBuckets();
    assert map.size() == 3;
    assert map.get(17).equals("B") && map.containsKey(17);
    assert map.get(217).equals("E") && map.containsKey(217);
    assert map.get(34).equals("F") && map.containsKey(34);
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
  }

  private static void testAllMaps() {
    testMap(new SynchronizedMap<Integer,String>(25));
    testMap(new StripedMap<Integer,String>(25, 5));
    testMap(new StripedWriteMap<Integer,String>(25, 5));
    testMap(new WrapConcurrentHashMap<Integer,String>());
  }

  private static void testMapPut(final OurMap<Integer, String> map, ConcurrentHashMap<Integer, String> helper, int threadNumber) {
    Random rand = new Random();
    Set<Integer> intSet = new HashSet<>();
    for (int inserts =0; inserts<rand.nextInt(10000); inserts++){
      int v = rand.nextInt(100000);
      intSet.add(v);

      helper.put(v, "");
      var value = map.get(v);
      map.put(v, threadNumber+":"+v);
      assert value != map.get(v);
    }

    //done with setup
    for (int v : intSet) {
      String value = map.get(v);
      assert value != null;
      assert value.endsWith(""+v);
    }
  }
  private static void testMapPutIfAbsent(final OurMap<Integer, String> map, int bestKey, int threadNumber) {
    var rand = new Random();
    for (int inserts =0; inserts<rand.nextInt(10000); inserts++){
      int v = rand.nextInt();
      var value = map.get(v);
      map.putIfAbsent(bestKey, threadNumber+":"+v);
      assert value == map.get(v);
    }
    assert map.get(bestKey).equals(0+":"+bestKey);
  }


  private static void testMapContainsKey(final OurMap<Integer, String> map,ConcurrentHashMap<Integer, String> helper, int threadNumber) {
    Random rand = new Random();
    Set<Integer> intSet = new HashSet<>();
    for (int inserts =0; inserts<rand.nextInt(10000); inserts++){
      int v = rand.nextInt();
      helper.put(v,threadNumber+":"+v);
      map.put(v, threadNumber+":"+v);
      assert map.containsKey(v);
    }
  }

  private static long testMapRemove(final OurMap<Integer, String> map, int threadNumber) {
    Random rand = new Random();
    Set<Integer> intSet = new HashSet<>();
    for (int inserts =0; inserts<rand.nextInt(10000); inserts++){
      int v = rand.nextInt();
      intSet.add(v);
      map.put(v, threadNumber+":"+v);
      
    }
    for (int v : intSet) {
      map.remove(v);
    }
    long sum = 0;
    //done with setup
    for (int v : intSet) {
      assert map.get(v) == null;
       sum += v;
    }

    return sum;

  }
  private static void testAllMapsGood() {
    var hm = new ConcurrentHashMap<Integer, String>();
    testMapPut(new StripedWriteMap<Integer,String>(25, 5), hm, 0);
    Random rand = new Random();
    int bestKey = rand.nextInt();

    var map = new StripedWriteMap<Integer,String>(25, 5);
    map.put(bestKey, 0+":"+bestKey);
    testMapPutIfAbsent(map, bestKey, 0);
    testMapRemove(new StripedWriteMap<Integer,String>(25, 5), 0);
    var hm2 = new ConcurrentHashMap<Integer, String>();
    testMapContainsKey(new StripedWriteMap<Integer,String>(25, 5), hm2, 0);

  }

  private static void testAllMapsConcurrent(IntFunction<OurMap<Integer,String>> f) {
    Random rand = new Random();
    int bestKey = rand.nextInt();

    var putMap =    f.apply(0);
    var removeMap = f.apply(0);
    var putIfAbsentMap = f.apply(0);
    var ContainsKeyMap = f.apply(0);



    putIfAbsentMap.put(bestKey, 0+":"+bestKey);

    var hm = new ConcurrentHashMap<Integer, String>();
    var hmContains = new ConcurrentHashMap<Integer, String>();
    var numThreads = 16;
    final var barrier = new CyclicBarrier(numThreads + 1);
    try {
      for (int i = 0; i < numThreads; i++) {
        final int threadNumber = i;
        new Thread(() -> {
        try {
          barrier.await();
          runTest(map -> testMapPut(map, hm, threadNumber), putMap, "put test failed!");
          barrier.await();
          runTest(map -> testMapRemove(map, threadNumber), removeMap, "remove test failed!");
          barrier.await();
          runTest(map -> testMapPutIfAbsent(map, bestKey, threadNumber), putIfAbsentMap, "put if absent test failed!");
          barrier.await();
          runTest(map -> testMapContainsKey(map,hmContains, threadNumber), ContainsKeyMap, "contains key test failed!");
          barrier.await();
        } catch (Exception e) {System.out.println("THIS SHOULD BE A BARRIER EXCEPTION"); System.out.println(e);}

        }).start();
      }
      barrier.await();
      //System.out.println("Start putting");
      barrier.await();
      //System.out.println("Dome putting, now removing");
      barrier.await();
      //System.out.println("done removing, now put if absent");
      barrier.await();
      //System.out.println("done putting, now contains key");
      barrier.await();
      //System.out.println("done with all, now test sums");

      try {
        assert sizeOfOurMap(putMap) == sizeOfConcurrentHashMap(hm);
      }catch(AssertionError e) {System.out.println("Put"); }
        try {
        assert sizeOfOurMap(removeMap) ==  0L;
      }catch(AssertionError e) {System.out.println("remove"); }
      try {
        assert sizeOfOurMap(putIfAbsentMap) ==  bestKey;
      }catch(AssertionError e) {System.out.println("putIfAbsent"); }
      try {
        assert sizeOfOurMap(ContainsKeyMap) ==  sizeOfConcurrentHashMap(hmContains);
      }catch(AssertionError e) {System.out.println("Contains"); }
    } catch(AssertionError e) {
      System.out.println(e);
    }
    catch (Exception e){}


    //System.out.println("done burrito");

  }


  static long sizeOfOurMap(OurMap<Integer, String> ourMap){
    final var sum = new AtomicLong(0);
    ourMap.forEach((key, value) -> sum.addAndGet(key));
    return sum.get();
  }

  static long sizeOfConcurrentHashMap(ConcurrentHashMap<Integer, String> ourMap){
    final var sum = new AtomicLong(0);
    ourMap.forEach((key, value) -> sum.addAndGet(key));
    return sum.get();
  }

  static void runTest(Consumer<OurMap<Integer, String>> f, OurMap<Integer, String> ourMap, String testError){
    try {
      f.accept(ourMap);
    } catch (AssertionError e) {
      System.out.println(testError);
    }
  }






  static void testMapCounting(){
    var ourMap =    new WrapConcurrentHashMap<Integer,Integer>();
    var numThreads = 16;
    final AtomicInteger[] count = new AtomicInteger[numThreads];
    for(int i=0; i<count.length; i++){
      count[i] = new AtomicInteger(0);
    }
    final var barrier = new CyclicBarrier(numThreads + 1);
    try {
      for (int i = 0; i < numThreads; i++) {
        final int threadNumber = i;
        new Thread(() -> {
          Random rand = new Random();
          try {
            barrier.await();
            for (int x = 0; x<10000; x++){
              int v = rand.nextInt() % 100;
              if(ourMap.containsKey(v)){
                var u = ourMap.remove(v);
                assert u != null;
                count[u].decrementAndGet();
              }
              else{
                ourMap.put(v, threadNumber);
                count[threadNumber].incrementAndGet();
              }
            }
          } catch (Exception e) {System.out.println("THIS SHOULD BE A BARRIER EXCEPTION"); System.out.println(e);}
          catch (AssertionError e) { System.out.println(e);}
          finally {
            try {
              barrier.await();
            } catch (InterruptedException e) {
              e.printStackTrace();
            } catch (BrokenBarrierException e) {
              e.printStackTrace();
            }
          }

        }).start();
      }
      barrier.await();
      System.out.println("Start test");
      barrier.await();
      System.out.println("");
      System.out.println("start comparing counts:");
      var countReal = NumberOfKeysInMap(ourMap, numThreads);
      for(int i=0; i< numThreads; i++){
        assert count[i].get() == countReal[i];
      }


    } catch(AssertionError e) {
      System.out.println(e);
    }
    catch (Exception e){}


    System.out.println("done burrito");
  }
  static int[] NumberOfKeysInMap(OurMap<Integer, Integer> ourMap, int numberOfThreads){
    final int[] count = new int[numberOfThreads];
    ourMap.forEach((key, value) -> count[value]++);
    return count;
  }












  // --- Benchmarking infrastructure ---

  private static class Timer {
    private long start, spent = 0;
    public Timer() { play(); }
    public double check() { return (System.nanoTime()-start+spent)/1e9; }
    public void pause() { spent += System.nanoTime()-start; }
    public void play() { start = System.nanoTime(); }
  }

  // NB: Modified to show microseconds instead of nanoseconds

  public static double Mark7(String msg, IntToDoubleFunction f) {
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
        double time = runningTime * 1e6 / count; // microseconds
        st += time; 
        sst += time * time;
        totalCount += count;
      }
    } while (runningTime < 0.25 && count < Integer.MAX_VALUE/2);
    double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    System.out.printf("%-25s %15.1f us %10.2f %10d%n", msg, mean, sdev, count);
    return dummy / totalCount;
  }

  public static void SystemInfo() {
    System.out.printf("# OS:   %s; %s; %s%n", 
                      System.getProperty("os.name"), 
                      System.getProperty("os.version"), 
                      System.getProperty("os.arch"));
    System.out.printf("# JVM:  %s; %s%n", 
                      System.getProperty("java.vendor"), 
                      System.getProperty("java.version"));
    // The processor identifier works only on MS Windows:
    System.out.printf("# CPU:  %s; %d \"cores\"%n", 
                      System.getenv("PROCESSOR_IDENTIFIER"),
                      Runtime.getRuntime().availableProcessors());
    java.util.Date now = new java.util.Date();
    System.out.printf("# Date: %s%n", 
      new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(now));
  }
}

interface BiConsumer<K,V> {
  void accept(K k, V v);
}

interface OurMap<K,V> {
  boolean containsKey(K k);
  V get(K k);
  V put(K k, V v);
  V putIfAbsent(K k, V v);
  V remove(K k);
  int size();
  void forEach(BiConsumer<K,V> biConsumer);
  void reallocateBuckets();
}

// ----------------------------------------------------------------------
// A hashmap that permits thread-safe concurrent operations, similar
// to a synchronized version of HashMap<K,V>.

class SynchronizedMap<K,V> implements OurMap<K,V>  {
  // Synchronization policy: 
  //   buckets[hash] and cachedSize are guarded by this
  private ItemNode<K,V>[] buckets;
  private int cachedSize;
  
  public SynchronizedMap(int bucketCount) {
    this.buckets = makeBuckets(bucketCount);
  }

  @SuppressWarnings("unchecked") 
  private static <K,V> ItemNode<K,V>[] makeBuckets(int size) {
    // Java's @$#@?!! type system requires this unsafe cast    
    return (ItemNode<K,V>[])new ItemNode[size];
  }

  // Protect against poor hash functions and make non-negative
  private static <K> int getHash(K k) {
    final int kh = k.hashCode();
    return (kh ^ (kh >>> 16)) & 0x7FFFFFFF;  
  }

  // Return true if key k is in map, else false
  public synchronized boolean containsKey(K k) {
    final int h = getHash(k), hash = h % buckets.length;
    return ItemNode.search(buckets[hash], k) != null;
  }

  // Return value v associated with key k, or null
  public synchronized V get(K k) {
    final int h = getHash(k), hash = h % buckets.length;
    ItemNode<K,V> node = ItemNode.search(buckets[hash], k);
    if (node != null) 
      return node.v;
    else
      return null;
  }

  public synchronized int size() {
    return cachedSize;
  }

  // Put v at key k, or update if already present 
  public synchronized V put(K k, V v) {
    final int h = getHash(k), hash = h % buckets.length;
    ItemNode<K,V> node = ItemNode.search(buckets[hash], k);
    if (node != null) {
      V old = node.v;
      node.v = v;
      return old;
    } else {
      buckets[hash] = new ItemNode<K,V>(k, v, buckets[hash]);
      cachedSize++;
      return null;
    }
  }

  // Put v at key k only if absent
  public synchronized V putIfAbsent(K k, V v) {
    final int h = getHash(k), hash = h % buckets.length;
    ItemNode<K,V> node = ItemNode.search(buckets[hash], k);
    if (node != null)
      return node.v;
    else {
      buckets[hash] = new ItemNode<K,V>(k, v, buckets[hash]);
      cachedSize++;
      return null;
    }
  }

  // Remove and return the value at key k if any, else return null
  public synchronized V remove(K k) {
    final int h = getHash(k), hash = h % buckets.length;
    ItemNode<K,V> prev = buckets[hash];
    if (prev == null) 
      return null;
    else if (k.equals(prev.k)) {        // Delete first ItemNode
      V old = prev.v;
      cachedSize--;
      buckets[hash] = prev.next;
      return old;
    } else {                            // Search later ItemNodes
      while (prev.next != null && !k.equals(prev.next.k))
        prev = prev.next;
      // Now prev.next == null || k.equals(prev.next.k)
      if (prev.next != null) {  // Delete ItemNode prev.next
        V old = prev.next.v;
        cachedSize--; 
        prev.next = prev.next.next;
        return old;
      } else
        return null;
    }
  }

  // Iterate over the hashmap's entries one bucket at a time
  public synchronized void forEach(BiConsumer<K,V> biConsumer) {
    for (int hash=0; hash<buckets.length; hash++) {
      ItemNode<K,V> node = buckets[hash];
      while (node != null) {
        biConsumer.accept(node.k, node.v);
        node = node.next;
      }
    }
  }

  // Double bucket table size, rehash, and redistribute entries.

  public synchronized void reallocateBuckets() {
    final ItemNode<K,V>[] newBuckets = makeBuckets(2 * buckets.length);
    for (int hash=0; hash<buckets.length; hash++) {
      ItemNode<K,V> node = buckets[hash];
      while (node != null) {
        final int newHash = getHash(node.k) % newBuckets.length;
        ItemNode<K,V> next = node.next;
        node.next = newBuckets[newHash];
        newBuckets[newHash] = node;
        node = next;
      }
    }
    buckets = newBuckets;
  }

  static class ItemNode<K,V> {
    private final K k;
    private V v;
    private ItemNode<K,V> next;
    
    public ItemNode(K k, V v, ItemNode<K,V> next) {
      this.k = k;
      this.v = v;
      this.next = next;
    }

    public static <K,V> ItemNode<K,V> search(ItemNode<K,V> node, K k) {
      while (node != null && !k.equals(node.k))
        node = node.next;
      return node;
    }
  }
}

// ----------------------------------------------------------------------
// A hash map that permits thread-safe concurrent operations, using
// lock striping (intrinsic locks on Objects created for the purpose).

// NOT IMPLEMENTED: get, putIfAbsent, size, remove and forEach.

// The bucketCount must be a multiple of the number lockCount of
// stripes, so that h % lockCount == (h % bucketCount) % lockCount and
// so that h % lockCount is invariant under doubling the number of
// buckets in method reallocateBuckets.  Otherwise there is a risk of
// locking a stripe, only to have the relevant entry moved to a
// different stripe by an intervening call to reallocateBuckets.

class StripedMap<K,V> implements OurMap<K,V> {
  // Synchronization policy: 
  //   buckets[hash] is guarded by locks[hash%lockCount]
  //   sizes[stripe] is guarded by locks[stripe]
  private volatile ItemNode<K,V>[] buckets;
  private final int lockCount;
  private final Object[] locks;
  private final int[] sizes;

  public StripedMap(int bucketCount, int lockCount) {
    if (bucketCount % lockCount != 0)
      throw new RuntimeException("bucket count must be a multiple of stripe count");
    this.lockCount = lockCount;
    this.buckets = makeBuckets(bucketCount);
    this.locks = new Object[lockCount];
    this.sizes = new int[lockCount];
    for (int stripe=0; stripe<lockCount; stripe++) 
      this.locks[stripe] = new Object();
  }

  @SuppressWarnings("unchecked") 
  private static <K,V> ItemNode<K,V>[] makeBuckets(int size) {
    // Java's @$#@?!! type system requires this unsafe cast    
    return (ItemNode<K,V>[])new ItemNode[size];
  }

  // Protect against poor hash functions and make non-negative
  private static <K> int getHash(K k) {
    final int kh = k.hashCode();
    return (kh ^ (kh >>> 16)) & 0x7FFFFFFF;  
  }

  // Return true if key k is in map, else false
  public boolean containsKey(K k) {
    final int h = getHash(k), stripe = h % lockCount;
    synchronized (locks[stripe]) {
      final int hash = h % buckets.length;
      return ItemNode.search(buckets[hash], k) != null;
    }
  }

  // Return value v associated with key k, or null
  public V get(K k) {
    final int h = getHash(k), hash = h % buckets.length;
    StripedMap.ItemNode<K, V> node = null;
    synchronized (locks[hash % lockCount]) {
      node = StripedMap.ItemNode.search(buckets[hash], k);
    }
    if (node != null)
      return node.v;
    else
      return null;
  }

  public int size() {
    int result = 0;
    for (int i = 0; i<sizes.length; i++){
      synchronized(locks[i]) {result+=sizes[i];}
    }
    return result;
  }

  // Put v at key k, or update if already present 
  public V put(K k, V v) {
    final int h = getHash(k), stripe = h % lockCount;
    synchronized (locks[stripe]) {
      final int hash = h % buckets.length;
      final ItemNode<K,V> node = ItemNode.search(buckets[hash], k);
      if (node != null) {
        V old = node.v;
        node.v = v;
        return old;
      } else {
        buckets[hash] = new ItemNode<K,V>(k, v, buckets[hash]);
        sizes[stripe]++;
        return null;
      }
    }
  }

  // Put v at key k only if absent
  public V putIfAbsent(K k, V v) {
    final int h = getHash(k), hash = h % buckets.length;
    StripedMap.ItemNode<K, V> node = null;
    int newSize;
    synchronized (locks[hash % lockCount]) {
      node = StripedMap.ItemNode.search(buckets[hash], k);
      if (node != null)
        return node.v;
      else {
        buckets[hash] = new StripedMap.ItemNode<K,V>(k, v, buckets[hash]);
        sizes[hash % lockCount]++;
        newSize = sizes[hash%lockCount];
      }
    }
    if (newSize * lockCount > buckets.length)
      reallocateBuckets();

    return null;

  }

  // Remove and return the value at key k if any, else return null
  public V remove(K k) {
    final int h = getHash(k), hash = h % buckets.length;
    synchronized (locks[hash % lockCount]){

      StripedMap.ItemNode<K,V> prev = buckets[hash];
      if (prev == null)
        return null;
      else if (k.equals(prev.k)) {        // Delete first ItemNode
        V old = prev.v;
        sizes[hash % lockCount]--;
        buckets[hash] = prev.next;
        return old;
      } else {                            // Search later ItemNodes
        while (prev.next != null && !k.equals(prev.next.k))
          prev = prev.next;
        // Now prev.next == null || k.equals(prev.next.k)
        if (prev.next != null) {  // Delete ItemNode prev.next
          V old = prev.next.v;
          sizes[hash % lockCount]--;
          prev.next = prev.next.next;
          return old;
        } else
          return null;
      }
    }
  }

  // Iterate over the hashmap's entries one stripe at a time;
  // stripewise less locking and more concurrency.  An intervening
  // reallocateBuckets (cannot happen while holding the lock on a
  // stripe so no need to take a local copy bs of the buckets field)
  // may redistribute items between buckets but each item stays in the
  // same stripe.
  public void forEach(BiConsumer<K,V> biConsumer) {
    int bucketLenght = buckets.length;
    for(int stripe=0; stripe<lockCount; stripe++){       //for each stripe
      synchronized (locks[stripe]){
        for(int i=stripe; i<bucketLenght; i+=lockCount){    //if: 3=locks   0,3,6,9,12,13
          StripedMap.ItemNode<K,V> node = buckets[i];
          while (node != null){
            biConsumer.accept(node.k, node.v);
            node=node.next;
          }
        }
      }
    }
  }

  // First lock all stripes.  Then double bucket table size, rehash,
  // and redistribute entries.  Since the number of stripes does not
  // change, and since buckets.length is a multiple of lockCount, a
  // key that belongs to stripe s because (getHash(k) % N) %
  // lockCount == s will continue to belong to stripe s.  Hence the
  // sizes array need not be recomputed.

  public void reallocateBuckets() {
    lockAllAndThen(() -> {
        final ItemNode<K,V>[] newBuckets = makeBuckets(2 * buckets.length);
        for (int hash=0; hash<buckets.length; hash++) {
          ItemNode<K,V> node = buckets[hash];
          while (node != null) {
            final int newHash = getHash(node.k) % newBuckets.length;
            ItemNode<K,V> next = node.next;
            node.next = newBuckets[newHash];
            newBuckets[newHash] = node;
            node = next;
          }
        }
        buckets = newBuckets;
      });
  }
  
  // Lock all stripes, perform the action, then unlock all stripes
  private void lockAllAndThen(Runnable action) {
    lockAllAndThen(0, action);
  }

  private void lockAllAndThen(int nextStripe, Runnable action) {
    if (nextStripe >= lockCount)
      action.run();
    else 
      synchronized (locks[nextStripe]) {
        lockAllAndThen(nextStripe + 1, action);
      }
  }

  static class ItemNode<K,V> {
    private final K k;
    private V v;
    private ItemNode<K,V> next;
    
    public ItemNode(K k, V v, ItemNode<K,V> next) {
      this.k = k;
      this.v = v;
      this.next = next;
    }

    // Assumes locks[getHash(k) % lockCount] is held by the thread
    public static <K,V> ItemNode<K,V> search(ItemNode<K,V> node, K k) {
      while (node != null && !k.equals(node.k))
        node = node.next;
      return node;
    }
  }
}

// ----------------------------------------------------------------------
// A hashmap that permits thread-safe concurrent operations, using
// lock striping (intrinsic locks on Objects created for the purpose),
// and with immutable ItemNodes, so that reads do not need to lock at
// all, only need visibility of writes, which is ensured through the
// AtomicIntegerArray called sizes.

// NOT IMPLEMENTED: get, putIfAbsent, size, remove and forEach.

// The bucketCount must be a multiple of the number lockCount of
// stripes, so that h % lockCount == (h % bucketCount) % lockCount and
// so that h % lockCount is invariant under doubling the number of
// buckets in method reallocateBuckets.  Otherwise there is a risk of
// locking a stripe, only to have the relevant entry moved to a
// different stripe by an intervening call to reallocateBuckets.

class StripedWriteMap<K,V> implements OurMap<K,V> {
  // Synchronization policy: writing to
  //   buckets[hash] is guarded by locks[hash % lockCount]
  //   sizes[stripe] is guarded by locks[stripe]
  // Visibility of writes to reads is ensured by writes writing to
  // the stripe's size component (even if size does not change) and
  // reads reading from the stripe's size component.
  private volatile ItemNode<K,V>[] buckets;
  private final int lockCount;
  private final Object[] locks;
  private final AtomicIntegerArray sizes;  

  public StripedWriteMap(int bucketCount, int lockCount) {
    if (bucketCount % lockCount != 0)
      throw new RuntimeException("bucket count must be a multiple of stripe count");
    this.lockCount = lockCount;
    this.buckets = makeBuckets(bucketCount);
    this.locks = new Object[lockCount];
    this.sizes = new AtomicIntegerArray(lockCount);
    for (int stripe=0; stripe<lockCount; stripe++) 
      this.locks[stripe] = new Object();
  }

  @SuppressWarnings("unchecked") 
  private static <K,V> ItemNode<K,V>[] makeBuckets(int size) {
    // Java's @$#@?!! type system requires "unsafe" cast here:
    return (ItemNode<K,V>[])new ItemNode[size];
  }

  // Protect against poor hash functions and make non-negative
  private static <K> int getHash(K k) {
    final int kh = k.hashCode();
    return (kh ^ (kh >>> 16)) & 0x7FFFFFFF;  
  }

  // Return true if key k is in map, else false
  public boolean containsKey(K k) {
    final ItemNode<K,V>[] bs = buckets;
    final int h = getHash(k), stripe = h % lockCount, hash = h % bs.length;
    // The sizes access is necessary for visibility of bs elements
    return sizes.get(stripe) != 0 && ItemNode.search(bs[hash], k, null);
  }

  // Return value v associated with key k, or null
  public V get(K k) {
    final int h = getHash(k), hash = h % buckets.length;
    V value = null;
    var holder = new Holder<V>();
    synchronized (locks[hash % lockCount]) {
      StripedWriteMap.ItemNode.search(buckets[hash], k, holder);
    }
    value = holder.get();
    if (value != null)
      return value;
    else
      return null;
  }

  public int size() {
    int result = 0;
    for (int i = 0; i<lockCount; i++){
      result+=sizes.get(i);
    }
    return result;
  }

  // Put v at key k, or update if already present.  The logic here has
  // become more contorted because we must not hold the stripe lock
  // when calling reallocateBuckets, otherwise there will be deadlock
  // when two threads working on different stripes try to reallocate
  // at the same time.
  public V put(K k, V v) {
    final int h = getHash(k), stripe = h % lockCount;
    final Holder<V> old = new Holder<V>();
    ItemNode<K,V>[] bs;
    int afterSize; 
    synchronized (locks[stripe]) {
      bs = buckets;
      final int hash = h % bs.length;
      final ItemNode<K,V> node = bs[hash], 
        newNode = ItemNode.delete(node, k, old);
      bs[hash] = new ItemNode<K,V>(k, v, newNode);
      // Write for visibility; increment if k was not already in map
      afterSize = sizes.addAndGet(stripe, newNode == node ? 1 : 0);
    }
    if (afterSize * lockCount > bs.length)
      reallocateBuckets(bs);
    return old.get();
  }

  // Put v at key k only if absent.  
  public V putIfAbsent(K k, V v) {
    final int h = getHash(k), hash = h % buckets.length;
    StripedWriteMap.ItemNode<K, V> node = null;
    final Holder<V> old = new Holder<V>();
    int newSize;
    synchronized (locks[hash % lockCount]) {
      StripedWriteMap.ItemNode.search(buckets[hash], k, old);
      V val = old.get();
      if (val != null)
        return val;     //don't have to update size
      else {
        buckets[hash] = new StripedWriteMap.ItemNode<K,V>(k, v, buckets[hash]);
        newSize = sizes.incrementAndGet(hash % lockCount);
      }
    }

    if (newSize * lockCount > buckets.length)
      reallocateBuckets();
    return null;
  }

  // Remove and return the value at key k if any, else return null
  public V remove(K k) {
    final int h = getHash(k), hash = h % buckets.length;
    synchronized (locks[hash % lockCount]){

      StripedWriteMap.ItemNode<K,V> prev = buckets[hash];
      if (prev == null)
        return null;
      else {
        Holder<V> old = new Holder<V>();
        var newBucket = ItemNode.delete(prev, k, old);
        if(old.get() != null){
          buckets[hash] = newBucket;
          sizes.decrementAndGet(hash % lockCount);
          return old.get();
        }
        return null;
      }
    }
  }

  // Iterate over the hashmap's entries one stripe at a time.  
  public void forEach(BiConsumer<K,V> biConsumer) {
    int bucketLenght = buckets.length;
    for(int stripe=0; stripe<lockCount; stripe++){       //for each stripe
      synchronized (locks[stripe]){
        for(int i=stripe; i<bucketLenght; i+=lockCount){    //if: 3=locks   0,3,6,9,12,13
          ItemNode<K,V> node = buckets[i];
          while (node != null){
             biConsumer.accept(node.k, node.v);
             node=node.next;
          }
        }
      }
    }
  }

  // Now that reallocation happens internally, do not do it externally
  public void reallocateBuckets() { }

  // First lock all stripes.  Then double bucket table size, rehash,
  // and redistribute entries.  Since the number of stripes does not
  // change, and since buckets.length is a multiple of lockCount, a
  // key that belongs to stripe s because (getHash(k) % N) %
  // lockCount == s will continue to belong to stripe s.  Hence the
  // sizes array need not be recomputed.  

  // In any case, do not reallocate if the buckets field was updated
  // since the need for reallocation was discovered; this means that
  // another thread has already reallocated.  This happens very often
  // with 16 threads and a largish buckets table, size > 10,000.

  public void reallocateBuckets(final ItemNode<K,V>[] oldBuckets) {
    lockAllAndThen(() -> { 
        final ItemNode<K,V>[] bs = buckets;
        if (oldBuckets == bs) {
          // System.out.printf("Reallocating from %d buckets%n", bs.length);
          final ItemNode<K,V>[] newBuckets = makeBuckets(2 * bs.length);
          for (int hash=0; hash<bs.length; hash++) {
            ItemNode<K,V> node = bs[hash];
            while (node != null) {
              final int newHash = getHash(node.k) % newBuckets.length;
              newBuckets[newHash] 
                = new ItemNode<K,V>(node.k, node.v, newBuckets[newHash]);
              node = node.next;
            }
          }
          buckets = newBuckets; // Visibility: buckets field is volatile
        } 
      });
  }
  
  // Lock all stripes, perform action, then unlock all stripes
  private void lockAllAndThen(Runnable action) {
    lockAllAndThen(0, action);
  }

  private void lockAllAndThen(int nextStripe, Runnable action) {
    if (nextStripe >= lockCount)
      action.run();
    else 
      synchronized (locks[nextStripe]) {
        lockAllAndThen(nextStripe + 1, action);
      }
  }

  static class ItemNode<K,V> {
    private final K k;
    private final V v;
    private final ItemNode<K,V> next;
    
    public ItemNode(K k, V v, ItemNode<K,V> next) {
      this.k = k;
      this.v = v;
      this.next = next;
    }

    // These work on immutable data only, no synchronization needed.

    public static <K,V> boolean search(ItemNode<K,V> node, K k, Holder<V> old) {
      while (node != null) 
        if (k.equals(node.k)) {
          if (old != null) 
            old.set(node.v);
          return true;
        } else 
          node = node.next;
      return false;
    }
    
    public static <K,V> ItemNode<K,V> delete(ItemNode<K,V> node, K k, Holder<V> old) {
      if (node == null) 
        return null; 
      else if (k.equals(node.k)) {
        old.set(node.v);
        return node.next;
      } else {
        final ItemNode<K,V> newNode = delete(node.next, k, old);
        if (newNode == node.next) 
          return node;
        else 
          return new ItemNode<K,V>(node.k, node.v, newNode);
      }
    }
  }
  
  // Object to hold a "by reference" parameter.  For use only on a
  // single thread, so no need for "volatile" or synchronization.

  static class Holder<V> {
    private V value;
    public V get() { 
      return value; 
    }
    public void set(V value) { 
      this.value = value;
    }
  }
}

// ----------------------------------------------------------------------
// A wrapper around the Java class library's sophisticated
// ConcurrentHashMap<K,V>, making it implement OurMap<K,V>

class WrapConcurrentHashMap<K,V> implements OurMap<K,V> {
  final ConcurrentHashMap<K,V> underlying = new ConcurrentHashMap<K,V>();

  public boolean containsKey(K k) {
    return underlying.containsKey(k);
  }

  public V get(K k) {
    return underlying.get(k);
  }

  public V put(K k, V v) {
    return underlying.put(k, v);
  }

  public V putIfAbsent(K k, V v) {
    return underlying.putIfAbsent(k, v);
  }
  
  public V remove(K k) {
    return underlying.remove(k);
  }

  public int size() {
    return underlying.size();
  }
  
  public void forEach(BiConsumer<K,V> biConsumer) {
    underlying.forEach((k,v) -> biConsumer.accept(k,v));
  }

  public void reallocateBuckets() { }
}
