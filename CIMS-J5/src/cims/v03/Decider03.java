package cims.v03;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.datatypes.BeatTime;
import cims.datatypes.MidiControlMessage;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.deciders.DeciderMidi;
import cims.generators.GeneratorMidi;
import cims.interfaces.Interface_Controls;
import cims.supervisors.SupervisorMidi;
import cims.utilities.Randomiser;

public class Decider03 extends DeciderMidi {

	private int currentAction = -1;
	private int nextAction = 0;
	
	private GeneratorMidi generator;
	private Randomiser randomiser;
	
	public static Logger LOGGER = Logger.getLogger(Decider03.class);
	
	public Decider03(SupervisorMidi supervisor) {
		super(supervisor);
		this.generator = new Generator03(supervisor);
		this.randomiser = new Randomiser();
		this.nextAction = GeneratorMidi.SUPPORT;
		this.generator.setActivityType(this.nextAction);
		this.setLoopSleepTime(0);
		LOGGER.setLevel(Level.INFO);
	}

	@Override
	public void deciderLoop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void firstMidiMessage(MidiMessage message) {
		LOGGER.debug("first midi message");
		this.oldFirstAction(message);
		
	}
	
	@Override
	public void newMidiMessage(MidiMessage message) {
		LOGGER.debug("new midi message");
		this.oldMessageIn(message);

	}

	@Override
	public void newMidiSegment(MidiSegment segment) {
		LOGGER.debug("new midi segement");
		this.oldChooseNextAction();

	}

	@Override
	public void newSegmentBoundaryTimecode(Long timecode) {
		LOGGER.debug("new segment boundary");
		// TODO Auto-generated method stub

	}

	@Override
	public void newBeatTime(BeatTime beatTime) {
		LOGGER.debug("new beatTime");
		// TODO Auto-generated method stub

	}

	@Override
	public void newMidiControlMessage(MidiControlMessage controlMessage) {
		LOGGER.debug("new control message");
		// TODO Auto-generated method stub

	}

	@Override
	public void newInterfaceUpdate(Interface_Controls controls) {
		LOGGER.debug("new interface update");
		// TODO Auto-generated method stub

	}

	@Override
	public void playMidiMessage(MidiMessage message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playMidiSegment(MidiSegment segment) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scheduleBeatTimeMidiMessage(MidiMessage message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scheduleBeatTimeMidiSegment(MidiSegment segment) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendMessageToInterface(String message) {
		// TODO Auto-generated method stub

	}
	
	
	/***
	 * 
	 * OLD METHODS
	 * 
	 */

	
	private void oldFirstAction(MidiMessage firstMessage) {
		LOGGER.debug("firsMessage Status: "+firstMessage.status);
		generator.setFirst_message(firstMessage);
		this.nextAction = 2;
		this.oldChooseNextAction();
		generator.setActivityType(GeneratorMidi.SUPPORT);
		generator.run();
	}
	
	private void oldMessageIn(MidiMessage message) {
		if(generator.getActivityType()==GeneratorMidi.MIRROR) {
			generator.setMirror_message(message);
			generator.run();
		}
		if (currentAction==GeneratorMidi.SUPPORT && !generator.supportHasStarted()) { //Wait for midi in before supporting
			generator.run();
		}
	}
	
	private void oldChooseNextAction() {
		generator.supportStop();
		generator.initiateStop();
		currentAction = nextAction;
		nextAction = randomiser.weightedActivityChoice(); //randomiser.positiveInteger(0);
		LOGGER.debug("chooseNextAction");
		String userFeedback = ""+ this.oldActionName(currentAction) +" >> "+ this.oldActionName(nextAction);
		supervisor.txtMsg(userFeedback);
		supervisor.oscSysMsg(userFeedback);
		supervisor.displayNextAction(nextAction,false);
		supervisor.displayNextAction(currentAction,true);
		generator.setActivityType(currentAction);
		generator.run();
	}
	
	private String oldActionName(int action) {
		String returnString = "";
		switch(action) {
		case 0:
			returnString = "SILENCE on play";
			break;
		case 1:
			returnString = "REPEAT after play";
			break;
		case 2:
			returnString = "MIRROR on play";
			break;
		case 3:
			returnString = "SUPPORT on play";
			break;
		case 4:
			returnString = "INITIATE after play";
			break;
		}
		return returnString;
	}
}
