package cims.v02;

import cims.datatypes.MidiMessage;
//import cims.generators.GenerateMidi_Loop;
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
	private GenerateMidi_Loop_02 support_loop;
	private GenerateMidi_Loop_02 initiate_loop;
	private Randomiser randomiser;
	
	private int currentAction = -1;
	private int nextAction = 2;
	private boolean mirroring = false;
	
	private boolean firstAction = true;
	private MidiMessage firstMessage;
	
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
	
	public void firstAction(MidiMessage firstMessage) {
		this.firstMessage = firstMessage;
		//Support as first activity
		nextAction = 2;
		chooseNextAction();
		firstAction = false;
		support_loop.start();
	}
	
	public void chooseNextAction() {
		int segmentLength = 0;
		mirroring = false;
		if(!(support_loop==null)) support_loop.stop();
		if(!(initiate_loop==null)) initiate_loop.stop();
		currentAction = nextAction;
		nextAction = randomiser.positiveInteger(0); //randomiser.weightedActivityChoice(); //randomiser.positiveInteger(0);
		LOGGER.info("chooseNextAction");
		String userFeedback = ""+ this.actionName(currentAction) +" >> "+ this.actionName(nextAction);
		supervisor.txtMsg(userFeedback);
		supervisor.oscSysMsg(userFeedback);
		supervisor.displayNextAction(nextAction,false);
		supervisor.displayNextAction(currentAction,true);
		//TODO Force support for testing
		//currentAction = 2;
		
		switch (currentAction) {
			case 0: // repeat
				generator_repeatSegment.makeLastSegment();
				generator_repeatSegment.generate(sNextPlay); // repeat last segment?
				break;
			case 1: // initiate
				//if(!(initiate_loop==null)) initiate_loop.stop(); 
				segmentLength = generator_initiateSegment.initiateSegment();
				initiate_loop = new GenerateMidi_Loop_02(generator_initiateSegment);
				initiate_loop.setInterval(segmentLength);
				initiate_loop.start();
				// clear out pitch histogram memory
				sMidiStats.clearPitchHistogram();
				break;
			case 2: // support
				if(firstAction) {
					segmentLength = generator_supportSegment.firstSupportSegment(firstMessage);
				} else {
					segmentLength = generator_supportSegment.supportSegment();
				}
				support_loop = new GenerateMidi_Loop_02(generator_supportSegment);
				support_loop.setInterval(segmentLength);
				break;
			case 3: // mirror
				mirroring = true;
				break;
			case 4: // silence
				// do nothing
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
		case 4:
			returnString = "SILENCE on play";
			break;
		}
		return returnString;
	}
	
}
