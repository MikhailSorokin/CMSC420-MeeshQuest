package cmsc420.meeshquest.datastructures;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;

import cmsc420.geom.Geometry2D;
import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.citymapobjects.Line;
import cmsc420.meeshquest.citymapobjects.Point;


/**
 * Represents an internal node of a MX Quadtree.
 */
public class GreyNode extends Node {
	public static int numberNodes = 0;
	
	/** children nodes of this node */
	public Node children[];
	
	/** rectangular quadrants of the children nodes */
	protected Rectangle2D.Float[] regions;

	/** origin of the rectangular bounds of this node */
	public Point2D.Float origin;

	/** origins of the rectangular bounds of each child node */
	protected Point2D.Float[] origins;

	/** width of the rectangular bounds of this node */
	public int width;

	/** height of the rectangular bounds of this node */
	public int height;

	/** half of the width of the rectangular bounds of this node */
	protected int halfWidth;

	/** half of the height of the rectangular bounds of this node */
	protected int halfHeight;

	/**
	 * Constructs and initializes this internal MX Quadtree node.
	 * 
	 * @param origin
	 *            origin of the rectangular bounds of this node
	 * @param width
	 *            width of the rectangular bounds of this node
	 * @param height
	 *            height of the rectangular bounds of this node
	 */
	public GreyNode(Point2D.Float origin, int width, int height) {
		super(Node.INTERNAL);

		this.origin = origin;

		children = new Node[4];
		for (int i = 0; i < 4; i++) {
			children[i] = WhiteNode.instance;
		}

		this.width = width;
		this.height = height;

		halfWidth = width >> 1;
		halfHeight = height >> 1;

		origins = new Point2D.Float[4];
		origins[0] = new Point2D.Float(origin.x, origin.y + halfHeight);
		origins[1] = new Point2D.Float(origin.x + halfWidth, origin.y
				+ halfHeight);
		origins[2] = new Point2D.Float(origin.x, origin.y);
		origins[3] = new Point2D.Float(origin.x + halfWidth, origin.y);

		numberNodes++;
		regions = new Rectangle2D.Float[4];
		int i = 0;
		while (i < 4) {
			regions[i] = new Rectangle2D.Float(origins[i].x, origins[i].y,
					halfWidth, halfHeight);
			i++;
		}

	}

	public Node add(City city, Point2D.Float origin, int width, int height) {
		final Point2D cityLocation = city.getPoint();
		for (int i = 0; i < 4; i++) {
			if (intersects(cityLocation, regions[i])) {
				children[i] = children[i].add(city, origins[i], halfWidth,
						halfHeight);
				break;
			}
		}
		return this;
	}
	
	@Override
	public Node add(Geometry2D g, Float origin, int width, int height) {
		for (int i = 0; i < 4; i++ ) {
			if (g.getType() == Geometry2D.POINT) {
				Point2D singlePoint = ((Point)g).getCity().getPoint();
				if (intersects(singlePoint, regions[i])) {
					children[i] = children[i].add(g, origins[i], halfWidth, halfHeight);
				}
			} else if (g.getType() == Geometry2D.SEGMENT) {
				if (intersects(((Line)g).getLine(), regions[i])) {
						children[i] = children[i].add(g, origins[i], halfWidth,
								halfHeight);
				}
			}
		}
		return this;
	}

	public Node remove(City city, Point2D.Float origin, int width,
			int height) {
		final Point2D cityLocation = city.getPoint();
		for (int i = 0; i < 4; i++) {
			if (intersects(cityLocation, regions[i])) {
				children[i] = children[i].remove(city, origins[i],
						halfWidth, halfHeight);
			}
		}
		return this;
	}
	
	public static boolean intersects(Line2D line, Rectangle2D rect) {
		return (rect.intersectsLine(line));
	}
	
	/**
	 * Returns if a point lies within a given rectangular bounds according to
	 * the rules of the MX Quadtree.
	 * 
	 * @param point
	 *            point to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */	
	public static boolean intersects(Point2D point, Rectangle2D rect) {
		//Issue is that it is not getting partitioned enough
		return (point.getX() >= rect.getMinX() && point.getX() <= rect.getMaxX()
				&& point.getY() >= rect.getMinY() && point.getY() <= rect
				.getMaxY());
	}

	/**
	 * Gets the number of empty child nodes contained by this internal node.
	 * 
	 * @return the number of empty child nodes
	 */
	protected int getNumEmptyNodes() {
		int numEmptyNodes = 0;
		for (Node node : children) {
			if (node == WhiteNode.instance) {
				numEmptyNodes++;
			}
		}
		return numEmptyNodes;
	}

	/**
	 * Gets the number of leaf child nodes contained by this internal node.
	 * 
	 * @return the number of leaf child nodes
	 */
	protected int getNumLeafNodes() {
		int numLeafNodes = 0;
		for (Node node : children) {
			if (node.getType() == Node.LEAF) {
				numLeafNodes++;
			}
		}
		return numLeafNodes;
	}

	/**
	 * Gets the child node of this node according to which quadrant it falls
	 * in
	 * 
	 * @param quadrant
	 *            quadrant number (top left is 0, top right is 1, bottom
	 *            left is 2, bottom right is 3)
	 * @return child node
	 */
	public Node getChild(int quadrant) {
		if (quadrant < 0 || quadrant > 3) {
			throw new IllegalArgumentException();
		} else {
			return children[quadrant];
		}
	}

	/**
	 * Gets the rectangular region for the specified child node of this
	 * internal node.
	 * 
	 * @param quadrant
	 *            quadrant that child lies within
	 * @return rectangular region for this child node
	 */
	public Rectangle2D.Float getChildRegion(int quadrant) {
		if (quadrant < 0 || quadrant > 3) {
			throw new IllegalArgumentException();
		} else {
			return regions[quadrant];
		}
	}

	/**
	 * Gets the rectangular region contained by this internal node.
	 * 
	 * @return rectangular region contained by this internal node
	 */
	public Rectangle2D.Float getRegion() {
		return new Rectangle2D.Float(origin.x, origin.y, width, height);
	}

	/**
	 * Gets the center X coordinate of this node's rectangular bounds.
	 * 
	 * @return center X coordinate of this node's rectangular bounds
	 */
	public int getCenterX() {
		return (int) origin.x + halfWidth;
	}

	/**
	 * Gets the center Y coordinate of this node's rectangular bounds.
	 * 
	 * @return center Y coordinate of this node's rectangular bounds
	 */
	public int getCenterY() {
		return (int) origin.y + halfHeight;
	}

	/**
	 * Gets half the width of this internal node.
	 * @return half the width of this internal node
	 */
	public int getHalfWidth() {
		return halfWidth;
	}

	/** 
	 * Gets half the height of this internal node.
	 * @return half the height of this internal node
	 */
	public int getHalfHeight() {
		return halfHeight;
	}
}
