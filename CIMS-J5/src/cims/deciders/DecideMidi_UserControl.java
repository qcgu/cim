package cims.deciders;

import static cims.supervisors.SupervisorMidi_Globals.sBeatList;
import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeat;
import static cims.supervisors.SupervisorMidi_Globals.sMetronome;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.sRepeatInterval;
import static cims.supervisors.SupervisorMidi_Globals.sSegmentGap;
import static cims.supervisors.SupervisorMidi_Globals.sSegmentGapDuration;
import static cims.supervisors.SupervisorMidi_Globals.sDefaultDuration;
import static cims.supervisors.SupervisorMidi_Globals.sTimeBetweenBeats;
import static cims.supervisors.SupervisorMidi_Globals.sTestMode;
import static cims.supervisors.SupervisorMidi_Globals.sBeatsPerMinute;
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
					recalcDefaultTimings();
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
				if(key.equals("beat") && sMetronome) {
					
					int beat = value;
					sCurrentBeat = beat;
					sBeatList[beat] = System.currentTimeMillis();
					int prevBeat = beat-1;
					if (prevBeat<1) prevBeat = (int) sBeatList[0];
					Long timeBetween = (sBeatList[beat] - sBeatList[prevBeat]);
					if (timeBetween>4000) timeBetween = (long) 500; // default 120BPM
					sTimeBetweenBeats = timeBetween.intValue();
					if (sTimeBetweenBeats<1) sTimeBetweenBeats = 0;
					//this.txtMsg("Time between beats: "+sTimeBetweenBeats);
				}
				if(key.equals("bpm") && sMetronome) {
					sBeatsPerMinute = value;
					recalcDefaultTimings();
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
	
	public void recalcDefaultTimings() {
		Float beatLength = 1000/((float)sBeatsPerMinute/60);
		sSegmentGapDuration = (sSegmentGap*(beatLength.intValue()/4))-10;
		sDefaultDuration = (beatLength.intValue()/2);
		/* supervisor.txtMsg("Segment Gap Duration: "+sSegmentGapDuration+"ms");
		supervisor.txtMsg("Default Duration: "+sDefaultDuration+"ms");
		supervisor.txtMsg("Segment Gap: "+sSegmentGap+"semiquavers");
		supervisor.txtMsg("Beat Length: "+beatLength+"ms"); */
		
	}

}
