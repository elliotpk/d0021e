package Sim;

import java.util.Random;

// Extends Link class adding functionality for delay, jitter and packet loss parameters

public class LossyLink extends Link {
	private float delay = 0;
	private float jitter = 0;
	private float loss = 0;
	
	public LossyLink(float delay, float jitter, float loss) {  //Loss specified in % (0-100)
		super();
		this.delay = delay;
		this.jitter = jitter;
		this.loss = loss/100;
	}
	
	public void recv(SimEnt src, Event ev) {
		if (ev instanceof Message)
		{
			Random rand = new Random();
			double wait;
			do {
				wait = this.delay + rand.nextGaussian()*this.jitter;		//Shift the mean to delay and standard deviation to jitter
			} while(wait < 0);
			
			if(rand.nextFloat() < loss) {
				System.out.println("!!Link recv msg but dropped it!!");
			} else {
				//System.out.println("Link recv msg, passes it through");
				if (src == _connectorA)
				{
					send(_connectorB, ev, wait);
				}
				else
				{
					send(_connectorA, ev, wait);
				}
			}
		}
	}
}
