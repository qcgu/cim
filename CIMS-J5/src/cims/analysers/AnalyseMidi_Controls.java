package cims.analysers;

//import java.util.*;
import cims.analysers.AnalyseMidi;
import cims.supervisors.SupervisorMidi;

import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class AnalyseMidi_Controls extends AnalyseMidi {
	
	public AnalyseMidi_Controls(SupervisorMidi supervisor) {
		super(supervisor);
	}
	
	public void analyse() {
		LOGGER.info("ANALYSE CONTROLLER: "+current_message.controller + " VALUE: "+current_message.controlData + " DATA1: "+current_message.otherData1 + " DATA2: "+current_message.otherData2);
	}
	
}
