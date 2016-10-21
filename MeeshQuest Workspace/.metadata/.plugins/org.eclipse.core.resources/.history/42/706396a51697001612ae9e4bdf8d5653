package cmsc420.meeshquest.datastructures;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

import cmsc420.geom.Geometry2D;
import cmsc420.meeshquest.citymapobjects.City;

/**
 * This is a singleton class which extends
 * an abstract class Node. S WhiteNode represents an empty
 * node.
 * @author Mikhail Sorokin
 *
 */
public class WhiteNode extends Node {

	/** empty PR Quadtree node */
	public static WhiteNode instance = new WhiteNode();
	
	/**
	 * Constructs and initializes an empty node.
	 */
	public WhiteNode() {
		super(Node.EMPTY);
	}

	public Node add(City city, Point2D.Float origin, int width, int height) {
		Node blackNode = new BlackNode();
		return blackNode.add(city, origin, width, height);
	}
	

	@Override
	public Node add(Geometry2D g, City city, Float origin, int width, int height) {
		Node blackNode = new BlackNode();
		return blackNode.add(g, city, origin, width, height);
	}

	public Node remove(City city, Point2D.Float origin, int width,
			int height) {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}

}
