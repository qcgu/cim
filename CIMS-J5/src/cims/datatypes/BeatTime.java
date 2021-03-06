package cims.datatypes;

import static cims.supervisors.SupervisorMidi_Globals.sSegmentGap;
import static cims.supervisors.SupervisorMidi_Globals.sSegmentGapDuration;


import java.util.TreeMap;

public class BeatTime {
	private TreeMap<String,Integer> transport;
	private String[] transportNames = {"bar","beat","unit","ppq","tempo","beatsPerBar","beatType","state","ticks"};
	private Long timestamp;
	private Double key;
	
	private boolean onTheBeat;
	private boolean onTheBar;
	
	public static long lastBeatTimestamp;
	public static long elapsedSinceBeat;
	public static long lastBarTimestamp;
	public static long elapsedSinceBar;
	public static long timeBetweenBeats;
	
	public BeatTime() {
		this.reset();
	}
	
	public BeatTime(Integer[] currentTransport) {
		this.reset();
		this.timestamp = System.currentTimeMillis();
		if(currentTransport!=null) {
			for(int i=0;i<transportNames.length;i++) {
				transport.put(transportNames[i], currentTransport[i]);
			}
			timeBetweenBeats = 60000/transport.get("tempo");
			if (timeBetweenBeats<1) timeBetweenBeats = 0;
			this.recalcDefaultTimings();
		}
		//On the Beat Calcs
		if((transport.get("unit")<30) || (transport.get("unit")>450)) {
			onTheBeat = true;
			lastBeatTimestamp = this.timestamp;
			elapsedSinceBeat = 0;
		}	else {
			onTheBeat = false;
			elapsedSinceBeat = this.timestamp - lastBeatTimestamp;	
		}
		//On the bar calcs
		if(((transport.get("unit")<30) || (transport.get("unit")>450)) && (transport.get("beat")==1)) {
			onTheBar = true;
			lastBarTimestamp = this.timestamp;
			elapsedSinceBar = 0;
		} else {
			onTheBar = false;
			elapsedSinceBar = this.timestamp - lastBarTimestamp;
		}
		this.key = this.makeKey();
	}
	
	public void reset() {
		this.transport = new TreeMap<String, Integer>();
		this.timestamp = Long.valueOf(0);
		for(int i=0;i<transportNames.length;i++) {
			transport.put(transportNames[i], 0);
		}
		this.onTheBeat = false;
		this.onTheBar = false;
		timeBetweenBeats = 60000/120; //Default value if transport not running
		this.timestamp = System.currentTimeMillis();
	}
	
	private Double makeKey() {
		return 1000000 + (this.transport.get("bar") * 100) + this.transport.get("beat") + (this.transport.get("unit") * 0.001);
	}
	public Double makeKey(int barNum, int beat, int subBeat) {
		this.key = 1000000 + (barNum * 100) + beat + (subBeat * 0.001);
		return this.key;
	}
	
	public void recalcDefaultTimings() {
		Float beatLength = 1000/((float)transport.get("tempo")/60);
		sSegmentGapDuration = (sSegmentGap*(beatLength.intValue()/4))-10;
	}
	
	public int getBeatsPerBar() {
		int bpb = this.transport.get("beatsPerBar").intValue();
		if (bpb<1) bpb = 4; 
		return bpb;
	}
	
	public int getDefaultDuration() {
		int dd = (Long.valueOf(timeBetweenBeats).intValue())/2;
		if (dd<50) dd = 250;
		return dd;
	}
	
	
	public TreeMap<String,Integer> getAllValues() {
		return this.transport;
	}
	
	public Integer getValueFor(String name) {
		return this.transport.get(name);
	}
	
	public String toString() {
		return "|"+transport.get("bar")+ "|"+transport.get("beat")+ "|"+transport.get("unit")+ "|"+
				"|"+transport.get("ppq")+ "|"+transport.get("tempo")+ "|"+transport.get("beatsPerBar")+ "|"+
				"|"+transport.get("beatType")+ "|"+transport.get("state")+ "|"+transport.get("ticks")+ "|";
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isOnTheBeat() {
		return onTheBeat;
	}

	public void setOnTheBeat(boolean onTheBeat) {
		this.onTheBeat = onTheBeat;
	}

	public boolean isOnTheBar() {
		return onTheBar;
	}

	public void setOnTheBar(boolean onTheBar) {
		this.onTheBar = onTheBar;
	}

	public Double getKey() {
		return key;
	}

	public void setKey(Double key) {
		this.key = key;
	}

}
