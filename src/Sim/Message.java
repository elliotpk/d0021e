package Sim;

// This class implements an event that send a Message, currently the only
// fields in the message are who the sender is, the destination and a sequence 
// number

public class Message implements Event{
	private NetworkAddr _source;
	private NetworkAddr _destination;
	private int _seq=0;
	private int _TTL;
	
	Message (NetworkAddr from, NetworkAddr to, int seq, int ttl)
	{
		_source = from;
		_destination = to;
		_seq=seq;
		_TTL = ttl;
	}
	
	public NetworkAddr source()
	{
		return _source; 
	}
	
	public NetworkAddr destination()
	{
		return _destination; 
	}
	
	public int seq()
	{
		return _seq; 
	}
	
	public int ttl() {
		return _TTL;
	}
	
	public void decTTL() {
		_TTL -= 1;
	}

	public void entering(SimEnt locale)
	{
	}
}
	
