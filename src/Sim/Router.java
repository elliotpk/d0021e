package Sim;

// This class implements a simple router

public class Router extends SimEnt {

	private RouteTableEntry[] _routingTable;
	private AgentBinding[] _bindingTable;
	private Link[] _linkTable;
	private int _interfaces;
	private int _now = 0;
	private NetworkAddr _routerAddr;
	private RouteTableEntry nextHop;

	// When created, number of interfaces are defined

	Router(int interfaces, int network) {
		_routingTable = new RouteTableEntry[interfaces];
		_bindingTable = new AgentBinding[interfaces];
		_linkTable = new Link[interfaces];
		_interfaces = interfaces;
		_routerAddr = new NetworkAddr(network, 0);
	}

	public NetworkAddr getAddr() {
		return this._routerAddr;
	}

	public void printRoutingTable() {
		for (int i = 0; i < _interfaces; i++) {
			if (_routingTable[i] != null) {
				System.out
						.println("Interface: " + i + " Node: " + ((NetworkAddr) _routingTable[i].address()).networkId()
								+ "." + ((NetworkAddr) _routingTable[i].address()).nodeId());
			} else {
				System.out.println("Interface: " + i + " Node: #.#");
			}
		}
	}

	// This method connects links to the router and also informs the
	// router of the host connects to the other end of the link

	public void connectInterface(int interfaceNumber, SimEnt link, NetworkAddr address) {
		if (interfaceNumber < _interfaces) {
			_routingTable[interfaceNumber] = new RouteTableEntry(link, address);
			_linkTable[interfaceNumber] = (Link) link;
			System.out.println("Router #" + this._routerAddr.networkId() + ": Interface connected to: "
					+ address.networkId() + "." + address.nodeId());
		} else
			System.out.println("Trying to connect to port not in router");

		((Link) link).setConnector(this);
	}

	public void setNextHop(SimEnt link, NetworkAddr address) {
		nextHop = new RouteTableEntry(link, address);
		((Link) link).setConnector(this);
	}
	// This method searches for an entry in the routing table that matches
	// the network address in the destination field of a messages. The link
	// represents that network address is returned

	private SimEnt getInterface(NetworkAddr networkAddress) {
		SimEnt routerInterface = null;
		if (networkAddress.networkId() == this._routerAddr.networkId()) {
			for (int i = 0; i < _interfaces; i++)
				if (_routingTable[i] != null) {
					if (((NetworkAddr) _routingTable[i].address()).networkId() == networkAddress.networkId()
							&& ((NetworkAddr) _routingTable[i].address()).nodeId() == networkAddress.nodeId()) {
						routerInterface = _routingTable[i].link();
					}
				}
		}
		if (routerInterface == null) {
			routerInterface = nextHop.link();
		}
		return routerInterface;
	}

//**********************************************************************************
	// Mobile IP

	// Finds an available ID within the network to assign to a Mobile Node
	private NetworkAddr newAddress() {
		boolean taken = true;
		int number = 0;

		while (taken) {
			number++;
			taken = false;
			for (int i = 0; i < _interfaces; i++) {
				if (_routingTable[i] != null) {
					if (((NetworkAddr) _routingTable[i].address()).nodeId() == number) {
						taken = true;
						break;
					}
				}
			}
		}
		return new NetworkAddr(this._routerAddr.networkId(), number);
	}

	// Finds an available interface in the router
	private int getNewInterface() {
		for (int i = 0; i < _interfaces; i++) {
			if (_routingTable[i] == null) {
				return i;
			}
		}
		return -1;
	}

	// Find the link matching input

	private int findLink(Link link) {
		for (int i = 0; i < _interfaces; i++) {
			if (_linkTable[i] != null) {
				if (_linkTable[i].equals(link)) {
					return i;
				}
			}
		}
		return -1;
	}

