package cmsc420.meeshquest.datastructures;

import cmsc420.meeshquest.citymapobjects.City;

public abstract class Node {

	protected Node quadrantOne, quadrantTwo, quadrantThree, quadrantFour;
	protected City data;
	
	public Node() {
		quadrantOne = null;
		quadrantTwo = null;
		quadrantThree = null;
		quadrantFour = null;
		data = null;
	}
	
}
