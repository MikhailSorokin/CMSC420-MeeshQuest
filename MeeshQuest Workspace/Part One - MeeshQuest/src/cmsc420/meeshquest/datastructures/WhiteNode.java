package cmsc420.meeshquest.datastructures;

import cmsc420.meeshquest.citymapobjects.City;

/**
 * This is a singleton class which extends
 * an abstract class Node. S WhiteNode represents an empty
 * node.
 * @author Mikhail Sorokin
 *
 */
public class WhiteNode extends Node {

	public static WhiteNode singleton;
	
	public WhiteNode() {
		singleton = this;
	}

	@Override
	protected Node add(City cityData) {
    	/*if (this == null) {
    		return singleton
    	} else if (cityData.getX() < node.getCoords()[0] && cityData.getY() > node.getCoords()[1]) {
    		node.quadrantOne = insert(cityData, node.quadrantOne);
    	} else if (cityData.getX() > node.getCoords()[0] && cityData.getY() > node.getCoords()[1]) {
    		node.quadrantTwo = insert(cityData, node.quadrantTwo);
    	} else if (cityData.getX() < node.getCoords()[0] && cityData.getY() < node.getCoords()[1]) {
    		node.quadrantThree = insert(cityData, node.quadrantThree);
    	} else if (cityData.getX() > node.getCoords()[0] && cityData.getY() < node.getCoords()[1]) { {
    		node.quadrantFour = insert(cityData, node.quadrantFour);
    	}
    	return node;*/
		return null;
	}

	@Override
	protected void delete() {

	}
	
}
