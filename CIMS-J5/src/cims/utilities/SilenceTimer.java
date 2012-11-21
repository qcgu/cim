package cims.utilities;

import java.util.Timer;
import java.util.TimerTask;

import cims.analysers.AnalyseMidi_Silence;

/*****************************************************************************************
 * Provides a general Java Timer for detecting silence. When timer expires, the run
 * method of the inner TimerTask class calls the silent method of AnalyseMidi_Silence.
 * 
 * @author Andrew Gibson a.gibson@griffith.edu.au
 *
 */
public class SilenceTimer {
	private AnalyseMidi_Silence analyser;
	private Timer silenceTimer;
	private long delay;

	public SilenceTimer(AnalyseMidi_Silence newAnalyser) {
		analyser = newAnalyser;
		this.silenceTimer = new Timer(); // initial timer
	}
		
	public void start(int delay) {
			this.cancel();
			this.silenceTimer = new Timer();
			this.delay = delay;		
			this.silenceTimer.schedule(new Timeout(this.analyser), this.delay);
	}
	public void cancel() {
		if(silenceTimer!=null) {
			this.silenceTimer.cancel();
		}
	}
		
	private class Timeout extends TimerTask {
		private AnalyseMidi_Silence am;

		public Timeout(AnalyseMidi_Silence am) {
			this.am = am;
		}

		@Override
		public void run() {
			am.silent();
		}
		
	}

}
