package Sim;

public class RegisterMe extends Message{
	
	public RegisterMe(NetworkAddr from, NetworkAddr to, int seq, int ttl) {
		super(from,to,seq,ttl);
	}
}
