/**
 * CIMS - GenerateMIDI - Make decisions based on Analysis and raw data and generate MIDI for playing.
 * 
 */

/**
 * @author Andrew Gibson andrew@gibsons.id.au
 * @version 120725
 */

package cims.generators;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class GenerateMidi_Segment extends GenerateMidi {

	private volatile MidiSegment midiSegment;
	private volatile OutputQueue midiQueue;
	private SupervisorMidi supervisor;
	private int initiateSegementLength = 0;
	
	public GenerateMidi_Segment(SupervisorMidi supervisor) {
		super(supervisor);
		this.supervisor = supervisor;
		midiQueue = new OutputQueue(this);
	}
	
	public GenerateMidi_Segment(SupervisorMidi supervisor, MidiSegment segment) {
		super(supervisor);
		this.supervisor = supervisor;
		midiQueue = new OutputQueue(this);
		this.midiSegment = segment;
	}
	
	public void generate() {
		midiQueue.addSegment(midiSegment);
		//System.out.println("Playing generated segment " + midiSegment.size());
		midiQueue.play();
	}
	
	public void stop() {
		midiQueue.cancel();
	}
	
	public void generate(int start) {
		switch(start) {
		case 0:
			midiQueue.startOnPlay();
			break;
		case 1:
			midiQueue.startOnNextBeat();
			break;
		case 2:
			midiQueue.startOnNextBar();
		}
		this.generate();
	}
	
	public void output(MidiMessage midimessage) {
		int[] message = {midimessage.status,midimessage.pitch,midimessage.velocity};
		this.supervisor.dataOut(message);
		//supervisor.txtMsg("TC: "+Thread.activeCount());
	}
		
	public synchronized void makeLastSegment () {
		// Play back the last segment
		midiSegment = supervisor.getLastMidiSegment();
		midiSegment.zeroTiming();
	}
	
	public synchronized void makeInitiateSegment(int duration) {
		//supervisor.txtMsg("rnd pitch = " + supervisor.analyser_stats.getRandomPitchClass());
		this.midiSegment = new MidiSegment();
		//int[] pitches = {72, 74, 76, 79, 81, 84};
		int accumTime = 0;
		addNote(accumTime, supervisor.analyser_stats.getRandomPitchClass() + 72, (int)(Math.random() * 30) + 80, duration);
		accumTime += duration;
		for(int i=1; i<4; i++) {
			int dur = duration;
			if (Math.random() < 0.5) dur = duration / 2;
			addNote(accumTime, supervisor.analyser_stats.getRandomPitchClass() + 72, (int)(Math.random() * 30) + 80, dur);
			accumTime += dur;
		}
		addNote(accumTime, supervisor.analyser_stats.getRandomPitchClass() + 72, (int)(Math.random() * 30) + 80, duration * 2);
		initiateSegementLength = accumTime + duration * 2 - 20; // slight reduction to avoid overshoot assuming quantise is on
	}
	
	public int getInitiateSegementLength() {
		return initiateSegementLength;
	}
	
	public synchronized void makeSupportSegment(int duration, int pitch) {
		this.midiSegment = new MidiSegment();
		// for getting previous segment first pitch - depricated
		//int pitchClass = 0; 
		//if (supervisor.getLastMidiSegment() != null) {
		//	MidiMessage mess = supervisor.getLastMidiSegment().getFirstMessage();
		//	pitchClass = mess.pitch % 12;
		//}
		for (int i=0; i<4; i++) {
			addNote(i * duration, pitch - 12, (int)(Math.random() * 30) + 80, duration);
		}
		
	}
	
	private void addNote(int startTime, int pitch, int velocity, int duration) {
		MidiMessage noteOn = new MidiMessage();
		int[] onMessage = {MidiMessage.NOTE_ON,pitch,velocity};
		noteOn.set(onMessage, false);
		noteOn.timeMillis = startTime;
		MidiMessage noteOff = new MidiMessage();
		int[] offMessage = {MidiMessage.NOTE_OFF,pitch,0};
		noteOff.set(offMessage, false);
		noteOff.timeMillis = (int)(startTime + duration * 0.8);
		this.midiSegment.add(noteOn);
		this.midiSegment.add(noteOff);
	}
	
}
	
