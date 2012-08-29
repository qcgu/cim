package cims.deciders;

import cims.datatypes.MidiMessage;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;

import static cims.supervisors.SupervisorMidi_Globals.sLastMidiMessage;
import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;
import static cims.supervisors.SupervisorMidi_Globals.sMidiStartTime;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;

public class DecideMidi_01 {
	private SupervisorMidi supervisor;
	private GenerateMidi_Segment generator_segment;
	private GenerateMidi_Loop generator_loop;
	
	private int currentAction = -1;
	private boolean mirroring = false;
	
	
	public DecideMidi_01(SupervisorMidi supervisor) {
		this.supervisor=supervisor;
	}
	
	public void addGenerator(GenerateMidi_Segment gs) {
		this.generator_segment = gs;
	}
	public void addGenerator(GenerateMidi_Loop gl) {
		this.generator_loop = gl;
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
			supervisor.txtMsg("Choosing to SUPPORT");
			currentAction = 2;
			generator_segment.makeSupportSegment(250);
			generator_loop = new GenerateMidi_Loop(generator_segment);
			generator_loop.setInterval(250);
			generator_loop.start();
		}
		// stop generator if in mirror mode
		if (mirroring) {
			generator_loop.stop();
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
		currentAction = (int)(Math.random() * 4);
		switch (currentAction) {
			case 0: // repeat
				supervisor.txtMsg("Choosing to REPEAT");
				mirroring = false;
				//initiating = false;
				generator_loop.stop();
				generator_segment.makeLastSegment();
				generator_segment.generate(sNextPlay); // repeat last segment?
				break;
			case 1: // initiate
				supervisor.txtMsg("Choosing to INITIATE");
				mirroring = false;
				//initiating = true;
				generator_loop.stop(); // stop support
				generator_segment.stop(); // stop prev initiate
				turnOffAgentNotes();
				generator_segment.makeInitiateSegment(250);
				generator_loop = new GenerateMidi_Loop(generator_segment);
				generator_loop.setInterval(4000);
				generator_loop.start();
				break;
			case 2: // support
				supervisor.txtMsg("Choosing to SUPPORT");
				mirroring = false;
				//initiating = false;
				generator_loop.stop();
				generator_segment.makeSupportSegment(250);
				generator_loop = new GenerateMidi_Loop(generator_segment);
				generator_loop.setInterval(250);
				generator_loop.start();
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
