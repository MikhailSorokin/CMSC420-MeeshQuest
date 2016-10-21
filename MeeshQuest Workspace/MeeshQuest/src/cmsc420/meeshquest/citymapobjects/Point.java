package cmsc420.meeshquest.citymapobjects;

import java.awt.geom.Point2D;

import cmsc420.geom.Geometry2D;

public class Point implements Geometry2D {

	protected City singleCity;
	
	public Point(City city) {
		singleCity = city;
	}

	@Override
	public int getType() {
		return Geometry2D.POINT;
	}
	
	public Point2D getPoint() {
		return singleCity.getPoint();
	}
	
	public City getCity() {
		return singleCity;
	}

}
