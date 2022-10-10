package Sim;

// An example of how to build a topology and starting the simulation engine
// Now also containing examples for running the implemented code from the lab(s)!

public class Run {
	public static void main (String [] args)
	{
		// Lab - 1
		/*
		// Create 2 lossy links
		LossyLink link1 = new LossyLink(0, 0, 25);	//Delay, jitter, loss (1-100%)
		LossyLink link2 = new LossyLink(0, 0, 0);
		// Create 2 hosts to communicate 
		Node host1 = new Node(1,1);
		Node host2 = new Node(2,1);
		// Connect them to the links
		host1.setPeer(link1);
		host2.setPeer(link2);
		// Setup the routing table for the hosts/links
		Router routeNode = new Router(2);
		routeNode.connectInterface(0, link1, host1);
		routeNode.connectInterface(1, link2, host2);
		// Start sending data
		host1.StartSending(1, 2, 10, 1, 1); // Network, node, number of messages, time interval, seq.start
		*/
		
		// Lab - 2
		/*
		// Create 2 lossy links
		LossyLink link1 = new LossyLink(0, 0, 0);	//Delay, jitter, loss (1-100%)
		LossyLink link2 = new LossyLink(0, 0, 0);
		// Create 1 host/sink to recieve data 
		Sink sink1 = new Sink(2,1,"Sink");
		// Create generator of choice
		//CBRGen host1 = new CBRGen(1,1);
		GaussGen host1 = new GaussGen(1,1);
		//PoissonGen host1 = new PoissonGen(1,1);
		// Connect them to the links
		sink1.setPeer(link1);
		host1.setPeer(link2);
		// Setup the routing table for the hosts/links
		Router routeNode = new Router(2);
		routeNode.connectInterface(0, link1, sink1);
		routeNode.connectInterface(1, link2, host1);
		// Start sending data (depending on generator)
		//host1.StartSending(1, 2, 10, 1, 1); // Network, node, number of messages, time interval, seq.start
		host1.StartSendingGauss(1, 2, 500, 20, 2, 1); // Network, node, transmit time, avg.rate, std.dev, seq.start
		//host1.StartSendingPoisson(1, 2, 500, 20, 1); // Network, node, transmit time, avg.rate, seq.start
		*/
		
		// Lab - 3
		
		// Setup links
		Link link1 = new Link();
		Link link2 = new Link();
		// Setup hosts
		Node host1 = new Node(1,1);
		Node host2 = new Node(2,1);
		// Connect hosts to links
		host1.setPeer(link1);
		host2.setPeer(link2);
		// Create router
		Router routeNode = new Router(4);
		// Connect the hosts and links to the router and print the routing table
		routeNode.connectInterface(0, link1, host1);
		routeNode.connectInterface(1, link2, host2);
		routeNode.printRoutingTable();
		// Schedule an interface change to #3 at a specified time
		host1.scheduleChange(3, 10);
		// Start sending some data to the other host
		host2.StartSending(1, 1, 10, 2, 1);
		
		// Start the simulation engine and of we go!
		Thread t=new Thread(SimEngine.instance());
		
		t.start();
		try
		{
			t.join();
		}
		catch (Exception e)
		{
			System.out.println("The motor seems to have a problem, time for service?");
		}		

	}
}
