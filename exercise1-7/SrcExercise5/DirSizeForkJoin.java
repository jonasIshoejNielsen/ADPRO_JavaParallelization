
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.Callable;

class DirSizeFJ {

  private static class DirSizeTask extends RecursiveTask<Long> {

    private final File file;

    public DirSizeTask(final File file) {
      this.file = Objects.requireNonNull(file);
    }

    @Override
    protected Long compute() {
      if (file.isFile()) {
        return file.length();
      }

      final List<DirSizeTask> tasks = new ArrayList<>();
      final File[] children = file.listFiles();
      if (children != null) {
        for (final File child : children) {
          final DirSizeTask task = new DirSizeTask(child);
          task.fork();
          tasks.add(task);
        }
      }

      long size = 0;
      for (final DirSizeTask task : tasks) {
        size += task.join();
      }

      return size;
    }
  }

  public static long sizeOf(final File file) {
    final ForkJoinPool pool = new ForkJoinPool();
    try {
      return pool.invoke(new DirSizeTask(file));
    } finally {
      pool.shutdown();
    }
  }

}

class DirSizeSeq {
  public static long sizeOf(final File file) {
    long size = 0;

    /* Ignore files which are not files and dirs */
    if (file.isFile()) {
      size = file.length();
    } else {
      final File[] children = file.listFiles();
      if (children != null) {
        for (final File child : children) {
          size += DirSizeSeq.sizeOf(child);
        }
      }
    }
    return size;
  }
}

public class DirSizeForkJoin {
  private static File TESTDIR = new File("/System/Applications"); // On mac - pick your own 
  public static void main(final String[] args) throws Exception {
    
    tester( ()-> DirSizeSeq.sizeOf(TESTDIR));
    tester( ()-> DirSizeFJ.sizeOf(TESTDIR));
  }
  
  private static void tester(Callable<Long> r) throws Exception{
    int N = 10;
    long sum = 0;
    for(int i = 0; i<N; i++){
      final long start = System.nanoTime();
      final long size = r.call();
      final long taken = System.nanoTime() - start;
      sum += taken;
      if (i == 0) System.out.println("Size of: " + TESTDIR + " is " + size);
      System.out.println("Nano seconds to compute: " + taken);
    }
    System.out.println("Average milli seconds to compute: " + (long)(sum/N)/1000000);
  }
}