// For week 5
// sestoft@itu.dk * 2014-09-19

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class TestDownload {


  private static final String[] urls = 
  { "http://www.itu.dk", "http://www.di.ku.dk", "http://www.miele.de",
    "http://www.microsoft.com", "http://www.amazon.com", "http://www.dr.dk",
    "http://www.vg.no", "http://www.tv2.dk", "http://www.google.com",
    "http://www.ing.dk", "http://www.dtu.dk", "http://www.eb.dk", 
    "http://www.nytimes.com", "http://www.guardian.co.uk", "http://www.lemonde.fr",   
    "http://www.welt.de", "http://www.dn.se", "http://www.heise.de", "http://www.wsj.com", 
    "http://www.bbc.co.uk", "http://www.dsb.dk", "http://www.bmw.com", "https://www.cia.gov" 
  };

  public static void main(String[] args) throws IOException {
    /*String url = "https://www.wikipedia.org/";
    String page = getPage(url, 10);
    System.out.printf("%-30s%n%s%n", url, page);*/
    int N_CPUS = Runtime.getRuntime().availableProcessors();
    int wc = 50;
    int NTHREADS = N_CPUS *1 * (1+wc);
    ExecutorService[] execs = {
            //Executors.newWorkStealingPool(),
           //Executors.newCachedThreadPool(),
           Executors.newFixedThreadPool(NTHREADS)
    };

    for (var exec : execs) {
      var timer = new Timer();
      var hm = getPagesParallel(urls, 200, Executors.newWorkStealingPool());
      timer.pause();
      System.out.println(timer.check());
    }

    //System.out.println(hm);
  }
  public static HashMap<String, String> getPages (String[] urls, int maxLines) throws IOException {
    var hm = new HashMap<String, String>();
    for (var url : urls) {
      String page = getPage(url, maxLines);
      hm.put(url, page);
    }
    return hm;
  }

  public static Map<String, String> getPagesParallel (String[] urls, int maxLines, ExecutorService exec) throws IOException {

    var hm = new ConcurrentHashMap<String, String>();
    for (var url : urls) {
      // create task
      Runnable task = () -> {
        String page = "";
        try {
          page = getPage(url, maxLines);
        }
        catch (Exception e) {
          System.out.println("fuk yo internet");
        }
        hm.put(url, page);
      };
      exec.execute(task);
    }
    //wait for executor
    exec.shutdown();
    while (!exec.isTerminated()) {}


    return hm;
  }


  public static String getPage(String url, int maxLines) throws IOException {
    // This will close the streams after use (JLS 8 para 14.20.3):
    try (BufferedReader in 
         = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<maxLines; i++) {
        String inputLine = in.readLine();
        if (inputLine == null)
          break;
        else
          sb.append(inputLine).append("\n");
      }
      return sb.toString();
    }
  }
}
