package cims.v03;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_Note;
import cims.generators.GeneratorMidi;
import cims.supervisors.SupervisorMidi;

public class Generator03 extends GeneratorMidi {
	
	private double mirroringType = 0;
	private boolean firstAction = true;

	private GenerateMidi_Note generator_note;
	private GenerateMidi_Loop generator_support;
	private GenerateMidi_Loop generator_initiate;
	private GenerateMidi_Segment_03 generator_repeatSegment;
	private GenerateMidi_Segment_03 generator_supportSegment;
	private GenerateMidi_Segment_03 generator_initiateSegment;
	
	public static Logger LOGGER = Logger.getLogger(Generator03.class);
	
	public Generator03(SupervisorMidi supervisor) {
		super(supervisor);
		this.generator_note = new GenerateMidi_Note(supervisor);
		this.generator_repeatSegment = new GenerateMidi_Segment_03(supervisor);
		this.generator_supportSegment = new GenerateMidi_Segment_03(supervisor);
		this.generator_support = new GenerateMidi_Loop(this.generator_supportSegment);
		this.generator_initiateSegment = new GenerateMidi_Segment_03(supervisor);
		this.generator_initiate = new GenerateMidi_Loop(this.generator_initiateSegment);
		LOGGER.setLevel(Level.INFO);
	}

	@Override
	public void silence() {
		// TODO Auto-generated method stub

	}

	@Override
	public void repeat() {
		generator_repeatSegment.makeLastSegment();
		generator_repeatSegment.generate(sNextPlay);

	}

	@Override
	public void mirror() {
		LOGGER.debug("mirror");
		generator_note.setMessage(getMirror_message());
		mirroringType = Math.random();
		LOGGER.debug("mirroringType: "+mirroringType);
		if (this.mirroringType < 0.3){ // select one of two mirroring processes at random
			generator_note.transform(GenerateMidi_Note.PITCH_SHIFT, 12);
			LOGGER.debug("completed transform - PITCH_SHIFT");
		} else if (mirroringType < 0.7) {
			generator_note.transform(GenerateMidi_Note.LOWER_TRIADIC, 0);
			LOGGER.debug("completed transform - LOWER_TRIADIC");
		} else {
			generator_note.transform(GenerateMidi_Note.PARALLEL_INTERVAL, 3);
			LOGGER.debug("completed transform - PARALLEL_INTERVAL");
		}
		
		generator_note.output();
	}

	@Override
	public void support() {
		int segmentLength = 0;
		if(firstAction) {
			segmentLength = generator_supportSegment.firstSupportSegment(this.getFirst_message());
			firstAction = false;
		} else {
			segmentLength = generator_supportSegment.supportSegment();
		}
		generator_support = new GenerateMidi_Loop(generator_supportSegment);
		generator_support.setInterval(segmentLength);
		
		generator_support.start();

	}
	
	@Override
	public void supportStop() {
		if(!(generator_support==null)) generator_support.stop();

	}

	@Override
	public void initiate() {
		int segmentLength = generator_initiateSegment.initiateSegment();
		generator_initiate = new GenerateMidi_Loop(generator_initiateSegment);
		generator_initiate.setInterval(segmentLength);
		generator_initiate.start();
		// clear out pitch histogram memory
		sMidiStats.clearPitchHistogram();

	}

	@Override
	public void initiateStop() {
		if(!(generator_initiate==null)) generator_initiate.stop();

	}
	
	public boolean supportHasStarted() {
		return this.generator_support.hasStarted;
	}



	
	/****
	 * WORKER METHODS
	 * 
	 */
	

}
