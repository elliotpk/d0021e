package Sim;

// This class represent a routing table entry by including
// the link connecting to an interface as well as the node 
// connected to the other side of the link

public class RouteTableEntry extends TableEntry{

	RouteTableEntry(SimEnt link, NetworkAddr address)
	{
		super(link, address);
	}
	
	public SimEnt link()
	{
		return super.link();
	}

	public NetworkAddr node()
	{
		return super.address();
	}
	
}
