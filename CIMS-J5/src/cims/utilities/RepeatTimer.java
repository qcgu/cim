/**
 * SilenceTimer - A Gibson
 */
package cims.utilities;
import java.util.*;

import cims.generators.GenerateMidi_Loop;

/**
 * @author andrew
 *
 */
public class RepeatTimer {
	private GenerateMidi_Loop loop;
	private Timer repTimer;
	private long interval;
	private long startDelay;
	/**
	 * 
	 */
	public RepeatTimer(GenerateMidi_Loop newLoop) {
		this.loop = newLoop;
		this.repTimer = new Timer(); // initial timer
	}
		
	public void start(int interval) {
		this.repTimer = new Timer();
		this.interval = interval;
		this.startDelay = interval;
		this.repTimer.scheduleAtFixedRate(new Repeater(this.loop), this.startDelay,this.interval);
	}
	
	public void cancel() {
		if(repTimer!=null) {
			this.repTimer.cancel();
		}
	}
		
	private class Repeater extends TimerTask {
		private GenerateMidi_Loop gm_loop;

		public Repeater(GenerateMidi_Loop loop) {
			this.gm_loop = loop;
		}

		@Override
		public void run() {
			//if(this.am.segmentStarted) {
			gm_loop.play();
		}
		
	}

}
