package Sim;

// An example of how to build a topology and starting the simulation engine

public class Run {
	public static void main (String [] args)
	{
 		//Creates two links
 		LossyLink link1 = new LossyLink(0, 0, 25);
		LossyLink link2 = new LossyLink(0, 0, 0);
		
		// Create two end hosts that will be
		// communicating via the router
		//Node host1 = new Node(1,1);
		//CBRGen host1 = new CBRGen(1,1);
		GaussGen host1 = new GaussGen(1,1);
		//PoissonGen host1 = new PoissonGen(1,1);
		//Node host2 = new Node(2,1);

		//Connect links to hosts
		host1.setPeer(link1);
		//host2.setPeer(link2);
		
		Sink sink1 = new Sink(2,2,"sink1.txt");
		sink1.setPeer(link2);

		// Creates as router and connect
		// links to it. Information about 
		// the host connected to the other
		// side of the link is also provided
		// Note. A switch is created in same way using the Switch class
		Router routeNode = new Router(2);
		routeNode.connectInterface(0, link1, host1);
		//routeNode.connectInterface(1, link2, host2);
		routeNode.connectInterface(0, link2, sink1);
		
		// Generate some traffic
		// host1 will send 3 messages with time interval 5 to network 2, node 1. Sequence starts with number 1
		host1.StartSendingGauss(2, 2, 2000, 50, 8, 1);
		
		// host2 will send 2 messages with time interval 10 to network 1, node 1. Sequence starts with number 10
		//host2.StartSending(1, 1, 2, 10, 10); 
		
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
