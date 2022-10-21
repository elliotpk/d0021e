package Sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GaussGen extends Node {
	private double timeBetween = 1;
	private double stopTime = 0;
	private int i = 1;
	
	//Additions for generating gaussian traffic
	private int meanRate;
	private int stdDev;
	private Random rand = new Random();

	public GaussGen(int network, int node) {
		super(network, node);
		_id = new NetworkAddr(network, node);
	}
	
	private void log(String time, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
		writer.write(time + "\n");
		writer.close();
	}
	
	// Method called to start sending messages, destination network, node, desired stop time and rate + std.dev for gauss distribution
	public void StartSendingGauss(int network, int node, int stopTime, int meanRate, int stdDev, int startSeq)
	{
		this.stopTime = stopTime;
		this.meanRate = meanRate;
		this.stdDev = stdDev;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		send(this, new TimerEvent(), 0);	
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
					this.timeBetween = 1/(Math.abs(rand.nextGaussian()*this.stdDev + this.meanRate));
				} else if(SimEngine.getTime() == 0) {
					this.timeBetween = 1/(Math.abs(rand.nextGaussian()*this.stdDev + this.meanRate));
				}

				try {
					log(Double.toString(SimEngine.getTime()) + ", " + _seq, "GaussGen_Log2.txt");
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