	// Finds a free slot in the binding table and makes an entry for the node
	private void addBinding(NetworkAddr from, NetworkAddr to) {
		for (int i = 0; i < _interfaces; i++) {
			if (_bindingTable[i] == null) {
				_bindingTable[i] = new AgentBinding(from, to);
				System.out.println("Router #" + this._routerAddr.networkId() + ": Added binding from "
						+ from.networkId() + "." + from.nodeId() + " to " + to.networkId() + "." + to.nodeId());
				break;
			}
		}
	}

	// Finds a node in the binding table and removes it, executed when receiving
	// lifetime = 0 in BU
	private void removeBinding(NetworkAddr from, NetworkAddr to) {
		for (int i = 0; i < _interfaces; i++) {
			if (_bindingTable[i] != null) {
				if (_bindingTable[i].getHoA().networkId() == from.networkId() && _bindingTable[i].getHoA().nodeId() == from.nodeId()) {
					if (_bindingTable[i].getCoA().networkId() == to.networkId() && _bindingTable[i].getCoA().nodeId() == to.nodeId()) {
						_bindingTable[i] = null;
						System.out.println("Router #" + this._routerAddr.networkId() + ": Removed binding from "
								+ from.networkId() + "." + from.nodeId() + " to " + to.networkId() + "." + to.nodeId());
						break;
					}
				}
			}
		}
	}

	// Function to search the bindings to check if router is home agent to the node
	// (and should tunnel the packet)
	private NetworkAddr isAgent(NetworkAddr HoA) {
		for (int i = 0; i < _interfaces; i++) {
			if (this._bindingTable[i] != null) {
				if (HoA.networkId() == this._bindingTable[i].getHoA().networkId()
						&& HoA.nodeId() == this._bindingTable[i].getHoA().nodeId()) {
					return this._bindingTable[i].getCoA();
				}
			}
		}
		return null;
	}

	private int _stopSendingAfter = 0; // How many ads to send
	private int _timeBetweenSending = 10; // Time between ads
	private int _sentMessages = 0;

	// Starts sending router advertisements, will send to every network in routing
	// table
	public void startAdvertising(int number, int timeInterval) {
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		send(this, new TimerEvent(), 0);
	}

	// Function used to send the advertisements
	private void sendAdvertisements() {
		for (int i = 0; i < _interfaces; i++) {
			if (_routingTable[i] != null) {
				send(_routingTable[i].link(),
						new RouterAdvertisement(this._routerAddr, ((NetworkAddr) _routingTable[i].address()), 0, null),
						0); // Sends advertisement of the routers existence
			}
		}
	}

	// Receives a link and address and checks if it is added to routing table
	// already, if not it is added
	private void addToTable(SimEnt link, NetworkAddr address) {
		for (int i = 0; i < _interfaces; i++) {
			if (_linkTable[i] != null) {
				if (_linkTable[i].equals(link)) {
					if (_routingTable[i] == null) {
						connectInterface(i, link, address);
						System.out.println("Router #" + this._routerAddr.networkId() + ": Node " + address.networkId()
								+ "." + address.nodeId() + " added to routing table");
					}
				}
			}
		}
	}

	// Add an a node to the routing table without setting the link peer to this
	// (used when the node already has a connection to the link)
	private void addEntry(int index, NetworkAddr address) {
		_routingTable[index] = new RouteTableEntry(_linkTable[index], address);
	}

	// Removes a specified address and link from the routing table
	private void removeFromTable(SimEnt link, NetworkAddr address) {
		for (int i = 0; i < _interfaces; i++) {
			if (_routingTable[i] != null) {
				if (_routingTable[i].address().networkId() == address.networkId()
						&& _routingTable[i].address().nodeId() == address.nodeId()) {
					if (link.equals(_routingTable[i].link())) {
						_routingTable[i] = null;
						System.out.println("Router #" + this._routerAddr.networkId() + ": Removed node "
								+ address.networkId() + "." + address.nodeId() + " from routing table");
					}
				}
			}
		}
	}

	public void addLink(int interfaceNumber, Link link) {
		if (interfaceNumber < _interfaces) {
			if (_linkTable[interfaceNumber] == null) {
				System.out.println("Link added");
				_linkTable[interfaceNumber] = link;
				link.setConnector(this);
			} else {
				System.out.println("This interface already has a link associated.");
			}
		} else {
			System.out.println("Trying to add link to port not in router");
		}
	}

//**********************************************************************************
	// Message/Event reception

