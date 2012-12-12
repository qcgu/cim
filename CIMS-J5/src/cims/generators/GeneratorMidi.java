package cims.generators;

import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;

public abstract class GeneratorMidi implements Runnable {
	
	private int activityType = 0;
	private MidiMessage mirror_message;
	private MidiMessage first_message;
	
	
	public static final int SILENCE = 0;
	public static final int REPEAT = 1;
	public static final int MIRROR = 2;
	public static final int SUPPORT = 3;
	public static final int INITIATE = 4;

	public GeneratorMidi(SupervisorMidi supervisor) {
		mirror_message = new MidiMessage();
	}
	
	public void run() {
		switch(activityType) {
		case SILENCE:
			this.silence();
			break;
		case REPEAT:
			this.repeat();
			break;
		case MIRROR:
			this.mirror();
			break;
		case SUPPORT:
			this.support();
			break;
		case INITIATE:
			this.initiate();
			break;
		}
	}

	public abstract void silence();
	public abstract void repeat();
	public abstract void mirror();
	public abstract void support();
	public abstract void supportStop();
	public abstract void initiate();
	public abstract void initiateStop();
	
	public abstract boolean supportHasStarted();
	
	public int getActivityType() {
		return activityType;
	}

	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}
	
	

	public MidiMessage getMirror_message() {
		return mirror_message;
	}

	public void setMirror_message(MidiMessage mirror_message) {
		this.mirror_message = mirror_message;
	}

	public MidiMessage getFirst_message() {
		return first_message;
	}

	public void setFirst_message(MidiMessage first_message) {
		this.first_message = first_message;
	}
	
	

}
