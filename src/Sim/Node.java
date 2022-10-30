package Sim;

import java.util.LinkedList;
import java.util.Queue;

// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class Node extends SimEnt {
	private NetworkAddr homeRouter;
	protected NetworkAddr _id;
	private NetworkAddr _tempId;
	protected SimEnt _peer;
	protected int _sentmsg=0;
	protected int _seq = 0;
	
	protected LinkedList<Integer> _egressQ;

	protected int maxHops = 2;
	
	public Node (int network, int node)
	{
		super();
		_id = new NetworkAddr(network, node);
		_egressQ = new LinkedList<Integer>();
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
	
	protected int _waitForAck = 0;
	protected int _TCPSeq = 10;
	protected boolean _isSender = false;
	protected boolean _TCPStart = false;
	// Starts the client side of the TCP transmission, sends request
	public void startTCP(int network, int node, int startSeq) {
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		send(this, new TCPStart(),0);
	}
	
	private int _timeout = 5;			// Period of time before retransmission
	private int _timeoutCounter = 0;	// How many times we have retransmitted
	// Starts the actual transmission portion of the TCP, executed by "server"
	private void startTransmission(int network, int node) {
		_toNetwork = network;
		_toHost = node;
		// Data rate hard coded for now
		_stopSendingAfter = 50;
		_timeBetweenSending = 1;
		send(this, new TCPTransmission(), 0);
	}
//**********************************************************************************	
	
	// This method is called upon that an event destined for this node triggers.
	
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof Move) {
			moveNetworks(((Move) ev).getLink());
		}
		if (ev instanceof Retransmit) {			// Check if we have gotten an ACK for the message
			if(!_isSender) {
				return;
			}
			if(_timeoutCounter == 3) {
				System.out.println("Max timeouts occured, closing TCP connection");
				_sentmsg = _stopSendingAfter;
				_isSender = false;
				send(_peer, new TCPMsg(_id, new NetworkAddr(_toNetwork, _toHost), 0, 0, maxHops, 1),0);	// FIN message
			}
			if(_egressQ.contains(((Retransmit) ev).seq())) {
				System.out.println("-------------Retransmitting message with seq: " + ((Retransmit) ev).seq() + " at time: " + SimEngine.getTime());
				_timeoutCounter++;
				send(_peer, new TCPMsg(_id, new NetworkAddr(_toNetwork, _toHost), ((Retransmit) ev).seq(), 0, maxHops, 0),0);
				send(this, new Retransmit(((Retransmit) ev).seq()), _timeout);
			} else {
				_timeoutCounter = 0;
			}
		}
		if (ev instanceof TCPTransmission) {	// This is what the server uses to keep sending packets
			if(_stopSendingAfter > _sentmsg) {
				_sentmsg++;
				send(_peer, new TCPMsg(_id, new NetworkAddr(_toNetwork, _toHost), _seq, 0, maxHops, 0), 0);
				_egressQ.add(_seq);
				send(this, new Retransmit(_seq), _timeout);		// Schedule a retransmission
				send(this, new TCPTransmission(), _timeBetweenSending);
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" TCP: sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
			}
			return;
		}
		if (ev instanceof TCPStart) {
			_egressQ.add(_seq+1);
			System.out.println("Node " + _id.networkId() + "." + _id.nodeId() + " sends SYN: " + _seq);
			send(_peer, new TCPMsg(_id, new NetworkAddr(_toNetwork, _toHost), _seq, 0, maxHops, 2), 0);
		}
		if (ev instanceof TCPMsg) {
			int index = 0;
			switch(((TCPMsg) ev).flags()) {		// 1: FIN, 2: SYN, 3: RST, 4: ACK, 6: SYN+ACK
			case 0:								// Receive data message (no flags set)
				System.out.println("Node " + _id.networkId() + "." + _id.nodeId() + " receives TCP Message with seq: " + ((TCPMsg) ev).seq() + " at time " + SimEngine.getTime());
				send(_peer, new TCPMsg(_id, ((TCPMsg) ev).source(), 0, ((TCPMsg) ev).seq(), maxHops, 4), 0);	// Send ACK for the received packet
			case 1:								// Receive FIN
				break;
			case 2:								// Receive SYN (seq = x), send SYN+ACK (seq = y, ack = x + 1)
				System.out.println("Node " + _id.networkId() + "." + _id.nodeId() + " receives SYN: " + ((TCPMsg) ev).seq());
				_isSender = true;
				_egressQ.add(_TCPSeq + 1);
				send(_peer, new TCPMsg(_id, ((TCPMsg) ev).source(), _TCPSeq, ((TCPMsg) ev).seq()+1, maxHops, 6), 0);
				break;
			case 3:								// Receive RST
				break;
			case 4:								// Receive ACK
				System.out.println("Node " + _id.networkId() + "." + _id.nodeId() + " receives ACK: " + ((TCPMsg) ev).ack());
				index = _egressQ.indexOf(((TCPMsg) ev).ack());
				_egressQ.remove(index);
				if(_isSender && ((TCPMsg) ev).ack() == _TCPSeq+1) {
					System.out.println("-------------STARTING TCP TRANSMISSION-------------");
					_seq = _TCPSeq + 2;
					startTransmission(((TCPMsg) ev).source().networkId(), ((TCPMsg) ev).source().nodeId());
				}
				break;
			case 6:								// Receive SYN+ACK
				System.out.println("Node " + _id.networkId() + "." + _id.nodeId() + " receives SYN: " + ((TCPMsg) ev).seq() + " ACK:" + ((TCPMsg) ev).ack());
				index = _egressQ.indexOf(((TCPMsg) ev).ack());
				_egressQ.remove(index);
				send(_peer, new TCPMsg(_id, ((TCPMsg) ev).source(), 0, ((TCPMsg) ev).seq()+1, maxHops, 4), 0);	// Send ACK
				break;
			}
			return;
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