	// When messages are received at the router this method is called

	public void recv(SimEnt source, Event event) {
		// Timer section for automatic sending of advertisements
		if (event instanceof TimerEvent) {
			if (_stopSendingAfter > _sentMessages) {
				_sentMessages++;
				sendAdvertisements();
				send(this, new TimerEvent(), _timeBetweenSending);
			}
		}

		// Message section begins here

		if (((Message) event).ttl() == 0) {
			System.out.println("Router #" + this._routerAddr.networkId() + ": Time to live reached 0, dropping packet");
			return;
		} else {
			((Message) event).decTTL();
		}
		if (event instanceof RouterSolicitation) // Respond to solicitation with a new CoA and adds it to routing table
		{
			if(((RouterSolicitation) event).source().networkId() == _routerAddr.networkId()) {
				System.out.println("Node has returned to home network.");
				return;
			}
			System.out.println("Router #" + this._routerAddr.networkId() + ": Solicitation received");
			NetworkAddr CoA = newAddress();
			int interfaceNumber = findLink((Link) source);
			if (interfaceNumber == -1) {
				System.out.println("Interfaces are full");
				return;
			}
			send(_linkTable[interfaceNumber],
					new RouterAdvertisement(_routerAddr, ((RouterSolicitation) event).source(), 0, CoA), 0);
			addEntry(interfaceNumber, CoA);
			return;
		}
		if (event instanceof BindingUpdate && ((Message) event).destination().networkId() == this._routerAddr.networkId()) {
			System.out.println("Router #" + this._routerAddr.networkId() + ": Binding update received from CoA: "
					+ ((Message) event).source().networkId() + "." + ((Message) event).source().nodeId() + " to HoA: "
					+ ((Message) event).destination().networkId() + "." + ((Message) event).destination().nodeId());
			BindingUpdate temp = (BindingUpdate) event;
			if (temp.getLifetime() != 0) {
				addBinding(temp.destination(), temp.source()); // Adds the binding to this router, destination = HoA,
																// source = CoA
			} else {
				removeBinding(temp.destination(), temp.source()); // Remove the binding from this router
				return;
			}
			SimEnt sendTo = getInterface(((Message) event).source());
			send(sendTo, new BindingAck(_routerAddr, temp.source(), temp.seq(), 1), 0);
			System.out.println("Router #" + this._routerAddr.networkId() + ": Sending binding ACK");
			return;
		}
		if (event instanceof RegisterMe) {
			addToTable(source, ((RegisterMe) event).source());
			return;
		}
		if (event instanceof DeregisterMe) {
			((Link) source).clearConnector(this);
			removeFromTable(source, ((DeregisterMe) event).source());
			return;
		}
		if (event instanceof Message) {
			System.out.println("Router #" + _routerAddr.networkId() + ": Handles packet with seq: "
					+ ((Message) event).seq() + " from node: " + ((Message) event).source().networkId() + "."
					+ ((Message) event).source().nodeId());
			NetworkAddr newAddr = isAgent(((Message) event).destination());
			SimEnt sendNext;
			if (newAddr != null) {
				Message oldMsg = (Message) event;
				Message newMsg = new Message(oldMsg.source(), newAddr, oldMsg.seq(), oldMsg.ttl()); // Change the
																									// destination to
																									// the CoA
				sendNext = getInterface(newMsg.destination());
				System.out.println("Router #" + this._routerAddr.networkId() + ": Tunnels to node: "
						+ newMsg.destination().networkId() + "." + newMsg.destination().nodeId());
				send(sendNext, newMsg, _now);
			} else {
				sendNext = getInterface(((Message) event).destination());
				System.out.println("Router #" + this._routerAddr.networkId() + ": Sends to node: "
						+ ((Message) event).destination().networkId() + "." + ((Message) event).destination().nodeId());
				send(sendNext, event, _now);
			}
		}
	}
}
