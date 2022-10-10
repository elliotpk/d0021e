package Sim;

// New event class for scheduling interface changes
// Requires the nodes address and new interface
public class InterfaceChangeEvent implements Event{
	private NetworkAddr oldId;
	private int newInterface;
	
	public InterfaceChangeEvent(NetworkAddr oldId, int newI) {
		this.oldId = oldId;
		this.newInterface = newI;
	}
	
	public NetworkAddr getOldInterface() {
		return oldId;
	}
	
	public int getNewInterface() {
		return newInterface;
	}
	
	public void entering(SimEnt locale)
	{
	}
}
