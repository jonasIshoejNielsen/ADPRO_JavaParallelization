import java.awt.event.*;  
import javax.swing.*; 
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/* This example is inspired by the StopWatch app in Head First. Android Development
http://shop.oreilly.com/product/0636920029045.do

Modified to Java, October 2020 by Jørgen Staunstrup, ITU, jst@itu.dk */

public class Stopwatch {

	public static void main(String[] args) {
		MakeAStopwatches(1);
	}
	public static void MakeAStopwatches(int n){
		JFrame f=new JFrame("Stopwatch");
		f.setBounds(0, 0, 220+(stopwatchUI.spaceBetween*(n-1)), 220);
		stopwatchUI myUI= new stopwatchUI(0, f, n);

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
							myUI.updateTime(10, j);
						}
					} catch (java.lang.InterruptedException e) {
						System.out.println(e.toString());
					}
				}
			};
			t.start();
		}
	}
}

class Stopwatch2 {
	public static void main(String[] args) {
		Stopwatch.MakeAStopwatches(2);
	}
}
class StopwatchN {
	static int n = 14;
	public static void main(String[] args) {
		Stopwatch.MakeAStopwatches(n);
	}
}