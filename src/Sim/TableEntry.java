package Sim;

// Just a class that works like a table entry hosting
// a link connecting and the node at the other end

public class TableEntry {

	private SimEnt _link;
	private NetworkAddr _address;
	
	TableEntry(SimEnt link, NetworkAddr address)
	{
		_link=link;
		_address=address;
	}
	
	protected SimEnt link()
	{
		return _link;
	}

	protected NetworkAddr address()
	{
		return _address;
	}
	
}
