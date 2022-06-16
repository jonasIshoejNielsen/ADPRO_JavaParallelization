// Week 5
// Show JIT compilation timings
// kasper@itu.dk * 2020-09-24

import java.util.function.IntToDoubleFunction;
import java.util.concurrent.atomic.AtomicLong;

public class MeasureJIT {
  static final String fs = " - n: %d bestTime: %,d thisTime: %,d\n";
  public static void main(String[] args) {
    SystemInfo();
    boolean printNext = false;
    long bestTime = Long.MAX_VALUE;
    for(long n=1; n<100_000; n++){
      long start = System.nanoTime();
      for (long i = 0; i<n; i++)
        incr1000(i);
      long time = (long)(System.nanoTime()-start)/n;
      
      if (printNext){
        System.out.format("NEXT"+fs, n, bestTime, time);
        printNext = false;
      }
      if (time > bestTime*3){ // Outlier - maybe JIT
        System.out.format("SLOW"+fs, n, bestTime, time);
        printNext = true;
      }
      if (time < bestTime) bestTime = time;
      if (time == 0) {
        System.out.format("LAST"+fs, n, bestTime, time);
        return;
      }
    }
  }
  
  private static long incr1000(long n){
    long sum = 0;
    for (int i = 0; i<1000; i++) 
      sum += incr10(i);
    return sum;
  }
  private static long incr10(long n){
    long cnt = 0;
    for (int i = 0; i<10; i++) 
      cnt++;
    return cnt;
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

