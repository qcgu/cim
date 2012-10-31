package cims.deciders;

import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeatTime;
import static cims.supervisors.SupervisorMidi_Globals.sMetronome;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.sRepeatInterval;
import static cims.supervisors.SupervisorMidi_Globals.sSegmentGap;
import static cims.supervisors.SupervisorMidi_Globals.sTestMode;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

import cims.supervisors.SupervisorMidi;


public class DecideMidi_UserControl {
	private SupervisorMidi supervisor;
	
	public DecideMidi_UserControl(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
	}
	
	public void input(String key, int value) {
		//this.txtMsg("Super Key: "+this.io.key()+" Super Value: "+this.io.value());
				if(key.equals("segmentGap")) {
					sSegmentGap = value;
					sCurrentBeatTime.recalcDefaultTimings();
				}
				if(key.equals("repeatCue")) {
					sRepeatInterval = value;
					supervisor.txtMsg("Repeat interval set: "+sRepeatInterval+"ms");
				}
				if(key.equals("metronome")) {
					if(value==1) {
						sMetronome = true;
						LOGGER.info("METRONOME ON");
					} else {
						sMetronome = false;
						LOGGER.info("METRONOME OFF");
					}
				}
				
				if(key.equals("test")) {
					if(value==1) {
						supervisor.txtMsg("TEST MODE ON");
						sTestMode = true;
						//this.runTests();
					} else {
						supervisor.txtMsg("TEST MODE OFF");
						sTestMode = false;
					}
				}
				if(key.equals("nextPlay")) {
					LOGGER.info("NEXTPLAY: "+value);
					sNextPlay = value;
				}
	}
	

}
