package cims.analysers;

//import java.util.*;
import cims.analysers.AnalyseMidi;
import cims.supervisors.SupervisorMidi;

public class AnalyseMidi_Controls extends AnalyseMidi {
	
	public AnalyseMidi_Controls(SupervisorMidi supervisor) {
		super(supervisor);
	}
	
	public void analyse() {
		supervisor.txtMsg("CONTROLLER: "+current_message.controller + " VALUE: "+current_message.controlData);
	}
	
}
