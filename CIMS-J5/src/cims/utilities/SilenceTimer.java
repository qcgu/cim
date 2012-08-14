/**
 * 
 */
package cims.utilities;
import java.util.*;

import cims.analysers.*;

/**
 * @author andrew
 *
 */
public class SilenceTimer {
	private AnalyseMidi_Silence analyser;
	private Timer silenceTimer;
	private long delay;
	/**
	 * 
	 */
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
			//if(this.am.segmentStarted) {
			am.silent();
		}
		
	}

}
