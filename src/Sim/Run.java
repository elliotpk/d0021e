package Sim;

// An example of how to build a topology and starting the simulation engine

public class Run {
	public static void main (String [] args)
	{
		
		Link link1 = new Link();
		Link link2 = new Link();
		Link link3 = new Link();
		Link routerLink = new Link();
		
		Router routeNode1 = new Router(4, 1);
		Router routeNode2 = new Router(4, 2);
		
		Node host1 = new Node(1,1);
		Node host2 = new Node(1,2);

		// Connect hosts to links and router #1, set home router for hosts
		host1.setPeer(link1);
		host1.setHome(routeNode1);
		host2.setPeer(link2);
		host2.setHome(routeNode1);
		routeNode1.connectInterface(0, link1, host1.getAddr());
		routeNode1.connectInterface(1, link2, host2.getAddr());
		
		// Connect the routers together
		routeNode1.setNextHop(routerLink, routeNode2.getAddr());
		routeNode2.setNextHop(routerLink, routeNode2.getAddr());
		
		// Connect an empty link to router #2
		routeNode2.addLink(0, link3);
		
		// Schedule host2 to move to router #2 after 20 timeunits
		host2.send(host2, new Move(link3), 10);
		
		// Start sending data from host1 to host2, this will coincide with host2 becoming mobile
		host1.StartSending(1, 2, 50, 1, 1);
		
		// Move host2 back to home router/network
		host2.send(host2, new Move(link2), 30);
		host2.send(link2, new RegisterMe(host2.getAddr(), routeNode1.getAddr(), 1, 1), 40);
		
		
		/*
		LossyLink link1 = new LossyLink(0,0,25);
		LossyLink link2 = new LossyLink(0,0,0);
		
		Router routeNode1 = new Router(4,1);
		Node host1 = new Node(1,1);
		Node host2 = new Node(1,2);
		host1.setPeer(link1);
		host2.setPeer(link2);
		routeNode1.connectInterface(0, link1, host1.getAddr());
		routeNode1.connectInterface(1, link2, host2.getAddr());
		
		host1.startTCP(1, 2, 1);
		*/
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
