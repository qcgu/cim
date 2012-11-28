package cims.generators;


import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import cims.utilities.RepeatTimer;

import static cims.supervisors.SupervisorMidi_Globals.sRepeatInterval;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;

public class GenerateMidi_Loop {
	private RepeatTimer repeatTimer;
	private int interval;
	private GenerateMidi_Segment gm_segment;
	public boolean hasStarted = false;
	
	public static final Logger LOGGER = Logger.getLogger(GenerateMidi_Loop.class);
	
	public GenerateMidi_Loop(GenerateMidi_Segment segment) {

		this.repeatTimer = new RepeatTimer(this);
		this.gm_segment = segment;
		this.interval = sRepeatInterval;
		LOGGER.setLevel(Level.INFO);
	}
	
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public void start(){
		LOGGER.debug("start loop");
		this.play();
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
