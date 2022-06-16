import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

class PicoBankTest {
  private static final int N = 10; // Number of bank accounts in bank
  public static void main(String[] args){
    //testBankSequential(new PicoBankBasic( N ));

    //12.1.1
    //testBankParallel(new PicoBankBasic( N ), 10);

    //12.1.2
    //testBankParallel(new PicoBankSyncronised( N ), 10);

    //12.1.4
    //testBankParallel(new PicoBankLock( N ), 10);

    //12.1.5
    //testBankParallel(new PicoBankAtomic( N ), 10);

    //12.1.6
    //testBankParallel(new PicoBankAtomicAlt( N ), 10000);

  }
  
  private static void testBankSequential(PicoBank bank){
    long start = System.nanoTime();
    for (int i=0; i< 10_000; i++)
      doRandomTransfer(bank);
    long time = System.nanoTime() - start;
    long sum = 0L;
    for (int i=0; i<N; i++) sum += bank.balance(i);
    System.out.println("Single thread test: " + (sum == 0 ? "SUCCESS" : "FAILURE"));
    System.out.printf("Single thread time: %,dns\n", time );
  }

  private static void testBankParallel(PicoBank bank, int numThreads){
    CyclicBarrier barrier = new CyclicBarrier(numThreads + 1);
    long start = System.nanoTime();
    for(int i = 0; i < numThreads; i++) {
      new Thread(() -> {
        try {
          barrier.await();
          for (int j = 0; j < 10_000/numThreads; j++)
            doRandomTransfer(bank);
          barrier.await();
        } catch (Exception e) { }
      }).start();
    }

    try {
      barrier.await();  //wait for start
      barrier.await();  //wait for end
    } catch (Exception e) {}
    long time = System.nanoTime() - start;
    long sum = 0L;
    for (int i=0; i<N; i++) sum += bank.balance(i);
    System.out.println("Parallel thread test: " + (sum == 0 ? "SUCCESS" : "FAILURE"));
    System.out.printf("Parallel thread time: %,dns\n", time );
  }
    

  static final Random rnd = new Random(); // replace this with efficient Random
  public static void doRandomTransfer(PicoBank bank){
    long amount = rnd.nextInt(5000)+100; // Just a random possitive amount
    int source = rnd.nextInt(N);
    int target = (source + rnd.nextInt(N-2)+1) % N; // make sure target <> source
    bank.transfer(amount, source, target);
  }
}

////////////////////
//PicoBank interface
////////////////////

interface PicoBank {
  void transfer(long amount, int source, int target);
  long balance(int accountNr);
}

/////////////////////
//PicoBankBasic class
/////////////////////

class PicoBankBasic implements PicoBank{
  final int N; // Number of accounts
  final Account[] accounts ;
  
  PicoBankBasic(int noAccounts){
    N = noAccounts;
    accounts = new Account[N];
    for( int i = 0; i < N; i++){
      accounts[i] = new Account(i);
    }
  }


  
  public void transfer(long amount, int source, int target){
      accounts[source].withdraw(amount);
      accounts[target].deposit(amount);
  }
  public long balance(int accountNr){
    return accounts[accountNr].getBalance();
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
}

class PicoBankSyncronised implements PicoBank{
  final int N; // Number of accounts
  final Account[] accounts ;

  PicoBankSyncronised(int noAccounts){
    N = noAccounts;
    accounts = new Account[N];
    for( int i = 0; i < N; i++){
      accounts[i] = new Account(i);
    }
  }



  public synchronized void transfer(long amount, int source, int target){
    accounts[source].withdraw(amount);
    accounts[target].deposit(amount);
  }
  public long balance(int accountNr){
    return accounts[accountNr].getBalance();
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
}

class PicoBankLock implements PicoBank{
  final int N; // Number of accounts
  final Account[] accounts ;
  final Object[] locks;

  PicoBankLock(int noAccounts){
    N = noAccounts;
    accounts = new Account[N];
    for( int i = 0; i < N; i++){
      accounts[i] = new Account(i);
    }

    locks = new Object[N];
    for( int i = 0; i < N; i++){
      locks[i] = new Object();
    }
  }



  public synchronized void transfer(long amount, int source, int target){
    //No tie lock since account numbers are always unique
    if(source < target){
      synchronized (locks[source]) {
        synchronized (locks[target]){
          accounts[source].withdraw(amount);
          accounts[target].deposit(amount);
        }
      }
    } else {
      synchronized (locks[target]) {
        synchronized (locks[source]){
          accounts[source].withdraw(amount);
          accounts[target].deposit(amount);
        }
      }
    }
  }

  public long balance(int accountNr){
    return accounts[accountNr].getBalance();
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
}

class PicoBankAtomic implements PicoBank{
  final int N; // Number of accounts
  final Account[] accounts ;

  PicoBankAtomic(int noAccounts){
    N = noAccounts;
    accounts = new Account[N];
    for( int i = 0; i < N; i++){
      accounts[i] = new Account(i);
    }
  }



  public void transfer(long amount, int source, int target){
    accounts[source].withdraw(amount);
    accounts[target].deposit(amount);
  }
  public long balance(int accountNr){
    return accounts[accountNr].getBalance();
  }

  static class Account{
    // should have transaction history, owners, account-type, and 100 other real things
    public final int id;
    private AtomicLong balance = new AtomicLong(0);
    Account( int id ){ this.id = id;}
    public void deposit(long sum){ balance.addAndGet(sum); }
    public void withdraw(long sum){ balance.addAndGet(-sum); }
    public long getBalance(){ return balance.get(); }
  }
}

class PicoBankAtomicAlt implements PicoBank{
  final int N; // Number of accounts
  final Account[] accounts ;

  PicoBankAtomicAlt(int noAccounts){
    N = noAccounts;
    accounts = new Account[N];
    for( int i = 0; i < N; i++){
      accounts[i] = new Account(i);
    }
  }



  public void transfer(long amount, int source, int target){
    accounts[source].withdraw(amount);
    accounts[target].deposit(amount);
  }
  public long balance(int accountNr){
    return accounts[accountNr].getBalance();
  }

  static class Account{
    // should have transaction history, owners, account-type, and 100 other real things
    public final int id;
    private AtomicLong balance = new AtomicLong(0);
    Account( int id ){ this.id = id;}
    public void deposit(long sum){
      long tempBalance = 0;
      do{
        tempBalance = balance.get();
      }
      while(!balance.compareAndSet(tempBalance,tempBalance+sum));}
    public void withdraw(long sum){
        long tempBalance = 0;
        do{
          tempBalance = balance.get();
        }
        while(!balance.compareAndSet(tempBalance,tempBalance-sum));}
    public long getBalance(){ return balance.get(); }
  }
}

