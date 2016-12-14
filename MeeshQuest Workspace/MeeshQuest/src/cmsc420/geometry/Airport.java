package cmsc420.geometry;

import java.awt.geom.Point2D;

public class Airport extends Geometry {

	/** name of this city */
	protected String name;

	/** 2D coordinates of this city */
	protected Point2D.Float localPt;
	protected Point2D.Float remotePt;

	/**
	 * Constructs a city.
	 * 
	 * @param name
	 *            name of the city
	 * @param localX
	 *            localX coordinate within the Metropole
	 * @param localY
	 *            localY coordinate within the Metropole
	 * @param remoteX
	 *            remoteX coordinate of the Metropole
	 * @param remoteY
	 *            remoteY coordinate of the Metropole
	 */
	public Airport(String name, int localX, int localY, int remoteX, int remoteY) {
		this.name = name;
		localPt = new Point2D.Float(localX, localY);
		remotePt = new Point2D.Float(remoteX, remoteY);
	}

	/**
	 * Gets the name of this city.
	 * 
	 * @return name of this city
	 */
	public String getName() {
		return name;
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
	 * Determines if this city is equal to another object. The result is true if
	 * and only if the object is not null and a City object that contains the
	 * same name, X and Y coordinates, radius, and color.
	 * 
	 * @param obj
	 *            the object to compare this city against
	 * @return <code>true</code> if cities are equal, <code>false</code>
	 *         otherwise
	 */
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			City c = (City) obj;
			return (localPt.equals(c.localPt) && remotePt.equals(c.remotePt));
		}
		return false;
	}

	/**
	 * Returns a hash code for this city.
	 * 
	 * @return hash code for this city
	 */
	public int hashCode() {
		int hash = 12;
		hash = 37 * hash + name.hashCode();
		hash = 37 * hash + localPt.hashCode();
		hash = 37 * hash + remotePt.hashCode();
		return hash;
	}

	/**
	 * Returns an (x,y) representation of the city. Important: casts the x and y
	 * coordinates to integers.
	 * 
	 * @return string representing the location of the city
	 */
	public String getLocationString() {
		final StringBuilder location = new StringBuilder();
		location.append("(");
		location.append(getRemoteX());
		location.append(",");
		location.append(getRemoteY());
		location.append(")");
		return location.toString();

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
	
	public String toString() {
		return getLocationString();
	}
	
	@Override
	public int getType() {
		return CIRCLE; //circle as in point, tee-hee
	}

}
