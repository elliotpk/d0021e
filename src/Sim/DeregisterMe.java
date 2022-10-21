package Sim;

public class DeregisterMe extends Message{

	public DeregisterMe(NetworkAddr from, NetworkAddr to, int seq, int ttl) {
		super(from,to,seq,ttl);
	}
}
