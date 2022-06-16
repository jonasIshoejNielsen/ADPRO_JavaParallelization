

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntToDoubleFunction;

public class ExecutorAccountExperiments {

  static final int N = 10; // Number of accounts
  static final int NO_TRANSACTION=5;
  
  static final Account[] accounts = new Account[N];
  static final Random rnd = new Random();
  
  public static void main(String[] args){
    // Create empty accounts
    for( int i = 0; i < N; i++){
      accounts[i] = new Account(i);
      System.out.print(",   "+i+": " + accounts[i].balance);
    }
    System.out.println();

    Timer t = new Timer();
    doNTransactions(NO_TRANSACTION);
    System.out.println("took :" + t.check() + " ns");

    for( int i = 0; i < N; i++){
      System.out.print(",   "+i+": " + accounts[i].balance);
    }
    System.out.println();
  }
  
  private static void doNTransactions(int noTransactions){
    /*//5.2
    Thread t1 = new Thread( () -> {
      for(int i = 0; i<(int)noTransactions/2; i++){
        long amount = rnd.nextInt(5000)+100; // Just a random possitive amount
        int source = rnd.nextInt(N);
        int target = (source + rnd.nextInt(N-2)+1) % N; // make sure target <> source
        doTransaction( new Transaction( amount, accounts[source], accounts[target]));
      }
    });
    Thread t2 = new Thread( () -> {
      for(int i = (int)noTransactions/2; i<noTransactions; i++){
        long amount = rnd.nextInt(5000)+100; // Just a random possitive amount
        int source = rnd.nextInt(N);
        int target = (source + rnd.nextInt(N-2)+1) % N; // make sure target <> source
        doTransaction( new Transaction( amount, accounts[source], accounts[target]));
      }
    });
    t1.start(); t2.start();
    try {
      t1.join();
      t2.join();
    } catch (InterruptedException exn) { }
    */
    //5.4
    int N_CPUS = Runtime.getRuntime().availableProcessors();
    System.out.println("N_CPUS = "+ N_CPUS);
    for (int i = 50; i>=0; i-=10){
      if (i==0) i=1;
      int wc = (int)((double)i / 0.4);
      int NTHREADS = N_CPUS *1 * (1+wc);
      System.out.println("NTHREADS "+i+" = "+NTHREADS);
    }
    int wc = (int)(50.0/0.4);
    int NTHREADS = N_CPUS *1 * (1+wc);
    System.out.println("NTHREADS = "+NTHREADS);
    final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
    for(int i = 0; i<noTransactions; i++){
      Runnable task = () -> {
        long amount = rnd.nextInt(5000)+100; // Just a random possitive amount
        int source = rnd.nextInt(N);
        int target = (source + rnd.nextInt(N-2)+1) % N; // make sure target <> source
        doTransaction( new Transaction( amount, accounts[source], accounts[target]));
      };
      exec.execute(task);
    }
    exec.shutdown();
    while (!exec.isTerminated()) {}
    System.out.println("done");
  }
  
  private static void doTransaction(Transaction t){
    System.out.println(t);
    //Mark7("transfer", i -> t.transfer2());
    t.transfer();
  }

  
  static class Transaction {
    final Account source, target;
    final long amount;
    private static final Object tieLock = new Object();
    Transaction(long amount, Account source, Account target){
      this.amount = amount;
      this.source = source;
      this.target = target;
    }
    public long transfer2(){
      transfer();
      return amount;
    }
    public void transfer(){
      if (source.id < target.id){
        synchronized (source){
          synchronized (target){
            source.withdraw(amount);
            try{Thread.sleep(50);} catch(Exception e){}; // Simulate transaction time
            target.deposit(amount);
          }
        }
      }
      else if (source.id > target.id){
        synchronized (target){
          synchronized (source){
            source.withdraw(amount);
            try{Thread.sleep(50);} catch(Exception e){}; // Simulate transaction time
            target.deposit(amount);
          }
        }
      }
      else{
        synchronized (tieLock){
          synchronized (source){
            synchronized (target){
              source.withdraw(amount);
              try{Thread.sleep(50);} catch(Exception e){}; // Simulate transaction time
              target.deposit(amount);
            }
          }
        }
      }
    }
    
    public String toString(){
      return "Transfer " + amount + " from " + source.id + " to " + target.id;
    }
  }

  static class Account{
    // should have transaction history, owners, account-type, and 100 other real things
    public final int id;
    private long balance = 0;
    Account( int id ){ this.id = id;}
    public void deposit(long sum){ balance += sum; } 
    public void withdraw(long sum){ balance -= sum; }
    public long getBalance(){ return balance; }
  }

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
        double time = runningTime * 1e9 / count;
        st += time;
        sst += time * time;
        totalCount += count;
      }
    } while (runningTime < 0.25 && count < Integer.MAX_VALUE/2);
    double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    System.out.printf("%-25s %15.1f ns %10.2f %10d%n", msg, mean, sdev, count);
    return dummy / totalCount;
  }

}
