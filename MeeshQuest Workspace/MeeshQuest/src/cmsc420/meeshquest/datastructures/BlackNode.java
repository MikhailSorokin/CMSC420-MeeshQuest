package cmsc420.meeshquest.datastructures;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;

import cmsc420.geom.Geometry2D;
import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.citymapobjects.Point;


/**
 * Represents a leaf node of a PR Quadtree.
 */
public class BlackNode extends Node {

	protected City mxCity;
	private int cardinality = 0;
	private ArrayList<Geometry2D> geometryList = new ArrayList<Geometry2D>();
	private ArrayList<Geometry2D> allVertexList = new ArrayList<Geometry2D>(); 
	//whether or not the node has less than two times accessed with same node
	
	/**
	 * Constructs and initializes a leaf node.
	 */
	public BlackNode() {
		super(Node.LEAF);
	}

	public Node add(City newCity, Point2D.Float origin, int width,
			int height) {
		if (mxCity == null) {
			//node is empty, add city
			mxCity = newCity;
			return this;
		} else {
			//have not reached minimum partition, partition node and then add city
			GreyNode internalNode = new GreyNode(origin, width,
					height);
			if (mxCity != null)
				internalNode.add(mxCity, origin, width, height);
			internalNode.add(newCity, origin, width, height);
			return internalNode;
		}
	}
	
	//TODO: Don't think newCity will end up being used because it is a part of the geometry function.
	public Node add(Geometry2D g, Float origin, int width, int height) {
		
		geometryList.add(g);

		Node finale = this;
		//TODO: Need a check to make sure that
		if (!containsVertex() || g.getType() == Geometry2D.SEGMENT) {
			cardinality += 1;
			allVertexList.add(g);
		} else {
			//This is commented out in order to TRACE this!
			finale = partition(g, origin, width, height);
		}
		return finale;
	}
	
	private boolean containsVertex() {
		for (Geometry2D g : allVertexList) {
			if (g.getType() == Geometry2D.POINT) {
				return true;
			}
		}
		return false;
	}
	
	public Node partition(Geometry2D g, Float origin, int width, int height) {
		/* If we have not partitioned enough, partition node and then add city. */
		GreyNode internalNode = new GreyNode(origin, width,
				height);
		for (Geometry2D gElement : allVertexList) {
 			internalNode.add(gElement, origin, width, height);
		}
		return internalNode;
	}

	public Node remove(City city, Point2D.Float origin, int width,
			int height) {
		return WhiteNode.instance;
	}

	public void setCardinality(int cardinality) {
		this.cardinality  = cardinality;
	}
	
	public int getCardinality() {
		return cardinality;
	}
	
	public City getStartVertex() {
		return mxCity;
	}
	
	public ArrayList<Geometry2D> getAllList() {
		return allVertexList;
	}
	
}
