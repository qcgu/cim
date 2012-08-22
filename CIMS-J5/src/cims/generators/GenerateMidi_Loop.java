package cims.generators;

import cims.supervisors.SupervisorMidi;
import cims.utilities.RepeatTimer;

public class GenerateMidi_Loop {
	private RepeatTimer repeatTimer;
	private int interval;
	private GenerateMidi_Segment gm_segment;
	public GenerateMidi_Loop(GenerateMidi_Segment segment) {
		// TODO Auto-generated constructor stub
		this.repeatTimer = new RepeatTimer(this);
		this.gm_segment = segment;
		this.interval = SupervisorMidi.sRepeatInterval;
	}
	
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public void start(){
		if (interval==0) {
			//Calculate the length of the segement
			//interval = segmentLength;
		}
		this.repeatTimer.start(interval);
	}
	
	public void stop() {
		this.repeatTimer.cancel();
	}

	public void play() {
		this.gm_segment.generate();
	}
}
