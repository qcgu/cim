package cims.utilities;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;

public class Randomiser {
	
	public Randomiser() {
		
	}
	
	public int positiveInteger(int maxValue) {
		return (int) Math.random() * (maxValue+1);
	}
	
	public int getRandomPitchClass() {
		int pchLength = sMidiStats.getPitchClassHistogram().length;
		
		int maxCnt = 0;
		for (int i=0; i<pchLength; i++) {
			maxCnt += sMidiStats.getPitchClass(i);
		}
		int rnd = (int)(Math.random() * maxCnt);
		//LOGGER.info("mxCnt = " + maxCnt + " rnd = " + rnd);
		int val = 0;
		int pitchClass = 0;
		while(val < rnd) {
			val += sMidiStats.getPitchClass(pitchClass++);
		}
		return pitchClass;
	}
	
}
