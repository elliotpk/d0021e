package Sim;

public class RouterSolicitation extends Message{

	public RouterSolicitation(NetworkAddr from, NetworkAddr to, int seq) {
		super(from, to, seq, 1);
	}
}
