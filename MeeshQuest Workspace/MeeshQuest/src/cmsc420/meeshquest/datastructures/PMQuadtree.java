package cmsc420.meeshquest.datastructures;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import cmsc420.geom.Circle2D;
import cmsc420.geom.Geometry2D;
import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.citymapobjects.Line;
import cmsc420.meeshquest.citymapobjects.Point;
import cmsc420.meeshquest.exception.CityAlreadyMappedException;
import cmsc420.meeshquest.exception.CityOutOfBoundsException;

public class PMQuadtree {

	/** root of the PR Quadtree */
	protected Node root;

	/** bounds of the spatial map */
	protected Point2D.Float spatialOrigin;

	/** width of the spatial map */
	protected int spatialWidth;

	/** height of the spatial map */
	protected int spatialHeight;

	/** used to keep track of cities within the spatial map */
	protected HashSet<String> cityNames;


	/**
	 * Constructs an empty PR Quadtree.
	 */
	public PMQuadtree() {
		root = WhiteNode.instance;
		cityNames = new HashSet<String>();
		spatialOrigin = new Point2D.Float(0, 0);
	}

	/**
	 * Sets the width and height of the spatial map.
	 * 
	 * @param spatialWidth
	 *            width of the spatial map
	 * @param spatialHeight
	 *            height of the spatial map
	 */
	public void setRange(int spatialWidth, int spatialHeight) {
		this.spatialWidth = spatialWidth;
		this.spatialHeight = spatialHeight;
	}


	/**
	 * Gets the height of the spatial map
	 * 
	 * @return height of the spatial map
	 */
	public float getSpatialHeight() {
		return spatialHeight;
	}

	/**
	 * Gets the width of the spatial map
	 * 
	 * @return width of the spatial map
	 */
	public float getSpatialWidth() {
		return spatialWidth;
	}

	/**
	 * Gets the root node of the PR Quadtree.
	 * 
	 * @return root node of the PR Quadtree
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * Whether the PR Quadtree has zero or more elements.
	 * 
	 * @return <code>true</code> if the PR Quadtree has no non-empty nodes.
	 *         Otherwise returns <code>false</code>
	 */
	public boolean isEmpty() {
		return (root == WhiteNode.instance);
	}

	/**
	 * Inserts a city into the spatial map.
	 * 
	 * @param city
	 *            city to be added
	 * @throws CityAlreadyMappedException
	 *             city is already in the spatial map
	 * @throws CityOutOfBoundsException
	 *             city's location is outside the bounds of the spatial map
	 */
	public void add(City city) throws CityAlreadyMappedException,
			CityOutOfBoundsException {
		/* check bounds */
		int x = (int) city.getX();
		int y = (int) city.getY();
		if (x < spatialOrigin.x || x >= spatialWidth || y < spatialOrigin.y
				|| y >= spatialHeight) {
			/* city out of bounds */
			throw new CityOutOfBoundsException();
		}

		//TODO: Need to call this an isolatedCity
		/* insert city into PRQuadTree */
		cityNames.add(city.getName());
		
		root = root.add(new Point(city, "isolated"), spatialOrigin, spatialWidth, spatialHeight);
	}
	
	/**
	 * Inserts a city into the spatial map.
	 * 
	 * @param city
	 *            city to be added
	 * @throws CityAlreadyMappedException
	 *             city is already in the spatial map
	 * @throws CityOutOfBoundsException
	 *             city's location is outside the bounds of the spatial map
	 */
	public void add(City startCity, City endCity) throws CityAlreadyMappedException,
			CityOutOfBoundsException {
		/* check bounds */
		int startX = (int) startCity.getX();
		int startY = (int) startCity.getY();
		if (startX < spatialOrigin.x || startX >= spatialWidth || startY < spatialOrigin.y
				|| startY >= spatialHeight) {
			/* city out of bounds */
			throw new CityOutOfBoundsException();
		} else {
			//Need a check to see if a point is already contained;
			Point start = new Point(startCity);
			Point end = new Point(endCity);
			
			if (!contains(startCity.getName()))
				root = root.add(start, spatialOrigin, spatialWidth, spatialHeight);
			if (!contains(endCity.getName()))
				root = root.add(end, spatialOrigin, spatialWidth, spatialHeight);
			root = root.add(new Line(startCity, endCity), spatialOrigin, spatialWidth, spatialHeight);
	
			/* insert city into PRQuadTree */
			cityNames.add(startCity.getName());
			cityNames.add(endCity.getName());
		}
	}
	/**
	 * Removes a given city from the spatial map.
	 * 
	 * @param city
	 *            city to be removed
	 * @throws CityNotMappedException
	 *             city is not in the spatial map
	 */
	public boolean remove(City city) {
		final boolean success = cityNames.contains(city.getName());
		if (success) {
			cityNames.remove(city.getName());
			root = root
					.remove(city, spatialOrigin, spatialWidth, spatialHeight);
		}
		return success;
	}

