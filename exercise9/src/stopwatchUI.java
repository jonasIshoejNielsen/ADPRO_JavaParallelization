import io.reactivex.*;
import io.reactivex.disposables.Disposable;

import java.awt.event.*;
import javax.swing.*; 
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import io.reactivex.schedulers.Schedulers;

class stopwatchUI {
		final private String allzero = "0:00:00:0";
		private int lx;
		private static JFrame lf;
		private SecCounter[] lC;
		
		private JButton[] startButton;
		private JButton[] stopButton;
		private JButton[] resetButton;
		private JTextField[] tf;
		public static int spaceBetween = 100;
		
		public void updateTime(int divdeTime, int index){
		  if ( lC[index].incr() ) {
			int seconds= lC[index].seconds;
			int ds = (seconds%divdeTime);
			seconds /=divdeTime;
			int hours= seconds/3600;
			int minutes= (seconds%3600)/60;
			int secs= seconds%60;
			String time= String.format(Locale.getDefault(),	"%d:%02d:%02d:%01d", hours, minutes, secs, ds);
			tf[index].setText(time);
		  }
		};

    public boolean running(int index) { return lC[index].running();  }
		
		public stopwatchUI(int x, JFrame jF, int nStopWatches){
			initialize(x, jF, nStopWatches);
		}

		public stopwatchUI(int x, JFrame jF, int nStopWatches, StopwatchMessageBuffer<StopWatchMessagePassing.Message> buffer){
			initialize(x, jF, nStopWatches);

			Thread t= new Thread() {
				@Override
				public void run() {
					while ( true ) {
						StopWatchMessagePassing.Message temp = (StopWatchMessagePassing.Message)buffer.receiveMessage();
						updateTime(temp.divivde, temp.index);

					}
				}
			};
			t.start();
		}

		public void initialize(int x, JFrame jF, int nStopWatches){
			lx= x+50; lf= jF;

			lC 			= new SecCounter[nStopWatches];
			startButton = new JButton[nStopWatches];
			stopButton 	= new JButton[nStopWatches];
			resetButton = new JButton[nStopWatches];
			tf 			= new JTextField[nStopWatches];

			for (int i = 0; i <nStopWatches ; i++) {
				final int j = i;
				tf[i] = new JTextField();
				tf[i].setBounds(lx + (spaceBetween * i), 10, 60, 20);
				tf[i].setText(allzero);
				lC[i] = new SecCounter(0, false);
				startButton[i] = new JButton("Start");
				startButton[i].setBounds(lx + (spaceBetween * i), 50, 95, 25);
				Observable<Integer> rxStart = createObservable(startButton[j], j);
				rxStart.subscribe(k-> lC[k].setRunning(true));


				stopButton[i] = new JButton("Stop");
				stopButton[i].setBounds(lx + (spaceBetween * i), 90, 95, 25);
				Observable<Integer> rxStop = createObservable(stopButton[j], j);
				rxStop.subscribe(k-> lC[k].setRunning(false));

				resetButton[i] = new JButton("Reset");
				resetButton[i].setBounds(lx + (spaceBetween * i), 130, 95, 25);

				Observable<Integer> rxReset = createObservable(resetButton[j], j);
				rxReset.subscribe(k-> {
					lC[j] = new SecCounter(0, false);
					tf[j].setText(allzero);
				});

				lf.add(tf[i]);
				lf.add(startButton[i]);
				lf.add(stopButton[i]);
				lf.add(resetButton[i]);
			}
		}
		private static Observable<Integer> createObservable(JButton button, final int j){
    		return Observable.create(new ObservableOnSubscribe<Integer>() {
				@Override
				public void subscribe(ObservableEmitter<Integer> e) throws Exception {
					button.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent ee){
							e.onNext( j);
						}
					});
				}
			})
					.subscribeOn(Schedulers.newThread());
		}
}