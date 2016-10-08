package cmsc420.meeshquest.datastructures;

import cmsc420.meeshquest.citymapobjects.City;

public abstract class Node {
	
	protected int[] data = new int[2];
	
	protected abstract Node add(City cityData);
	protected abstract Node addPM(City cityData);
	protected abstract Node addPM(City startCityData, City endCityData);
	protected abstract void delete(String name);
	
}
