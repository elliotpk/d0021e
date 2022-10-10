package Sim;

// This class implements a simple router

public class Router extends SimEnt{

	private RouteTableEntry [] _routingTable;
	private int _interfaces;
	private int _now=0;

	// When created, number of interfaces are defined
	
	Router(int interfaces)
	{
		_routingTable = new RouteTableEntry[interfaces];
		_interfaces=interfaces;
	}
	
	// Prints out the routing table into the console
	
	public void printRoutingTable() {
		for(int i=0; i<_interfaces; i++) {
			if(_routingTable[i] != null) {
				System.out.println("Interface: " + i + " Node: " + ((Node) _routingTable[i].node()).getAddr().networkId() 
						+ "." + ((Node) _routingTable[i].node()).getAddr().nodeId() );
			} else {
				System.out.println("Interface: " + i + " Node: #.#");
			}
		}
	}
	
	// This method connects links to the router and also informs the 
	// router of the host connects to the other end of the link
	
	public void connectInterface(int interfaceNumber, SimEnt link, SimEnt node)
	{
		if (interfaceNumber<_interfaces)
		{
			_routingTable[interfaceNumber] = new RouteTableEntry(link, node);
		}
		else
			System.out.println("Trying to connect to port not in router");
		
		((Link) link).setConnector(this);
	}
	
	// Helper function to clear the routing table of a specified interface
	
	private void clearInterface(int interfaceNumber) {
		_routingTable[interfaceNumber] = null;
	}
	
	// Method to search for the node in the routing table and move it to the requested interface
	
	private void changeInterface(int interfaceNumber, int networkAddress) {
		if(_routingTable[interfaceNumber] != null) {
			System.out.println("Interface is already occupied");
			return;
		}
		RouteTableEntry temp = null;
		for(int i=0; i<_interfaces; i++) // Finds the node in the routing table and swaps it to the new position (if not occupied)
		{
			if (_routingTable[i] != null)
			{
				if (((Node) _routingTable[i].node()).getAddr().networkId() == networkAddress)
				{
					temp = _routingTable[i];
					clearInterface(i);
					_routingTable[interfaceNumber] = temp;
				}
			}
		}
		printRoutingTable();
	}

	// This method searches for an entry in the routing table that matches
	// the network number in the destination field of a messages. The link
	// represents that network number is returned
	
	private SimEnt getInterface(int networkAddress)
	{
		SimEnt routerInterface=null;
		for(int i=0; i<_interfaces; i++)
			if (_routingTable[i] != null)
			{
				if (((Node) _routingTable[i].node()).getAddr().networkId() == networkAddress)
				{
					routerInterface = _routingTable[i].link();
				}
			}
		if (routerInterface == null) {
			System.out.println("Interface not found");
		}
		return routerInterface;
	}
	
	
	// When messages are received at the router this method is called
	
	public void recv(SimEnt source, Event event)
	{
		if (event instanceof Message)
		{
			System.out.println("--Router handles packet with seq: " + ((Message) event).seq()+" from node: "+((Message) event).source().networkId()+"." + ((Message) event).source().nodeId() );
			SimEnt sendNext = getInterface(((Message) event).destination().networkId());
			System.out.println("--Router sends to node: " + ((Message) event).destination().networkId()+"." + ((Message) event).destination().nodeId());		
			send (sendNext, event, _now);
	
		}
		if (event instanceof InterfaceChangeEvent) // A node has requested an interface change
		{
			System.out.println("Interface change requested from node: " + ((InterfaceChangeEvent) event).getOldInterface().networkId() + "." + ((InterfaceChangeEvent) event).getOldInterface().nodeId() +
					" to interface #" + ((InterfaceChangeEvent) event).getNewInterface() + " At time: " + SimEngine.getTime());
			changeInterface(((InterfaceChangeEvent) event).getNewInterface(), ((InterfaceChangeEvent) event).getOldInterface().networkId());
		}
	}
}
