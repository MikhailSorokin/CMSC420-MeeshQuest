package cmsc420.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Terminal extends Geometry {

	/** name of this city */
	protected String airportName;
	protected String terminalName;
	
	/** 2D coordinates of this city */
	protected Point2D.Float localPt;
	protected Point2D.Float remotePt;

	/** end city */
	protected City end;

	/** distance from start city to end city */
	protected double distance;
	/**
	 * Constructs a city.
	 * 
	  * @param name
	 *            name of the terminal city
	 * @param localX
	 *            localX coordinate within the Metropole
	 * @param localY
	 *            localY coordinate within the Metropole
	 * @param remoteX
	 *            remoteX coordinate of the Metropole
	 * @param remoteY
	 */    
	public Terminal(String airportName, String terminalName, int localX, int localY, int remoteX, int remoteY, City endCity) {
		this.airportName = airportName;
		this.terminalName = terminalName;
		localPt = new Point2D.Float(localX, localY);
		remotePt = new Point2D.Float(remoteX, remoteY);
		end = endCity;
		if (endCity != null)
			distance = localPt.distance(endCity.toPoint2D());
	}
	
	/**
	 * Returns a line segment representation of the road which extends
	 * Line2D.Double.
	 * 
	 * @return line segment representation of road
	 */
	public Line2D toLine2D() {
		return new Line2D.Float(localPt, end.toPoint2D());
	}

	/**
	 * Gets the name of this city.
	 * 
	 * @return name of this city
	 */
	public String getAirportName() {
		return airportName;
	}
	
	public String getTerminalName() {
		return terminalName;
	}

	/**
	 * Gets the X coordinate of this city.
	 * 
	 * @return X coordinate of this city
	 */
	public int getLocalX() {
		return (int) localPt.x;
	}

	/**
	 * Gets the Y coordinate of this city.
	 * 
	 * @return Y coordinate of this city
	 */
	public int getLocalY() {
		return (int) localPt.y;
	}
	
	/**
	 * Gets the X coordinate of this city.
	 * 
	 * @return X coordinate of this city
	 */
	public int getRemoteX() {
		return (int) remotePt.x;
	}

	/**
	 * Gets the Y coordinate of this city.
	 * 
	 * @return Y coordinate of this city
	 */
	public int getRemoteY() {
		return (int) remotePt.y;
	}

	/**
	 * Gets the end city.
	 * 
	 * @return end city
	 */
	public City getEnd() {
		return end;
	}

	/**
	 * Gets the distance between the start and end cities.
	 * 
	 * @return distance between the two cities
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * Returns a string representing a road. For example, a road from city A to
	 * city B will print out as: (A,B).
	 * 
	 * @return road string
	 */
	public String getCityNameString() {
		return "(" + end.getName() + ")";
	}

	/**
	 * Returns a Point2D instance representing the City's location.
	 * 
	 * @return location of this city
	 */
	public Point2D localPoint2D() {
		return new Point2D.Float(localPt.x, localPt.y);
	}

	public Point2D remotePoint2D() {
		return new Point2D.Float(remotePt.x, remotePt.y);
	}
	
	/**
	 * Determines if one road is equal to another.
	 * 
	 * @param other
	 *            the other road
	 * @return <code>true</code> if the roads are equal, false otherwise
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			Road r = (Road) obj;
			return (end.equals(r.end) && distance == r.distance);
		}
		return false;
	}

	/**
	 * Returns the hash code value of a road.
	 */
	public int hashCode() {
		final long dBits = Double.doubleToLongBits(distance);
		int hash = 35;
		hash = 37 * hash + end.hashCode();
		hash = 37 * hash + (int) (dBits ^ (dBits >>> 32));
		return hash;
	}

	public boolean contains(City city) {
		return city.equals(end);
	}
	
	public String toString() {
		return getCityNameString();
	}

	@Override
	public int getType() {
		return RECTANGLE;
	}
	
}
