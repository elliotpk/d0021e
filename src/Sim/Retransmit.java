package Sim;

public class Retransmit implements Event {
	
	private int seq;
	public Retransmit(int seq) {
		this.seq = seq;
	}
	public void entering(SimEnt locale) {
	}
	
	public int seq() {
		return this.seq;
	}
}
