package Sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CBRGen extends Node {
	private double timeBetween = 1;
	private double stopTime = 0;

	public CBRGen(int network, int node) {
		super(network, node);
		_id = new NetworkAddr(network, node);
	}
	
	private void log(String time, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
		writer.write(time + "\n");
		writer.close();
	}
	
	//Modified from Node to specify package send rate and stop time instead of discrete package sending
	public void StartSending(int network, int node, int stopTime, int rate, int startSeq)
	{
		this.stopTime = stopTime;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		this.timeBetween = 1/(double)rate;
		send(this, new TimerEvent(), 0);	
	}
	
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof TimerEvent)
		{			
			if (SimEngine.getTime() < this.stopTime)
			{
				try {
					log(Double.toString(SimEngine.getTime()), "CBRGen_Log.txt");
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
