package Sim;

public class BindingAck extends Message {

	private int code;	// 0=decline, 1=accept
	
	public BindingAck(NetworkAddr from, NetworkAddr to, int seq, int code) {
		super(from,to,seq,2);
		this.code=code;
	}
	
	public int getCode() {
		return this.code;
	}
}
