package cims.analysers;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiStatistics;

public class AnalyseMidi_Stats extends AnalyseMidi {
	
	
	private MidiStatistics midiStats; 


	public AnalyseMidi_Stats(SupervisorMidi supervisor) {
		super(supervisor);
		midiStats = new MidiStatistics();
	}

	@Override
	public void analyse() {
		midiStats.addPitch(current_message.pitch);

		this.supervisor.txtMsg("P_CUR: " + midiStats.current_pitch);
		this.supervisor.txtMsg("P_MEAN: "+ midiStats.meanPitch);
		this.supervisor.txtMsg("P_SD: "+ midiStats.deviationPitch);
		
		//Update static version of midiStats
		SupervisorMidi.sMidiStats = midiStats;

	}
}
