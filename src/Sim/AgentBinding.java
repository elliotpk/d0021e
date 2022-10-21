package Sim;

public class AgentBinding {

	private NetworkAddr HoA;
	private NetworkAddr CoA;
	
	public AgentBinding(NetworkAddr from, NetworkAddr to) {
		this.HoA = from;
		this.CoA = to;
	}

	public NetworkAddr getHoA() {
		return HoA;
	}

	public NetworkAddr getCoA() {
		return CoA;
	}
	
}
