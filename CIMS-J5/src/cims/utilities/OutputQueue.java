package cims.utilities;

import java.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import cims.datatypes.*;
import cims.generators.*;
import cims.players.PlayMidi_BeatTime;

import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeatTime;

/*****************************************************************************************
 * Manages a Timer and TimerTask for the playback of MIDI messages in time according to
 * the timemillis in those messages. It can queue a segment for playback immediately, or on
 * the next beat or next bar (as determined by the current BeatTime information).
 * OutputQueue does NOT change the timing of segments to match changes in BeatTime that
 * may occur during the playback of MIDI messages. For this task, use an appropriate Player.
 * 
 * @author Andrew Gibson a.gibson@griffith.edu.au
 *
 */
public class OutputQueue {
	private GenerateMidi_Segment midiGen;
	private PlayMidi_BeatTime beatPlayer;
	private boolean isPlayer = false;
	private volatile MidiSegment segmentToPlay;
	private Timer segmentTimer;
	private ArrayList<Timer> timerList;
	private ArrayList<MidiMessage> noteOnList;
	private boolean startOnNextBeat = false;
	private boolean startOnNextBar = false;
	private long pastTheBeatTolerance = BeatTime.timeBetweenBeats/64;
	
	public static Logger LOGGER = Logger.getLogger(OutputQueue.class);

	public OutputQueue(GenerateMidi_Segment newMidiGen) {
		this.midiGen = newMidiGen;
		this.timerList = new ArrayList<Timer>();
		this.noteOnList = new ArrayList<MidiMessage>();
		LOGGER.setLevel(Level.INFO);
	}
	
	public OutputQueue(PlayMidi_BeatTime player) {
		this.beatPlayer = player;
		this.isPlayer = true;
		this.timerList = new ArrayList<Timer>();
		this.noteOnList = new ArrayList<MidiMessage>();
		LOGGER.setLevel(Level.DEBUG);
	}
	
	public synchronized void addSegment(MidiSegment segment) {
		this.segmentToPlay = segment;
	}
	
	public void play() {
		LOGGER.debug("OutputQueue:play()");
		Iterator<MidiMessage> segmentIterator;
		boolean firstEvent = true;
		long segmentStartTime = 0;
		long delay = 0;
		long timeToWait = BeatTime.timeBetweenBeats;
		MidiMessage midimessage = new MidiMessage();
		
		if(segmentToPlay==null) {
			LOGGER.error("segmentToPlay is NULL");
			segmentToPlay = new MidiSegment();
		}
		if(!segmentToPlay.isEmpty()) {
			segmentIterator = segmentToPlay.asList().iterator();
			while (segmentIterator.hasNext()) {
				midimessage = new MidiMessage();
				midimessage.copy(segmentIterator.next());
				segmentTimer = new Timer();
				timerList.add(segmentTimer);
				if(midimessage.noteOnOff==MidiMessage.NOTE_ON) {
					noteOnList.add(midimessage);
				}
				if(midimessage.timeMillis<0) {
					LOGGER.warn("midimessage.timeMillis is less than 0: "+midimessage.timeMillis+ " >> now set to 0");
					midimessage.timeMillis = 0;
				}
				if(firstEvent) {
					LOGGER.debug("First MidiMessage in queue");
					segmentStartTime = midimessage.timeMillis;
					delay = 0; //Play first message straight away
					firstEvent = false;
				} else {
					delay = midimessage.timeMillis - segmentStartTime;
				}
				if (this.isPlayer) {
					segmentTimer.schedule(new BeatPlayer(this.beatPlayer,midimessage), 0);
				} else {
					// SHIFTING TO SUIT NEXTPLAY - BEAT OR BAR
					if(startOnNextBeat) {
						if (BeatTime.elapsedSinceBeat<pastTheBeatTolerance) {
							timeToWait=0;
						} else {
							timeToWait = BeatTime.timeBetweenBeats - BeatTime.elapsedSinceBeat;
						}
						LOGGER.debug("BEAT >> DELAY: "+delay+" WAIT: "+timeToWait);
						delay = delay + timeToWait;
					}
					if(startOnNextBar) {
						if (BeatTime.elapsedSinceBar<pastTheBeatTolerance) {
							timeToWait=0;
						} else {
							timeToWait = (BeatTime.timeBetweenBeats*sCurrentBeatTime.getValueFor("beatsPerBar")) - BeatTime.elapsedSinceBar;
						}
						LOGGER.debug("BAR >> DELAY: "+delay+" WAIT: "+timeToWait);
						delay = delay + timeToWait;
					}
					if(delay<0) {
						LOGGER.warn("NEGATIVE DELAY - SET TO 0");
						delay=0;
					}
					//LOGGER.info("OutputQueue: pitch = " + midimessage.messageType + " " + midimessage.pitch + " " + midimessage.velocity + " " + midimessage.timeMillis);
					segmentTimer.schedule(new Player(this.midiGen,midimessage), delay);
				}
			}
		} else {
			LOGGER.error("NO SEGMENT TO PLAY!! ");
		}
		
	}
		
	public void cancel() {
		LOGGER.debug("Events to Cancel: "+timerList.size());
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
			this.gm = gm;
			this.outputMessage = midimessage;
		}

		@Override
		public void run() {
			LOGGER.debug("run() in OutputQueue.Player outputting midi: "+outputMessage.status);
			this.gm.output(this.outputMessage);
		}
		
	}
	
	private class BeatPlayer extends TimerTask {
		private PlayMidi_BeatTime pm;
		private MidiMessage outputMessage;
		public BeatPlayer(PlayMidi_BeatTime pm, MidiMessage midimessage) {;
			this.pm = pm;
			this.outputMessage = midimessage;
		}

		@Override
		public void run() {
			LOGGER.debug("run() in OutputQueue.BeatPlayer outputting midi: "+outputMessage.status);
			this.pm.output(this.outputMessage);
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
