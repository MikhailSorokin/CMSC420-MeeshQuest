package cmsc420.meeshquest.datastructures;

import cmsc420.meeshquest.citymapobjects.City;

/**
 * This is a singleton class which extends
 * an abstract class Node. A GreyNode represents a
 * coordinate link to four more nodes.
 * @author Mikhail Sorokin
 *
 */
public class GreyNode extends Node {
	
	public Node quadrantOne, quadrantTwo, quadrantThree, quadrantFour;
	private int[] coords;
	
	public GreyNode(int xCoord, int yCoord) {
		coords = new int[2];
		coords[0] = xCoord;
		coords[1] = yCoord;
		quadrantOne = quadrantTwo = quadrantThree = quadrantFour = WhiteNode.singleton;
	}
	
	public int[] getCoords() {
		return coords;
	}


	@Override
	protected Node add(City cityData) {
    	/*if (coords[0] <= 1 && coords[0] <= 1) {
    		return new BlackNode((int)cityData.getX(), (int)cityData.getY());
    	} else if (node == null) {
    		return new GreyNode(node.getX(), node.getY());
    	} else if (cityData.getX() < this.getCoords()[0] && cityData.getY() > this.getCoords()[1]) {
    		this.quadrantOne = add(cityData, node);
    	} else if (cityData.getX() > this.getCoords()[0] && cityData.getY() > this.getCoords()[1]) {
    		this.quadrantTwo = add(cityData, node);
    	} else if (cityData.getX() < this.getCoords()[0] && cityData.getY() < this.getCoords()[1]) {
    		this.quadrantThree = add(cityData, node);
    	} else if (cityData.getX() > this.getCoords()[0] && cityData.getY() < this.getCoords()[1]) {
    		this.quadrantFour = add(cityData, node);
    	}
    	return this;*/
		return null;
	}

	@Override
	protected void delete() {

	}
	
}
