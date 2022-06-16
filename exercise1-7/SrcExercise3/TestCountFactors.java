// For week 2
// sestoft@itu.dk * 2014-08-29

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class TestCountFactors {
  public static void main(String[] args) {
    final int range = 5_000_000;
    //final MyAtomicInteger count = MyAtomicInteger.Create(0);
    final AtomicInteger count = new AtomicInteger(0);
    var startTime = System.nanoTime();


    ConcurrentLinkedQueue<Thread> threads=new ConcurrentLinkedQueue<>();
    int atenthRange =  range/10;
    for(int i=0;i<10; i++){
      final int index = i;
      Thread t1=new Thread(()->{
        for(int p=atenthRange*index; p<atenthRange*(index+1); p++){
          count.addAndGet(countFactors(p));
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
    var endTime = System.nanoTime();
    System.out.println("Took: " + (endTime - startTime));
    System.out.printf("Total number of factors is %9d%n", count.get());

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

class MyAtomicInteger{
  private int value;

  private MyAtomicInteger(int value) {
    this.value = value;
  }
  public static MyAtomicInteger Create(int value){
    var obj = new MyAtomicInteger(value);
    return obj;
  }

  public synchronized int addAndGet(int amount){
      value += amount;
      return value;
  }
  public synchronized int get(){
      return value;
  }
}
