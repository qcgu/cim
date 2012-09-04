package cims.deciders;

import cims.datatypes.MidiMessage;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;

import static cims.supervisors.SupervisorMidi_Globals.sLastMidiMessage;
import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;
import static cims.supervisors.SupervisorMidi_Globals.sMidiStartTime;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.sSilenceDelay;

public class DecideMidi_01 {
	private SupervisorMidi supervisor;
	private GenerateMidi_Segment generator_segment;
	private GenerateMidi_Loop support_loop;
	private GenerateMidi_Loop initiate_loop;
	
	private int currentAction = -1;
	private boolean mirroring = false;
	private boolean mirrorFirstPass = false;
	
	
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
			generator_segment.makeSupportSegment(500, newMidiMessage.pitch); //sSilenceDelay, newMidiMessage.pitch);
			support_loop = new GenerateMidi_Loop(generator_segment);
			support_loop.setInterval(500 * 4); //sSilenceDelay * 4); // hard coded to 120 bpm for now2
			support_loop.start();
		}
		// stop generator if in mirror mode
		if (mirroring) {
			if (mirrorFirstPass){
				support_loop.stop();
				initiate_loop.stop();
				generator_segment.stop();
				turnOffAgentNotes();
				mirrorFirstPass = false;
			}
			supervisor.dataOut(new int[] {newMessage.status + 1, newMessage.pitch, newMessage.velocity});
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
				supervisor.txtMsg("Choosing to REPEATING");
				mirroring = false;
				support_loop.stop();
				initiate_loop.stop();
				generator_segment.makeLastSegment();
				generator_segment.generate(); //sNextPlay); // repeat last segment?
				break;
			case 1: // initiate
				supervisor.txtMsg("Choosing to INITIATING");
				mirroring = false;
				support_loop.stop(); // stop support
				initiate_loop.stop();
				generator_segment.stop(); // stop prev initiate
				turnOffAgentNotes();
				generator_segment.makeInitiateSegment(500); //sSilenceDelay);
				initiate_loop = new GenerateMidi_Loop(generator_segment);
				initiate_loop.setInterval(generator_segment.getInitiateSegementLength());
				initiate_loop.start();
				// clear out pitch histogram memory
				supervisor.analyser_stats.clearPitchHistogram();
				break;
			case 2: // support
				supervisor.txtMsg("Choosing to SUPPORT");
				mirroring = false;
				support_loop.stop();
				initiate_loop.stop();
				
				//generator_loop.start();
				break;
			case 3: // mirror
				supervisor.txtMsg("Choosing to MIRROR");
				mirroring = true;
				mirrorFirstPass = true;
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
