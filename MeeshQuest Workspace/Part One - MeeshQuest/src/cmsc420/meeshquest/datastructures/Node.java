package cmsc420.meeshquest.datastructures;

import cmsc420.meeshquest.citymapobjects.City;

public abstract class Node {
	
	protected abstract Node add(City cityData);
	protected abstract void delete();
	
}