	/**
	 * Clears the PR Quadtree so it contains no non-empty nodes.
	 */
	public void clear() {
		root = WhiteNode.instance;
		cityNames.clear();
	}

	/**
	 * Returns if the PR Quadtree contains a city with the given name.
	 * 
	 * @return true if the city is in the spatial map. false otherwise.
	 */
	public boolean contains(String name) {
		return cityNames.contains(name);
	}


	/**
	 * Returns if any part of a circle lies within a given rectangular bounds
	 * according to the rules of the PR Quadtree.
	 * 
	 * @param circle
	 *            circular region to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */
	public boolean intersects(Circle2D circle, Rectangle2D rect) {
		final double radiusSquared = circle.getRadius() * circle.getRadius();

		/* translate coordinates, placing circle at origin */
		final Rectangle2D.Double r = new Rectangle2D.Double(rect.getX()
				- circle.getCenterX(), rect.getY() - circle.getCenterY(), rect
				.getWidth(), rect.getHeight());

		if (r.getMaxX() < 0) {
			/* rectangle to left of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMinY() * r.getMinY()) < radiusSquared);
			} else {
				/* rectangle due west of circle */
				return (Math.abs(r.getMaxX()) < circle.getRadius());
			}
		} else if (r.getMinX() > 0) {
			/* rectangle to right of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower right corner */
				return ((r.getMinX() * r.getMinX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper right corner */
				return ((r.getMinX() * r.getMinX() + r.getMinY() * r.getMinY()) <= radiusSquared);
			} else {
				/* rectangle due east of circle */
				return (r.getMinX() <= circle.getRadius());
			}
		} else {
			/* rectangle on circle vertical centerline */
			if (r.getMaxY() < 0) {
				/* rectangle due south of circle */
				return (Math.abs(r.getMaxY()) < circle.getRadius());
			} else if (r.getMinY() > 0) {
				/* rectangle due north of circle */
				return (r.getMinY() <= circle.getRadius());
			} else {
				/* rectangle contains circle center point */
				return true;
			}
		}
	}


	private double currDistance; 
	private City closestCity;       
	
	public City findClosestPoint(int givenX, int givenY, boolean usingIsolatedCity) { 
		currDistance = Double.MAX_VALUE; 
		closestCity = null; 
		return findClosestPoint(givenX, givenY, root, usingIsolatedCity); 
	} 
	
	private City findClosestPoint(int givenX, int givenY, Node node, boolean usingIsolatedCity) { 
		if (node.getType() != Node.EMPTY) { 
			if (node.getType() == Node.INTERNAL) { 
				GreyNode greyNode = (GreyNode)node; 
				for (int i = 0; i < 4; i++) {
					findClosestPoint(givenX, givenY, greyNode.children[i], usingIsolatedCity); 
				}
			} else if (node.getType() == Node.LEAF) {  
				for (Geometry2D g : ((BlackNode) node).getAllList()) {
					if (g.getType() == Geometry2D.POINT
							&& (
								(!usingIsolatedCity && ((Point) g).isolatedString().compareTo("") == 0)
							|| (usingIsolatedCity && ((Point) g).isolatedString().compareTo("") != 0)
							)
						) {
						Point point = (Point)g;
						double distance = 
								Math.sqrt(Math.pow(point.getPoint().getX() - givenX,2) 
										+ Math.pow(point.getPoint().getY() - givenY,2)
										);
						if (distance < currDistance) { 
							closestCity = point.getCity(); 
							currDistance = distance; 
						} else if (distance == currDistance && closestCity != null) { 
							int value = closestCity.getName().compareTo
									(point.getCity().getName()); 
							if (value < 1) { 
								closestCity = point.getCity(); 
							} 
						} 
					}
				}
			} 
		} 
		return closestCity; 
	} 
    
}
