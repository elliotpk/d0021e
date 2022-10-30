package Sim;

public class TCPMsg extends Message{

	private int ack;
	private int flags;	// 1: FIN, 2: SYN, 3: RST, 4: ACK, 6: SYN+ACK
	
	public TCPMsg(NetworkAddr from, NetworkAddr to, int seq, int ack, int lifetime, int flags) {
		super(from, to, seq, lifetime);
		this.ack=ack;
		this.flags=flags;
	}
	
	public int ack() {
		return this.ack;
	}
	
	public int flags() {
		return this.flags;
	}
}
