package cims.deciders;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_NoteMirror;
import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.Randomiser;

import static cims.supervisors.SupervisorMidi_Globals.sLastMidiMessage;
import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;
import static cims.supervisors.SupervisorMidi_Globals.sMidiStartTime;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.sDefaultDuration;
import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class DecideMidi_01 {
	private SupervisorMidi supervisor;
	private GenerateMidi_Segment generator_repeatSegment;
	private GenerateMidi_Segment generator_mirrorSegment;
	private GenerateMidi_Segment generator_initiateSegment;
	private GenerateMidi_NoteMirror generator_mirror;
	private GenerateMidi_Loop support_loop;
	private GenerateMidi_Loop initiate_loop;
	private Randomiser randomiser;
	
	private int currentAction = -1;
	private int nextAction = 2;
	private boolean mirroring = false;
	
	public DecideMidi_01(SupervisorMidi supervisor) {
		this.supervisor=supervisor;
		generator_repeatSegment = new GenerateMidi_Segment(supervisor);
		generator_mirrorSegment = new GenerateMidi_Segment(supervisor);
		generator_initiateSegment = new GenerateMidi_Segment(supervisor);
		generator_mirror = new GenerateMidi_NoteMirror(supervisor);
		randomiser = new Randomiser();
	}

	public void messageIn(MidiMessage newMessage) {
		if(mirroring) {
			generator_mirror.setMessage(newMessage);
			generator_mirror.transform(GenerateMidi_NoteMirror.PITCH_SHIFT, 12);
			generator_mirror.output();
		}

		if (currentAction==2 && !support_loop.hasStarted) { //Wait for midi in before supporting
			support_loop.start();
		}

	}
	
	public void chooseNextAction() {
		mirroring = false;
		if(support_loop!=null) support_loop.stop();
		if(initiate_loop!=null) initiate_loop.stop();
		
		if(currentAction<0) {
			currentAction=2; //default start is SUPPORT
		} else {
			currentAction = nextAction;
		}
		nextAction = randomiser.positiveInteger(3);
		supervisor.txtMsg("NOW >> "+ this.actionName(currentAction) +" -- NEXT >>"+ this.actionName(nextAction));
		switch (currentAction) {
			case 0: // repeat
				generator_repeatSegment.makeLastSegment();
				generator_repeatSegment.generate(sNextPlay); // repeat last segment?
				break;
			case 1: // initiate
				generator_initiateSegment.makeEmptySegment();
				int length = this.addInitiateNotes(sDefaultDuration);
				initiate_loop = new GenerateMidi_Loop(generator_initiateSegment);
				initiate_loop.setInterval(length);
				initiate_loop.start();
				// clear out pitch histogram memory
				sMidiStats.clearPitchHistogram();
				break;
			case 2: // support
				generator_mirrorSegment.makeNoteSegment(0,supervisor.getLastMidiSegment().firstMessage().pitch, randomiser.positiveInteger(40) + 80, sDefaultDuration);
				support_loop = new GenerateMidi_Loop(generator_mirrorSegment);
				support_loop.setInterval(sDefaultDuration);
				//support_loop.start();  >>> Starts only when next midi is received.
				break;
			case 3: // mirror
				mirroring = true;
				break;
		}
	}
	
	public String actionName(int action) {
		String returnString = "";
		switch(action) {
		case 0:
			returnString = "REPEAT";
			break;
		case 1:
			returnString = "INITIATE";
			break;
		case 2:
			returnString = "SUPPORT";
			break;
		case 3:
			returnString = "MIRROR";
			break;
		}
		return returnString;
	}
	public int addInitiateNotes(int duration) {
		int accumTime = 0;
		int segmentLength = 0;
		generator_initiateSegment.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, duration);
		accumTime += duration;
		for(int i=1; i<8; i++) {
			int dur = duration;
			if (Math.random() < 0.5) dur = duration / 2;
			generator_initiateSegment.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, dur);
			accumTime += dur;
		}
		generator_initiateSegment.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, duration*2);
		segmentLength = accumTime + duration * 2 - 20; // slight reduction to avoid overshoot assuming quantise is on
		return segmentLength;
	}
}
