import io.reactivex.internal.operators.observable.ObservableUsing;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class StopWatchMessagePassing {

    public static void main(String[] args) {
        MakeAStopwatches(14);
    }
    public static void MakeAStopwatches(int n){
        JFrame f=new JFrame("Stopwatch");
        f.setBounds(0, 0, 220+(stopwatchUI.spaceBetween*(n-1)), 220);
        StopwatchMessageBuffer<Message> buffer = new StopwatchMessageBuffer<Message>();
        stopwatchUI myUI= new stopwatchUI(0, f, n, buffer);

        f.setLayout(null);
        f.setVisible(true);
        for (int i = 0; i <n ; i++) {
            final int j = i;
            Thread t= new Thread() {
                private int seconds= 0;
                // Background Thread simulation a clock ticking every 1 seconde
                @Override
                public void run() {
                    int temp= 0;
                    try {
                        while ( true ) {
                            TimeUnit.MILLISECONDS.sleep(100);
                            buffer.sendMessage(new Message(10, j));
                        }
                    } catch (java.lang.InterruptedException e) {
                        System.out.println(e.toString());
                    }
                }
            };
            t.start();
        }
    }

    public static class Message{
        int divivde;
        int index;
        public Message(int divivde, int index) {
            this.divivde = divivde;
            this.index = index;
        }
    }
}
