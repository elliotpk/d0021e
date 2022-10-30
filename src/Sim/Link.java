package Sim;

// This class implements a link without any loss, jitter or delay

public class Link extends SimEnt{
	protected SimEnt _connectorA=null;
	protected SimEnt _connectorB=null;
	protected int _now=0;
	
	public Link()
	{
		super();	
	}
	
	// Connects the link to some simulation entity like
	// a node, switch, router etc.
	
	public void setConnector(SimEnt connectTo)
	{
		if (_connectorA == null) 
			_connectorA=connectTo;
		else
			_connectorB=connectTo;
	}
	
	public void clearConnector(SimEnt disconnectFrom) {
		if(!_connectorA.equals(disconnectFrom)) {
			_connectorA = null;
		} else if(!_connectorB.equals(disconnectFrom)) {
			_connectorB = null;
		} else {
			System.out.println("Entity not connected to this link");
		}
	}

	// Called when a message enters the link
	
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof Message  || ev instanceof BindingUpdate || ev instanceof BindingAck || ev instanceof RouterAdvertisement || ev instanceof RouterSolicitation || ev instanceof TCPMsg)
		{
			if (src == _connectorA)
			{
				send(_connectorB, ev, _now);
			}
			else
			{
				send(_connectorA, ev, _now);
			}
		}
	}	
}