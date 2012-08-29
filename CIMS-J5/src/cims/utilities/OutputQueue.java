package cims.utilities;

import java.util.*;

import cims.datatypes.*;
import cims.generators.*;

import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeat;
import static cims.supervisors.SupervisorMidi_Globals.sBeatList;
import static cims.supervisors.SupervisorMidi_Globals.sTimeBetweenBeats;

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
		this.midiGen = newMidiGen;
		
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
				int currentBeat = sCurrentBeat;
				long currentBeatTime = sBeatList[currentBeat];
				long elapsedTime = System.currentTimeMillis() - currentBeatTime;
				long timeToWait = sTimeBetweenBeats;
				if(startOnNextBar) {
					long barElapsed = (currentBeat - 1) * timeToWait;
					timeToWait = (timeToWait * sBeatList[0]) - barElapsed;
				}
				timeToWait = timeToWait - elapsedTime;
				delay = delay + timeToWait;
				if (delay<0) delay=0;
			}
			//System.out.println("DELAY IS "+delay);
			segmentTimer = new Timer();
			segmentTimer.schedule(new Player(this.midiGen,midimessage), delay);
			//segmentTimer.cancel();
		}
	}
		
	public void cancel() {
		if(segmentTimer!=null) {
			this.segmentTimer.cancel();
		}
	}
	
	private class Player extends TimerTask {
		private GenerateMidi_Segment gm;
		private MidiMessage outputMessage;
		public Player(GenerateMidi_Segment gm, MidiMessage midimessage) {
			//System.out.println("New Player");
			this.gm = gm;
			this.outputMessage = midimessage;
		}

		@Override
		public void run() {
			//System.out.println("RUN called "+outputMessage.status);
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
