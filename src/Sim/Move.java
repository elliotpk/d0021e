package Sim;

public class Move implements Event{

	private Link newLink;
	
	public Move(Link link) {
		super();
		this.newLink = link;
	}

	public void entering(SimEnt locale) {	
	}
	
	public Link getLink() {
		return this.newLink;
	}
}
