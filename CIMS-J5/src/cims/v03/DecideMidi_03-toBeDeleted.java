package cims.v03;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.Randomiser;

import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;

public class DecideMidi_03 {
	private SupervisorMidi supervisor;
	private GenerateMidi_Segment_03 generator_repeatSegment;
	private GenerateMidi_Segment_03 generator_supportSegment;
	private GenerateMidi_Segment_03 generator_initiateSegment;
	//private GenerateMidi_Note_03 generator_mirror;
	//private GenerateMidi_Loop_03 support_loop;
	//private GenerateMidi_Loop_03 initiate_loop;
	private Randomiser randomiser;
	
	private int currentAction = -1;
	private int nextAction = 2;
	private boolean mirroring = false;
	private double mirroringType = 0;
	
	private boolean firstAction = true;
	private MidiMessage firstMessage;
	
	public static Logger LOGGER = Logger.getLogger(DecideMidi_03.class);
	
	public DecideMidi_03(SupervisorMidi supervisor) {
		this.supervisor=supervisor;
		generator_repeatSegment = new GenerateMidi_Segment_03(supervisor);
		generator_supportSegment = new GenerateMidi_Segment_03(supervisor);
		generator_initiateSegment = new GenerateMidi_Segment_03(supervisor);
		//generator_mirror = new GenerateMidi_Note_03(supervisor);
		randomiser = new Randomiser();
		
		LOGGER.setLevel(Level.INFO);
	}
/*
	public void messageIn(MidiMessage newMessage) {
		
		if(mirroring) {
			generator_mirror.setMessage(newMessage);
			if (mirroringType < 0.3){ // select one of two mirroring processes at random
				generator_mirror.transform(GenerateMidi_Note_03.PITCH_SHIFT, 12);
			} else if (mirroringType < 0.7) {
				generator_mirror.transform(GenerateMidi_Note_03.LOWER_TRIADIC, 0);
			} else {
				generator_mirror.transform(GenerateMidi_Note_03.PARALLEL_INTERVAL, 3);
			}
			generator_mirror.output();
		}

		if (currentAction==2 && !support_loop.hasStarted) { //Wait for midi in before supporting
			support_loop.start();
		}
		

	}
	*/
	/*
	public void segmentCreated(MidiSegment newSegment) {
		this.chooseNextAction();
	}
	*/
	/*
	public void firstAction(MidiMessage firstMessage) {
		LOGGER.debug("firsMessage Status: "+firstMessage.status);
		this.firstMessage = firstMessage;
		//Support as first activity
		this.nextAction = 2;
		this.chooseNextAction();
		this. firstAction = false;
		this.support_loop.start();
	} */
	/*
	public void chooseNextAction() {
		
		int segmentLength = 0;
		mirroring = false;
		if(!(support_loop==null)) support_loop.stop();
		if(!(initiate_loop==null)) initiate_loop.stop();
		currentAction = nextAction;
		nextAction = randomiser.weightedActivityChoice(); //randomiser.positiveInteger(0);
		LOGGER.debug("chooseNextAction");
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
				initiate_loop = new GenerateMidi_Loop_03(generator_initiateSegment);
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
				support_loop = new GenerateMidi_Loop_03(generator_supportSegment);
				support_loop.setInterval(segmentLength);
				break;
			case 3: // mirror
				mirroringType = Math.random();
				mirroring = true;
				break;
			case 4: // silence
				// do nothing
				break;
		}
		
	}
	*/
	
	/*****************************************************************************************
	 * Utility method to provide text version of an action
	 * @param action
	 * @return a string with the text version of the supplied action
	 */
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
