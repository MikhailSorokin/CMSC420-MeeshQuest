package cmsc420.meeshquest.citymapobjects;

import java.awt.geom.Line2D;

import cmsc420.geom.Geometry2D;

public class Line implements Geometry2D {

	protected City startCity, endCity;
	
	public Line(City startCity, City endCity) {
		this.startCity = startCity;
		this.endCity = endCity;
	}

	@Override
	public int getType() {
		return Geometry2D.SEGMENT;
	}
	
	public Line2D getLine() {
		return new Line2D.Double(startCity.getPoint(), endCity.getPoint());
	}
	
	public City getStartCity() {
		return startCity;
	}
	
	public City getEndCity() {
		return endCity;
	}

	public void setStartCity(City city) {
		startCity = city;
	}
	
	public void setEndCity(City city) {
		endCity = city;
	}

}