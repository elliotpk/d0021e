package Sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Sink extends Node {
	private NetworkAddr _id;
	private SimEnt _peer;
	private String logFile;
	
	public Sink (int network, int node, String logFile) {
		super(network, node);
		_id = new NetworkAddr(network, node);
		this.logFile = logFile;
	}
	
	public void setPeer (SimEnt peer)
	{
		_peer = peer;
		
		if(_peer instanceof Link )
		{
			 ((Link) _peer).setConnector(this);
		}
	}
	
	public NetworkAddr getAddr() {
		return _id;
	}
	
	private void log(String time, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
		writer.write(time + "\n");
		writer.close();
	}
	
	public void recv(SimEnt src, Event ev) {
		if (ev instanceof Message) {
			System.out.println("R: Sink "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
			
			try {
				log(Double.toString(SimEngine.getTime()), logFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
