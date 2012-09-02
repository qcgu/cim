package cims.deciders;

import cims.datatypes.MidiMessage;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;

import static cims.supervisors.SupervisorMidi_Globals.sLastMidiMessage;
import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;
import static cims.supervisors.SupervisorMidi_Globals.sMidiStartTime;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.sDefaultDuration;

public class DecideMidi_01 {
	private SupervisorMidi supervisor;
	private GenerateMidi_Segment generator_segment;
	private GenerateMidi_Loop support_loop;
	private GenerateMidi_Loop initiate_loop;
	
	private int currentAction = -1;
	private boolean mirroring = false;
	
	
	public DecideMidi_01(SupervisorMidi supervisor) {
		this.supervisor=supervisor;
		support_loop = new GenerateMidi_Loop(generator_segment);
		initiate_loop = new GenerateMidi_Loop(generator_segment);
	}
	
	public void addGenerator(GenerateMidi_Segment gs) {
		this.generator_segment = gs;
	}
	
	public void messageIn(MidiMessage newMessage) {
		MidiMessage newMidiMessage = new MidiMessage();
		newMidiMessage.copy(newMessage);
		sLastMidiMessage = newMidiMessage;
		sMidiMessageList.add(newMidiMessage);
		//this.txtMsg("AMM: "+newMessage.messageNum+"/"+MidiMessage.messagesCount+","+newMessage.timeMillis+","+newMessage.status+","+newMessage.pitch+","+newMessage.velocity+","+newMessage.noteOnOff+","+newMessage.channel+" |");
		if (sMidiStartTime==0) sMidiStartTime = System.currentTimeMillis();

		// start with supporting role
		if (currentAction == -1) {
			chooseNextAction();
			support_loop.start();
		}
		// play support with note on
		if (currentAction==2 && !support_loop.hasStarted) {
			support_loop.start();
		}
		// stop generator if in mirror mode
		if (mirroring) {
			support_loop.stop();
			initiate_loop.stop();
			generator_segment.stop();
			supervisor.txtMsg("Turning off notes");
			turnOffAgentNotes(); // these two calls need to go together to avoid stuck notes
			//mirroring = false;
			//initiating = false;
			// play mirrored note
			supervisor.dataOut(new int[] {newMessage.status, newMessage.pitch, newMessage.velocity});
		}
	}
	
	public void chooseNextAction() {
		if(currentAction<0) {
			currentAction=2; //default start is SUPPORT
		} else {
			currentAction = (int)(Math.random() * 4);
		}
		switch (currentAction) {
			case 0: // repeat
				supervisor.txtMsg("Choosing to REPEAT");
				mirroring = false;
				//initiating = false;
				support_loop.stop();
				initiate_loop.stop();
				generator_segment.makeLastSegment();
				generator_segment.generate(sNextPlay); // repeat last segment?
				break;
			case 1: // initiate
				supervisor.txtMsg("Choosing to INITIATE");
				mirroring = false;
				//initiating = true;
				support_loop.stop(); // stop support
				initiate_loop.stop();
				generator_segment.stop(); // stop prev initiate
				turnOffAgentNotes();
				generator_segment.makeInitiateSegment(sDefaultDuration);
				initiate_loop = new GenerateMidi_Loop(generator_segment);
				initiate_loop.setInterval(sDefaultDuration*16);
				initiate_loop.start();
				break;
			case 2: // support
				supervisor.txtMsg("Choosing to SUPPORT");
				mirroring = false;
				//initiating = false;
				support_loop.stop();
				initiate_loop.stop();
				generator_segment.makeSupportSegment(sDefaultDuration);
				support_loop = new GenerateMidi_Loop(generator_segment);
				support_loop.setInterval(sDefaultDuration);
				//generator_loop.start();
				break;
			case 3: // mirror
				supervisor.txtMsg("Choosing to MIRROR");
				mirroring = true;
				break;
		}
	}
	
	private void turnOffAgentNotes() {
		supervisor.allNotesOff();
	}
	
	public boolean isMirroring () {
		return mirroring;
	}
}
