package cims.v03;

import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.Randomiser;

import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeatTime;
import static cims.supervisors.SupervisorMidi_Globals.sPitchClassSet;
import static cims.supervisors.SupervisorMidi_Globals.sRootPitch;

public class GenerateMidi_Segment_03 extends GenerateMidi_Segment {
	private SupervisorMidi supervisor;
	private Randomiser randomiser;
	
	public static Logger LOGGER = Logger.getLogger(GenerateMidi_Segment_03.class);
	
	public GenerateMidi_Segment_03(SupervisorMidi supervisor) {
		super(supervisor);
		this.supervisor = supervisor;
		this.randomiser = new Randomiser();
		LOGGER.setLevel(Level.INFO);
	}

	public GenerateMidi_Segment_03(SupervisorMidi supervisor, MidiSegment segment) {
		super(supervisor, segment);
		this.supervisor = supervisor;
		this.randomiser = new Randomiser();
		LOGGER.setLevel(Level.INFO);
	}
	
	public int supportSegment() {
		this.makeEmptySegment();
		int beatsInBar = sCurrentBeatTime.getBeatsPerBar();
		int duration = sCurrentBeatTime.getDefaultDuration();
		for(int i=0;i<beatsInBar;i++) {
			this.addNote((duration*i), supervisor.getLastMidiSegment().firstMessage().pitch, randomiser.positiveInteger(40) + 80, duration);
		}
		return (duration*beatsInBar);	
	}
	
	public int firstSupportSegment(MidiMessage firstMessage) {
		this.makeEmptySegment();
		int beatsInBar = sCurrentBeatTime.getBeatsPerBar();
		int duration = sCurrentBeatTime.getDefaultDuration();
		for(int i=0;i<beatsInBar;i++) { //Creates 1 bar of notes
			LOGGER.debug("duration: "+duration);
			LOGGER.debug("firstMessage.pitch: "+firstMessage.pitch);
			this.addNote((duration*i), firstMessage.pitch, randomiser.positiveInteger(40) + 80, duration);
		}
		return (duration*beatsInBar);
	}
	
	public MidiSegment getMidiSegment() {
		return this.midiSegment;
	}
	
	public int initiateSegment() {
		this.makeEmptySegment();
		int duration = sCurrentBeatTime.getDefaultDuration();
		int accumTime = 0;
		int segmentLength = 0;
		this.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, duration);
		accumTime += duration;
		for(int i=1; i<8; i++) {
			int dur = duration;
			if (Math.random() < 0.5) dur = duration / 2;
			this.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, dur);
			accumTime += dur;
		}
		this.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, duration*2);
		segmentLength = accumTime + duration * 2 - 20; // slight reduction to avoid overshoot assuming quantise is on
		return segmentLength;
	}
	
	public synchronized void makeLastSegment () {

		midiSegment = supervisor.getLastMidiSegment();
		midiSegment.zeroTiming();
		if(midiSegment==null) midiSegment = new MidiSegment();	
		if (Math.random() < 0.8) { // choose to modify repeat 50% of the time
			HashMap<Integer, Integer> currentOnMod = new HashMap<Integer, Integer>();
			List<MidiMessage> data = midiSegment.asList();
			if(!(data.isEmpty())) {
				Iterator<MidiMessage> itr = data.iterator();
				while(itr.hasNext()) {
			         MidiMessage mess = itr.next();
			         if (MidiMessage.isNoteOn(mess.messageType)) {
			        	 int deviate = (int) (randomiser.gaussian(0, 1) * 2);
			        	 if (mess.pitch % 12 == sPitchClassSet[0] + sRootPitch) deviate = 0; // keep root note stable
			        	 int newPitch = pitchQuantize(mess.pitch + deviate);
			        	 //System.out.println("Changing note on from " + mess.pitch + " to " + " " + deviate + " " + newPitch);
			        	 currentOnMod.put(mess.pitch, newPitch);
			        	 mess.rawMessage[1] = newPitch; // data to be played
			        	 mess.pitch = newPitch; // meta data
			         } else if (MidiMessage.isNoteOff(mess.messageType)) {
			        	 int currPitch = mess.pitch;
				     		if(currentOnMod.containsKey(currPitch)) {
				     			mess.rawMessage[1] = currentOnMod.get(currPitch); // data to be played
				     			mess.pitch = currentOnMod.get(currPitch); // meta data
				     			currentOnMod.remove(currPitch);
				     		} else {
				     			LOGGER.error("No currPitch found in currentOnMod: "+currPitch);
				     		}
			         }
			    }
				midiSegment = new MidiSegment(data);
				midiSegment.setChannel(2); // channel is in the parochial range of 1-16

			}
		}
		
	}
	
	public int pitchQuantize(int pitch) {
		int[] pcSet = sPitchClassSet;
		int pitchClass = pitch%12;
		int adjust = 100;
		for (int i=0; i<pcSet.length; i++ ) {
			if (Math.abs(pitchClass - pcSet[i]) < adjust) adjust = Math.abs(pitchClass - pcSet[i]);
		}
		return pitch + adjust;
	}

}
