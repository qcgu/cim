package cims.utilities;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class Randomiser {
	
	public Randomiser() {
		
	}
	
	public int positiveInteger(int maxValue) {
		int value =  (int) (Math.random() * (maxValue+1));
		return value;
	}
	
	public int getRandomPitchClass() {
		int pchLength = sMidiStats.getPitchClassHistogram().length; //Fixed at 12
		int maxCnt = 0;
		for (int i=0; i<pchLength; i++) {
			maxCnt += sMidiStats.getPitchClass(i+1);
		}
		int rnd = (int)(Math.random() * maxCnt);
		LOGGER.info("mxCnt = " + maxCnt + " rnd = " + rnd);

		int val = 0;
		int pitchClass = 0;
		while(val < rnd) {
			pitchClass++;
			val += sMidiStats.getPitchClass(pitchClass);
		}
		return pitchClass;
	}
	
}
