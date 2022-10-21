package Sim;

// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class Node extends SimEnt {
	private NetworkAddr homeRouter;
	protected NetworkAddr _id;
	private NetworkAddr _tempId;
	protected SimEnt _peer;
	protected int _sentmsg=0;
	protected int _seq = 0;

	protected int maxHops = 2;
	
	public Node (int network, int node)
	{
		super();
		_id = new NetworkAddr(network, node);
	}	
	
	
	// Sets the peer to communicate with. This node is single homed
	
	public void setPeer (SimEnt peer)
	{
		_peer = peer;
		
		if(_peer instanceof Link )
		{
			 ((Link) _peer).setConnector(this);
		}
	}
	
	
	public NetworkAddr getAddr()
	{
		return _id;
	}
	
//**********************************************************************************
	// Mobile IP
	
	// Used to move the node, done by changing links. Will deregister from connected router and send a BU to HA if currently on foreign network
	private void moveNetworks(SimEnt newLink) {
		send(_peer, new DeregisterMe(_id, new NetworkAddr(0,0), 0, 1),0);
		if(this._tempId != null) {
			send(_peer, new BindingUpdate(_id, _tempId, 0, 0), 0);
			_id = _tempId;
		}
		System.out.println("Node " + _id.networkId() + "." + _id.nodeId() + " moved to another link");
		setPeer(newLink);
		solicitRouters(0);
		this._tempId = null;	// Reset our CoA when moving
	}
	
	// Used to move back to the home network, will send a registration message instead of solicitation
	public void moveHome(SimEnt newLink) {
		send(_peer, new BindingUpdate(_id, _tempId, 0, 0), 0);
		_id = _tempId;
		_tempId = null;
		setPeer(newLink);
		send(newLink, new RegisterMe(_id, homeRouter, 0, 1),0);
	}
	
	public void solicitRouters(int delay) {
		send(this, new RouterSolicitation(_id, new NetworkAddr(0, 0), 0), delay);
	}
	
	// Sets our home router
	public void setHome(SimEnt router) {
		this.homeRouter = ((Router) router).getAddr();
	}
	
//**********************************************************************************	
	// Just implemented to generate some traffic for demo.
	// In one of the labs you will create some traffic generators
	
	protected int _stopSendingAfter = 0; //messages
	protected int _timeBetweenSending = 10; //time between messages
	protected int _toNetwork = 0;
	protected int _toHost = 0;
	
	public void StartSending(int network, int node, int number, int timeInterval, int startSeq)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		send(this, new TimerEvent(),0);	
	}
	
//**********************************************************************************	
	
	// This method is called upon that an event destined for this node triggers.
	
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof Move) {
			moveNetworks(((Move) ev).getLink());
		}
		if (ev instanceof TimerEvent)
		{			
			if (_stopSendingAfter > _sentmsg)
			{
				_sentmsg++;
				send(_peer, new Message(_id, new NetworkAddr(_toNetwork, _toHost),_seq, maxHops),0);
				send(this, new TimerEvent(),_timeBetweenSending);
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
			}
		}
		if (ev instanceof RouterAdvertisement) {	// Advertisement from routers, also used to deliver CoA to the node
			if(((RouterAdvertisement) ev).source() == this.homeRouter) {	// Ignore messages from home network
				return;
			}
			if(((RouterAdvertisement) ev).getAddr() != null) {
				this._tempId = this._id;
				this._id = ((RouterAdvertisement) ev).getAddr();
				send(_peer, new BindingUpdate(_id, _tempId, 0, 1), 0);	// Send BU to home agent
				System.out.println("New address: " + _id.networkId() + "." + _id.nodeId());
				System.out.println("Old Address: " + _tempId.networkId() + "." + _tempId.nodeId());
			} else if (this._tempId == null && ((RouterAdvertisement) ev).getAddr() == null) {	// If we get advertisement without a CoA we will request it with solicitation
				send(_peer, new RouterSolicitation(_id, new NetworkAddr(0, 0), 0), 0);
			}
			return;
		}
		if (ev instanceof RouterSolicitation) {		// If we have not received a CoA we ask for it (this handles scheduling solicitation sending)
			if(this._tempId == null) {
				send(_peer, ev, 0);
			}
			return;
		}
		if (ev instanceof BindingAck) {
			if(((BindingAck) ev).getCode() == 1) {
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" Binding Ack received and approved");
			} else {
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" Binding Ack received and declined");
			}
			return;
		}
		if (ev instanceof Message)
		{
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
			return;
		}
	}
}
