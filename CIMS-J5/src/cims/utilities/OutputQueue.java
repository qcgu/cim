/**
 * 
 */
package cims.utilities;
import java.util.*;

import cims.datatypes.*;
import cims.generators.*;
import cims.supervisors.SupervisorMidi;

/**
 * @author andrew
 *
 */
public class OutputQueue {
	private GenerateMidi_Segment midiGen;
	private volatile MidiSegment segmentToPlay;
	private Timer segmentTimer;
	
	private boolean startOnNextBeat = false;
	private boolean startOnNextBar = false;
	/**
	 * 
	 */
	public OutputQueue(GenerateMidi_Segment newMidiGen) {
		midiGen = newMidiGen;
		
	}
	
	public synchronized void addSegment(MidiSegment segment) {
		this.segmentToPlay = segment;
	}
	
	public void play() {
		//Iterate segment and play
		Iterator<MidiMessage> segmentIterator = segmentToPlay.asList().iterator();
		boolean firstEvent = true;
		long startTime = 0;
		long delay = 0;
		while (segmentIterator.hasNext()) {
			MidiMessage midimessage = new MidiMessage();
			midimessage.copy(segmentIterator.next());
			if(firstEvent) {
				startTime = midimessage.timeMillis;
				firstEvent = false;
			}
			delay = midimessage.timeMillis - startTime;
			if (delay<1) delay=1; //allow 1 ms for timer
			if(startOnNextBeat || startOnNextBar) {
				int currentBeat = SupervisorMidi.sCurrentBeat;
				long currentBeatTime = SupervisorMidi.sBeatList[currentBeat];
				long elapsedTime = System.currentTimeMillis() - currentBeatTime;
				long timeToWait = SupervisorMidi.sTimeBetweenBeats;
				if(startOnNextBar) {
					long barElapsed = (currentBeat - 1) * timeToWait;
					timeToWait = (timeToWait * SupervisorMidi.sBeatList[0]) - barElapsed;
				}
				timeToWait = timeToWait - elapsedTime;
				delay = delay + timeToWait;
			}
			segmentTimer = new Timer();
			segmentTimer.schedule(new Player(this.midiGen,midimessage), delay);
		}
	}
	
	private class Player extends TimerTask {
		private GenerateMidi_Segment gm;
		private MidiMessage outputMessage;
		public Player(GenerateMidi_Segment gm, MidiMessage midimessage) {
			this.gm = gm;
			this.outputMessage = midimessage;
		}

		@Override
		public void run() {
			this.gm.output(this.outputMessage);
		}
		
	}
	
	public void startOnNextBeat() {
		this.startOnNextBeat = true;
		this.startOnNextBar = false;
	}
	
	public void startOnNextBar() {
		this.startOnNextBeat = false;
		this.startOnNextBar = true;
	}
	
	public void startOnPlay() {
		this.startOnNextBeat = false;
		this.startOnNextBar = false;
	}

}
