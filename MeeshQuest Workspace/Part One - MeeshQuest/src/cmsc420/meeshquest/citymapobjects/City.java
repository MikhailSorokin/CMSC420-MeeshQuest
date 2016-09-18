package cmsc420.meeshquest.citymapobjects;

import java.awt.geom.Point2D;

public class City extends Point2D {

	private String name;
	private Point2D.Float coordinates;
	private int radius;
	private String color;
	
	public City(String name, int x, int y, int radius, String color) {
		this.name = name;
		this.coordinates = new Point2D.Float(x, y);
		this.radius = radius;
		this.color = color;
	}

	@Override
	public double getX() {
		return coordinates.x;
	}

	@Override
	public double getY() {
		return coordinates.y;
	}

	@Override
	public void setLocation(double x, double y) {
		coordinates.x = (float) x;
		coordinates.y = (float) y;
	}
	
	public String getName() {
		return name;
	}

	public Point2D.Float getCoordinates() {
		return coordinates;
	}

	public int getRadius() {
		return radius;
	}

	public String getColor() {
		return color;
	}
	
}