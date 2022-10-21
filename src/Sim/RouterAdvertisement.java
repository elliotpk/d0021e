package Sim;

public class RouterAdvertisement extends Message {

	private NetworkAddr addr;
	
	public RouterAdvertisement(NetworkAddr from, NetworkAddr to, int seq, NetworkAddr address) {
		super(from,to,seq, 0);
		this.addr = address;
	}
	
	public NetworkAddr getAddr() {
		return this.addr;
	}
}
