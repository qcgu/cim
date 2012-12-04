package cims.analysers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.analysers.AnalyseMidi;
import cims.supervisors.SupervisorMidi;

//import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class AnalyseMidi_Controls extends AnalyseMidi {
	public static final Logger LOGGER = Logger.getLogger(AnalyseMidi_Controls.class);
	
	public AnalyseMidi_Controls(SupervisorMidi supervisor) {
		super(supervisor);
		LOGGER.setLevel(Level.INFO);
	}
	
	public void analyse() {
		LOGGER.debug("ANALYSE CONTROLLER: "+current_message.controller + " VALUE: "+current_message.controlData + " DATA1: "+current_message.otherData1 + " DATA2: "+current_message.otherData2);
	}
	
}
