package cims.generators;

//import cims.supervisors.SupervisorMidi;
import cims.utilities.RepeatTimer;

import static cims.supervisors.SupervisorMidi_Globals.sRepeatInterval;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;

public class GenerateMidi_Loop {
	private RepeatTimer repeatTimer;
	private int interval;
	private GenerateMidi_Segment gm_segment;
	public boolean hasStarted = false;
	
	public GenerateMidi_Loop(GenerateMidi_Segment segment) {

		this.repeatTimer = new RepeatTimer(this);
		this.gm_segment = segment;
		this.interval = sRepeatInterval;
	}
	
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public void start(){
		this.play();
		if (interval==0) {
			//Calculate the length of the segement
			//interval = segmentLength;
		}
		this.repeatTimer.start(interval);
		this.hasStarted = true;
	}
	
	public void stop() {
		this.repeatTimer.cancel();
		this.hasStarted = false;
	}

	public void play() {
		this.gm_segment.generate(sNextPlay);
	}
}
