package Sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class PoissonGen extends Node {
	private double stopTime = 0;
	private double timeBetween = 1;
	private int i = 1;
	
	private double lambda;
	
	public PoissonGen(int network, int node) {
		super(network, node);
		_id = new NetworkAddr(network, node);
	}
	
	private void log(String time, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
		writer.write(time + "\n");
		writer.close();
	}
	
	// Method called to start sending messages, destination network, node, desired stop time and sending rate
	public void StartSendingPoisson(int network, int node, int stopTime, int rate, int startSeq)
	{
		this.stopTime = stopTime;
		this.lambda = rate;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		send(this, new TimerEvent(), 0);	
	}
	
	// Method to generate Poisson distributed numbers, Knuth's algorithm
	private static int nextPoisson(double lambda) {
        double L = Math.exp(-lambda);
        Random rand = new Random();
        int k = 0;
        double p = 1;
        
        do{
            k++;
            p = p * rand.nextDouble();
        } while(p > L);
        return k-1;
	}
	
	//Function to log poisson generated numbers for validation
	public void testPoisson(int j) {
		for (int i = 0; i < j; i++) {
			try {
				log(Double.toString(nextPoisson(50)), "testing");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof TimerEvent)
		{			
			if (SimEngine.getTime() < this.stopTime)
			{	
				// Randomize the "packets" per second every 1 time unit 
				if(SimEngine.getTime() >= this.i) {
					this.i++;
					this.timeBetween = (double)1/(nextPoisson(this.lambda));
				} else if(SimEngine.getTime() == 0) {
					this.timeBetween = (double)1/(nextPoisson(this.lambda));
				}
				
				try {
					log(Double.toString(SimEngine.getTime()), "PoissonGen_Log.txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				_sentmsg++;
				send(_peer, new Message(_id, new NetworkAddr(_toNetwork, _toHost),_seq, maxHops),0);
				send(this, new TimerEvent(), this.timeBetween);
				System.out.println("S: Node "+_id.networkId()+ "." + _id.nodeId() +" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
			}
		}
		if (ev instanceof Message)
		{
			System.out.println("R: Node "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
		}
	}
}
