package cims.deciders;

import cims.datatypes.BeatTime;
import cims.datatypes.MidiControlMessage;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.interfaces.Interface_Controls;
import cims.supervisors.SupervisorMidi;

public abstract class DeciderMidi implements Runnable {
	
	protected SupervisorMidi supervisor;
	private long sleepTime = 0;
	
	public DeciderMidi(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
	}
	
	public void run() {
		try {
	        while (!Thread.interrupted()) {
	            this.deciderLoop();
	            Thread.sleep(this.sleepTime);
	        }
	    } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
	        // Tidy up before completion    	
	    }
	}
	
	/***
	 * Main decision loop - whatever has to run continuously should happen here
	 */
	public abstract void deciderLoop();
	
	public void setLoopSleepTime(long time) {
		this.sleepTime = time;
	}
	
	
	/***
	 * Receive new Message methods - these are called any time a new message comes in
	 * @param 
	 */
	public abstract void firstMidiMessage(MidiMessage message);
	public abstract void newMidiMessage(MidiMessage message);
	public abstract void newMidiSegment(MidiSegment segment);
	public abstract void newSegmentBoundaryTimecode(Long timecode);
	public abstract void newBeatTime(BeatTime beatTime);
	public abstract void newMidiControlMessage(MidiControlMessage controlMessage);
	public abstract void newInterfaceUpdate(Interface_Controls controls);
	
	/***
	 * Send Message methods - call these methods to send the appropriate message
	 */
	
	public abstract void playMidiMessage(MidiMessage message);
	public abstract void playMidiSegment(MidiSegment segment);
	public abstract void scheduleBeatTimeMidiMessage(MidiMessage message);
	public abstract void scheduleBeatTimeMidiSegment(MidiSegment segment);	
	public abstract void sendMessageToInterface(String message);

}
