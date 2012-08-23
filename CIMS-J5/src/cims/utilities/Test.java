package cims.utilities;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;

public class Test {
	
	private SupervisorMidi sm;

	public Test(SupervisorMidi supervisor) {
		sm = supervisor;
	}
	
	public GenerateMidi_Loop generateMidi_Loop() {
		sm.txtMsg("TST: generateMidi_Loop");
		GenerateMidi_Segment gm_segment = this.generateMidi_Segment(false);
		GenerateMidi_Loop gm_loop = new GenerateMidi_Loop(gm_segment);
		return gm_loop;
	}
	
	public GenerateMidi_Segment generateMidi_Segment(boolean generate) {
		sm.txtMsg("TST: generateMidi_Segment");
		MidiSegment segment = this.midiSegment();
		GenerateMidi_Segment gm_segment = new GenerateMidi_Segment(sm,segment);
		if(generate) {
			gm_segment.generate();
		}
		return gm_segment;
	}
	
	public MidiSegment midiSegment() {
		sm.txtMsg("TST: MidiSegment");
		MidiSegment segment = new MidiSegment();
		
		MidiMessage noteOn = this.midiMessage(1);
		MidiMessage noteOff = this.midiMessage(0);
		
		segment.add(noteOn);
		segment.add(noteOff);
		
		return segment;
	}
	
	public MidiMessage midiMessage(int onOff) {
		sm.txtMsg("TST: MidiMessage");
		MidiMessage message = new MidiMessage();
		switch(onOff) {
		case 1:
			int[] noteOn = {MidiMessage.NOTE_ON,64,100};
			message.set(noteOn, false);
			message.timeMillis = 0;
			break;
		case 0:
			int[] noteOff = {MidiMessage.NOTE_OFF,64,0};
			message.set(noteOff, false);
			message.timeMillis = 2000;
			break;
			default:
				// do nothing
		}
		return message;
	}
	

}
