package cims.supervisors;

public interface Supervisor {
	public void dataIn();
	public void dataOut(int[] message);
	public void txtMsg(String msg);
}
