package Sim;

public class BindingUpdate extends Message {

	private int lifetime; // 0 = Deregister agent
	
	public BindingUpdate(NetworkAddr from, NetworkAddr to, int seq, int lifetime) {
		super(from,to,seq,2);
		this.lifetime = lifetime;
	}
	
	public int getLifetime() {
		return this.lifetime;
	}
}
