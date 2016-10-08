package cmsc420.meeshquest.datastructures;

import java.awt.geom.Line2D;

import cmsc420.meeshquest.citymapobjects.City;

/**
 * A Black Node holds information about the city.
 * @author Mikhail Sorokin
 *
 */
public class BlackNode extends Node {

	private int[] coords;
	private String cityName;
	private String startCityName, endCityName;
	private GreyNode parent;
	private City vertex;
	private int cardinality = 0;
	
	public BlackNode(String cityName, int xCoord, int yCoord, GreyNode greyNode) {
		this.setCityName(cityName);
		coords = new int[2];
		coords[0] = xCoord;
		coords[1] = yCoord;
		setParent(greyNode);
	}
	
	public BlackNode(String startCityName, String endCityName, Line2D.Double line, GreyNode greyNode) {
		this.startCityName = startCityName;
		this.endCityName = endCityName;
		coords = new int[4];
		coords[0] = (int) line.getX1();
		coords[1] = (int) line.getY1();
		coords[2] = (int) line.getX2();
		coords[3] = (int) line.getY2();
		setParent(greyNode);
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
	protected void delete(String cityName) {
		if (cityName.equals(this.cityName)) {
			
		}
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	public String getStartCityName() {
		return startCityName;
	}

	public void setStartCityName(String cityName) {
		this.startCityName = cityName;
	}
	
	public String getEndCityName() {
		return endCityName;
	}

	public void setEndCityName(String cityName) {
		this.endCityName = cityName;
	}

	public GreyNode getParent() {
		return parent;
	}

	public void setParent(GreyNode parent) {
		this.parent = parent;
	}

	@Override
	protected Node addPM(City cityData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Node addPM(City startCityData, City endCityData) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addVertex(City cityData) {
		this.vertex = cityData;
	}
	
	public City getVertex() {
		return vertex;
	}

	public void setCardinality(int cardinality) {
		this.cardinality  = cardinality;
	}
	
	public int getCardinality() {
		return cardinality;
	}
	
}