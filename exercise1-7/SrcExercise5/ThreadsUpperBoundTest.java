

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadsUpperBoundTest{
    private static Object s = new Object();
    private static int count = 0;
    public static void main(String[] argv){
        while (true){
            new Thread(new Runnable(){
                public void run(){
                    synchronized(s){
                        count += 1;
                        System.err.println("New thread #"+count);
                    }
                    while (true){
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e){
                            System.err.println(e);
                        }
                    }
                }
            }).start();
        }
    }
}

