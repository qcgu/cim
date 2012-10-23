package cims.datatypes;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
//import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class MidiSegmentTable {
	
	private HashMap<String,TreeMap<Long, MidiSegment>> segmentsMap;
	private HashMap<String,TreeMap<Long, MidiSegment>> breakPointSegmentsMap;
	
	public MidiSegmentTable() {
		segmentsMap = new HashMap<String,TreeMap<Long, MidiSegment>>();
	}

	 public void add(MidiSegment segment) {
		String className = segment.getCreatorClass().getName();

		if (segmentsMap.containsKey(className)) {
			this.segmentsMap.get(className).put(segment.firstMessage().timeMillis,segment);
		} else {
			TreeMap<Long, MidiSegment> newSegmentMap = new TreeMap<Long, MidiSegment>();
			newSegmentMap.put(segment.firstMessage().timeMillis, segment);
			this.segmentsMap.put(className, newSegmentMap);
		}	
	}
	
	public void addBreakPoint(long timestamp,String className) {
/*
		if (breakPointSegmentsMap.containsKey(className)) {
			this.segmentsMap.get(className).put(segment.firstMessage().timeMillis,segment);
		} else {
			TreeMap<Long, MidiSegment> newSegmentMap = new TreeMap<Long, MidiSegment>();
			newSegmentMap.put(segment.firstMessage().timeMillis, segment);
			this.segmentsMap.put(className, newSegmentMap);
		}
		*/
	}
	 
	public HashMap<String,TreeMap<Long, MidiSegment>> getAllSegments() {
		return this.segmentsMap;
	}
	
	public TreeMap<Long, MidiSegment> getSegmentsForClassName(String className) {
		return this.segmentsMap.get(className);
	}
	
	public MidiSegment getSegment (String className, long timestamp) {
		return this.segmentsMap.get(className).get(timestamp);
	}
	


}
