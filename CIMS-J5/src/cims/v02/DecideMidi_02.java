package cims.v02;

import cims.datatypes.MidiMessage;
import cims.generators.GenerateMidi_Loop;
import cims.supervisors.SupervisorMidi;
import cims.utilities.Randomiser;

import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class DecideMidi_02 {
	private SupervisorMidi supervisor;
	private GenerateMidi_Segment_02 generator_repeatSegment;
	private GenerateMidi_Segment_02 generator_supportSegment;
	private GenerateMidi_Segment_02 generator_initiateSegment;
	private GenerateMidi_Note_02 generator_mirror;
	private GenerateMidi_Loop support_loop;
	private GenerateMidi_Loop initiate_loop;
	private Randomiser randomiser;
	
	private int currentAction = -1;
	private int nextAction = 2;
	private boolean mirroring = false;
	
	public DecideMidi_02(SupervisorMidi supervisor) {
		this.supervisor=supervisor;
		generator_repeatSegment = new GenerateMidi_Segment_02(supervisor);
		generator_supportSegment = new GenerateMidi_Segment_02(supervisor);
		generator_initiateSegment = new GenerateMidi_Segment_02(supervisor);
		generator_mirror = new GenerateMidi_Note_02(supervisor);
		randomiser = new Randomiser();
	}

	public void messageIn(MidiMessage newMessage) {
		if(mirroring) {
			generator_mirror.setMessage(newMessage);
			generator_mirror.transform(GenerateMidi_Note_02.PITCH_SHIFT, 12);
			generator_mirror.output();
		}

		if (currentAction==2 && !support_loop.hasStarted) { //Wait for midi in before supporting
			support_loop.start();
		}

	}
	
	public void chooseNextAction() {
		int segmentLength = 0;
		mirroring = false;
		if(!(support_loop==null)) support_loop.stop();
		if(!(initiate_loop==null)) initiate_loop.stop();
		
		if(currentAction<0) {
			currentAction=2; //default start is SUPPORT
		} else {
			currentAction = nextAction;
		}
		nextAction = randomiser.positiveInteger(3);
		LOGGER.info("chooseNextAction");
		supervisor.txtMsg(""+ this.actionName(currentAction) +" >> "+ this.actionName(nextAction));
		switch (currentAction) {
			case 0: // repeat
				generator_repeatSegment.makeLastSegment();
				generator_repeatSegment.generate(sNextPlay); // repeat last segment?
				break;
			case 1: // initiate
				//if(!(initiate_loop==null)) initiate_loop.stop(); 
				segmentLength = generator_initiateSegment.initiateSegment();
				initiate_loop = new GenerateMidi_Loop(generator_initiateSegment);
				initiate_loop.setInterval(segmentLength);
				initiate_loop.start();
				// clear out pitch histogram memory
				sMidiStats.clearPitchHistogram();
				break;
			case 2: // support
				segmentLength = generator_supportSegment.supportSegment();
				support_loop = new GenerateMidi_Loop(generator_supportSegment);
				support_loop.setInterval(segmentLength);
				// Call support_loop.start() when next midi is received.
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
			returnString = "REPEAT after play";
			break;
		case 1:
			returnString = "INITIATE after play";
			break;
		case 2:
			returnString = "SUPPORT on play";
			break;
		case 3:
			returnString = "MIRROR on play";
			break;
		}
		return returnString;
	}
	
}
