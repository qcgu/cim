package cims.utilities;

import java.util.*;
import cims.datatypes.*;
import cims.generators.*;
import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeat;
import static cims.supervisors.SupervisorMidi_Globals.sBeatList;
import static cims.supervisors.SupervisorMidi_Globals.sTimeBetweenBeats;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;


public class OutputQueue {
	private GenerateMidi_Segment midiGen;
	private volatile MidiSegment segmentToPlay;
	private Timer segmentTimer;
	private ArrayList<Timer> timerList;
	private ArrayList<MidiMessage> noteOnList;
	private boolean startOnNextBeat = false;
	private boolean startOnNextBar = false;
	private long elapsedSinceBeat = 0;
	private long elapsedSinceBar = 0;
	private long pastTheBeatTolerance = sTimeBetweenBeats/64;

	public OutputQueue(GenerateMidi_Segment newMidiGen) {
		this.midiGen = newMidiGen;
		this.timerList = new ArrayList<Timer>();
		this.noteOnList = new ArrayList<MidiMessage>();
	}
	
	public synchronized void addSegment(MidiSegment segment) {
		this.segmentToPlay = segment;
	}
	
	public void play() {
		LOGGER.info("OutputQueue:play()");
		Iterator<MidiMessage> segmentIterator;
		boolean firstEvent = true;
		long segmentStartTime = 0;
		long delay = 0;
		long timeToWait = sTimeBetweenBeats;
		
		if(segmentToPlay==null) {
			LOGGER.warning("segmentToPlay is NULL");
			segmentToPlay = new MidiSegment();
		}
		if(!segmentToPlay.isEmpty()) {
			segmentIterator = segmentToPlay.asList().iterator();
			while (segmentIterator.hasNext()) {
				MidiMessage midimessage = new MidiMessage();
				midimessage.copy(segmentIterator.next());	
				if(midimessage.noteOnOff==MidiMessage.NOTE_ON) {
					noteOnList.add(midimessage);
				}
				if(midimessage.timeMillis<0) {
					LOGGER.warning("midimessage.timeMillis is less than 0: "+midimessage.timeMillis+ " >> now set to 0");
					midimessage.timeMillis = 0;
				}
				if(firstEvent) {
					LOGGER.info("First MidiMessage in queue");
					segmentStartTime = midimessage.timeMillis;
					delay = 0; //Play first message straight away
					firstEvent = false;
				} else {
					delay = midimessage.timeMillis - segmentStartTime;
				}
				this.beatCalcs();
				if(startOnNextBeat) {
					if (elapsedSinceBeat<pastTheBeatTolerance) {
						timeToWait=0;
					} else {
						timeToWait = sTimeBetweenBeats - elapsedSinceBeat;
					}
					LOGGER.warning("BEAT >> DELAY: "+delay+" WAIT: "+timeToWait);
					delay = delay + timeToWait;
				}
				if(startOnNextBar) {
					if (elapsedSinceBar<pastTheBeatTolerance) {
						timeToWait=0;
					} else {
						timeToWait = (sTimeBetweenBeats*sBeatList[0]) - elapsedSinceBar;
					}
					LOGGER.warning("BAR >> DELAY: "+delay+" WAIT: "+timeToWait);
					delay = delay + timeToWait;
				}
				if(delay<0) {
					LOGGER.warning("NEGATIVE DELAY - SET TO 0");
					delay=0;
				}
				LOGGER.info("OutputQueue: pitch = " + midimessage.messageType + " " + midimessage.pitch + " " + midimessage.velocity + " " + midimessage.timeMillis);
				segmentTimer = new Timer();
				timerList.add(segmentTimer);
				segmentTimer.schedule(new Player(this.midiGen,midimessage), midimessage.timeMillis + delay);
			}
		} else {
			LOGGER.warning("NO SEGMENT TO PLAY!! ");
		}
		
	}
		
	public void cancel() {
		//System.out.println("Events to Cancel: "+timerList.size());
		Iterator<Timer> timerIterator = timerList.iterator();
		while(timerIterator.hasNext()) {
			Timer killTimer = timerIterator.next();
			if(killTimer!=null) {
				killTimer.cancel();
			}
		}
		Iterator<MidiMessage> noteOnIterator = noteOnList.iterator();
		while(noteOnIterator.hasNext()) {
			MidiMessage message = noteOnIterator.next();
			message.status = MidiMessage.NOTE_OFF;
			this.midiGen.output(message);
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
	
	public void beatCalcs() {
		int currentBeat = 1;
		long currentBeatTime = 0;
		currentBeat = sCurrentBeat;
		currentBeatTime = sBeatList[currentBeat];
		elapsedSinceBeat = System.currentTimeMillis() - currentBeatTime;
		elapsedSinceBar = System.currentTimeMillis() - sBeatList[1];
	}

}
