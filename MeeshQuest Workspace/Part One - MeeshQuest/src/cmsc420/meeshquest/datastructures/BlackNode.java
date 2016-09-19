package cmsc420.meeshquest.datastructures;

import cmsc420.meeshquest.citymapobjects.City;

/**
 * A Black Node holds information about the city.
 * @author Mikhail Sorokin
 *
 */
public class BlackNode extends Node {

	private int[] coords;
	
	public BlackNode(int xCoord, int yCoord) {
		coords = new int[2];
		coords[0] = xCoord;
		coords[1] = yCoord;
	}
	
	public int[] getCoords() {
		return coords;
	}

	//TODO: Finish this and in all of the other Node classes!
	@Override
	protected Node add(City cityData) {
		return null;
	}

	@Override
	protected void delete() {

	}
	
}
