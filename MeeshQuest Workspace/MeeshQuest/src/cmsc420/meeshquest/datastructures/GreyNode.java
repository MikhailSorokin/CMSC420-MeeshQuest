package cmsc420.meeshquest.datastructures;

import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;

import cmsc420.meeshquest.citymapobjects.City;

/**
 * This is a singleton class which extends
 * an abstract class Node. A GreyNode represents a
 * coordinate link to four more nodes.
 * @author Mikhail Sorokin
 *
 */
public class GreyNode extends Node {
	
	public Node quadrantOne, quadrantTwo, quadrantThree, quadrantFour;
	public int[] upperLeft, upperRight, lowerLeft, lowerRight;
	public GreyNode parent;
	
	private int[] coords;
	
	public GreyNode(int xCoord, int yCoord, GreyNode parent) {
		coords = new int[2];
		coords[0] = xCoord;
		coords[1] = yCoord;
		quadrantOne = quadrantTwo = quadrantThree = quadrantFour = WhiteNode.getInstance();
		upperLeft = new int[2];
		upperRight = new int[2];
		lowerLeft = new int[2];
		lowerRight = new int[2];
		this.parent = parent;
	}
	
	public int[] getCoords() {
		return coords;
	}


	@Override
	//FIXED: Need to figure out the math
	protected Node add(City cityData) {
		int highXVal = (upperRight[0] - upperLeft[0])/2;
		int highYVal = (upperRight[1] - lowerRight[1])/2;
		int lowXVal = (lowerRight[0] - lowerLeft[0])/2;
		int lowYVal = (upperLeft[1] - lowerLeft[1])/2;
    	if ((lowXVal == 1 && highXVal == 1) || (lowYVal == 1 && highYVal == 1)) {
    		//System.out.println(lowXVal + ", " + lowYVal + ", " + highXVal + ", " + highYVal);
    		AddBlackNode(cityData);
    	} else {
    		int newXSplit, newYSplit;
			if ((int)(cityData.getX()) >= upperLeft[0] && (int)cityData.getY() >= coords[1]
					&& (int)cityData.getX() < coords[0]) {
				if (this.quadrantOne == WhiteNode.getInstance()) {
					newXSplit = (int)(upperLeft[0] + coords[0])/2;
					newYSplit = (int)(upperLeft[1] + coords[1])/2;
		    		GreyNode deeperQ1Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ1Node.data[0] = newXSplit;
		    		deeperQ1Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
			    		deeperQ1Node.upperLeft[i] = upperLeft[i];
			    		deeperQ1Node.upperRight[i] = (upperLeft[i] + upperRight[i])/2;
			    		deeperQ1Node.lowerLeft[i] = (upperLeft[i] + lowerLeft[i])/2;
			    		deeperQ1Node.lowerRight[i] = coords[i];
		    		}
		    		this.quadrantOne = deeperQ1Node;
		    		deeperQ1Node.add(cityData);
				} else {
					this.quadrantOne.add(cityData);
				}
	    	} else if ((int)(cityData.getX()) <= upperRight[0] && (int)cityData.getY() >= coords[1]
					&& (int)cityData.getX() >= coords[0]) {
				if (this.quadrantTwo == WhiteNode.getInstance()) {
					newXSplit = (int)(upperRight[0] + coords[0])/2;
					newYSplit = (int)(upperRight[1] + coords[1])/2;

		    		GreyNode deeperQ2Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ2Node.data[0] = newXSplit;
		    		deeperQ2Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ2Node.upperLeft[i] = (upperLeft[i] + upperRight[i])/2;
		    			deeperQ2Node.upperRight[i] = upperRight[i];
		    			deeperQ2Node.lowerLeft[i] = coords[i];
		    			deeperQ2Node.lowerRight[i] = (upperRight[i] + lowerRight[i])/2;
		    		}
		    		this.quadrantTwo = deeperQ2Node;
		    		deeperQ2Node.add(cityData);
				} else {
					this.quadrantTwo.add(cityData);
				}
	    	} else if ((int)(cityData.getX()) >= lowerLeft[0] && (int)cityData.getY() <= coords[1]
					&& (int)cityData.getX() < coords[0]) {
				if (this.quadrantThree == WhiteNode.getInstance()) {
					newXSplit = (int)(lowerLeft[0] + coords[0])/2;
					newYSplit = (int)(lowerLeft[1] + coords[1])/2;
					GreyNode deeperQ3Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ3Node.data[0] = newXSplit;
		    		deeperQ3Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ3Node.upperLeft[i] = (upperLeft[i] + lowerLeft[i])/2;
		    			deeperQ3Node.upperRight[i] = coords[i];
		    			deeperQ3Node.lowerLeft[i] = lowerLeft[i];
		    			deeperQ3Node.lowerRight[i] = (lowerLeft[i] + lowerRight[i])/2;
		    		}
		    		this.quadrantThree = deeperQ3Node;
		    		deeperQ3Node.add(cityData);
				} else {
					this.quadrantThree.add(cityData);
				}
	    	} else if ((int)(cityData.getX()) <= lowerRight[0] && (int)cityData.getY() < coords[1]
					&& (int)cityData.getX() >= coords[0]) {
				if (this.quadrantFour == WhiteNode.getInstance()) {
					newXSplit = (int)(lowerRight[0] + coords[0])/2;
					newYSplit = (int)(lowerRight[1] + coords[1])/2;
					
					GreyNode deeperQ4Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ4Node.data[0] = newXSplit;
		    		deeperQ4Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ4Node.upperLeft[i] = coords[i];
		    			deeperQ4Node.upperRight[i] = (upperRight[i] + lowerRight[i])/2;
		    			deeperQ4Node.lowerLeft[i] = (lowerLeft[i] + lowerRight[i])/2;
		    			deeperQ4Node.lowerRight[i] = lowerRight[i];
		    		}
		    		this.quadrantFour = deeperQ4Node;
		    		deeperQ4Node.add(cityData);
				} else {
					this.quadrantFour.add(cityData);
				}
	    	}
    	}
    	//If duplicate, just return this.
    	return this;
	}

	protected Node addPM(City cityData) {
		int highXVal = (upperRight[0] - upperLeft[0])/2;
		int highYVal = (upperRight[1] - lowerRight[1])/2;
		int lowXVal = (lowerRight[0] - lowerLeft[0])/2;
		int lowYVal = (upperLeft[1] - lowerLeft[1])/2;
    	
		/*if a node isn't contained within this quadrant, add a black node to it.*/
		/*if (!containsVertexInQuadrant(cityData)) {
			AddBlackNode(cityData);
		}*/
		
		if ((lowXVal == 1 && highXVal == 1) || (lowYVal == 1 && highYVal == 1)) {
    		//System.out.println(lowXVal + ", " + lowYVal + ", " + highXVal + ", " + highYVal);
    		AddBlackNode(cityData);
    	} else {
    		int newXSplit, newYSplit;
			if ((int)(cityData.getX()) >= upperLeft[0] && (int)cityData.getY() >= coords[1]
					&& (int)cityData.getX() < coords[0]) {
				if (this.quadrantOne == WhiteNode.getInstance()) {
					newXSplit = (int)(upperLeft[0] + coords[0])/2;
					newYSplit = (int)(upperLeft[1] + coords[1])/2;
		    		GreyNode deeperQ1Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ1Node.data[0] = newXSplit;
		    		deeperQ1Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
			    		deeperQ1Node.upperLeft[i] = upperLeft[i];
			    		deeperQ1Node.upperRight[i] = (upperLeft[i] + upperRight[i])/2;
			    		deeperQ1Node.lowerLeft[i] = (upperLeft[i] + lowerLeft[i])/2;
			    		deeperQ1Node.lowerRight[i] = coords[i];
		    		}
		    		this.quadrantOne = deeperQ1Node;
		    		deeperQ1Node.add(cityData);
				} else {
					this.quadrantOne.add(cityData);
				}
	    	} else if ((int)(cityData.getX()) <= upperRight[0] && (int)cityData.getY() >= coords[1]
					&& (int)cityData.getX() >= coords[0]) {
				if (this.quadrantTwo == WhiteNode.getInstance()) {
					newXSplit = (int)(upperRight[0] + coords[0])/2;
					newYSplit = (int)(upperRight[1] + coords[1])/2;

		    		GreyNode deeperQ2Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ2Node.data[0] = newXSplit;
		    		deeperQ2Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ2Node.upperLeft[i] = (upperLeft[i] + upperRight[i])/2;
		    			deeperQ2Node.upperRight[i] = upperRight[i];
		    			deeperQ2Node.lowerLeft[i] = coords[i];
		    			deeperQ2Node.lowerRight[i] = (upperRight[i] + lowerRight[i])/2;
		    		}
		    		this.quadrantTwo = deeperQ2Node;
		    		deeperQ2Node.add(cityData);
				} else {
					this.quadrantTwo.add(cityData);
				}
	    	} else if ((int)(cityData.getX()) >= lowerLeft[0] && (int)cityData.getY() <= coords[1]
					&& (int)cityData.getX() < coords[0]) {
				if (this.quadrantThree == WhiteNode.getInstance()) {
					newXSplit = (int)(lowerLeft[0] + coords[0])/2;
					newYSplit = (int)(lowerLeft[1] + coords[1])/2;
					GreyNode deeperQ3Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ3Node.data[0] = newXSplit;
		    		deeperQ3Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ3Node.upperLeft[i] = (upperLeft[i] + lowerLeft[i])/2;
		    			deeperQ3Node.upperRight[i] = coords[i];
		    			deeperQ3Node.lowerLeft[i] = lowerLeft[i];
		    			deeperQ3Node.lowerRight[i] = (lowerLeft[i] + lowerRight[i])/2;
		    		}
		    		this.quadrantThree = deeperQ3Node;
		    		deeperQ3Node.add(cityData);
				} else {
					this.quadrantThree.add(cityData);
				}
	    	} else if ((int)(cityData.getX()) <= lowerRight[0] && (int)cityData.getY() < coords[1]
					&& (int)cityData.getX() >= coords[0]) {
				if (this.quadrantFour == WhiteNode.getInstance()) {
					newXSplit = (int)(lowerRight[0] + coords[0])/2;
					newYSplit = (int)(lowerRight[1] + coords[1])/2;
					
					GreyNode deeperQ4Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ4Node.data[0] = newXSplit;
		    		deeperQ4Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ4Node.upperLeft[i] = coords[i];
		    			deeperQ4Node.upperRight[i] = (upperRight[i] + lowerRight[i])/2;
		    			deeperQ4Node.lowerLeft[i] = (lowerLeft[i] + lowerRight[i])/2;
		    			deeperQ4Node.lowerRight[i] = lowerRight[i];
		    		}
		    		this.quadrantFour = deeperQ4Node;
		    		deeperQ4Node.add(cityData);
				} else {
					this.quadrantFour.add(cityData);
				}
	    	}
    	}
    	//If duplicate, just return this.
    	return this;
	}
	
	@Override
	protected void delete(String cityName) {
		
	}
	
	private void AddBlackNode(City cityData) {
		//TODO: Want to return in the BlackNode class
		BlackNode blackNode = new BlackNode(cityData.getName(), (int)cityData.getX(), (int)cityData.getY(), this);
		blackNode.data[0] = (int)cityData.getX();
		blackNode.data[1] = (int)cityData.getY();
		if ((int)(cityData.getX()) <= upperRight[0] && (int)cityData.getY() >= coords[1]
				&& (int)cityData.getX() >= coords[0]) {
			this.quadrantTwo = blackNode;
		} else if ((int)(cityData.getX()) <= lowerRight[0] && (int)cityData.getY() <= coords[1]
				&& (int)cityData.getX() >= coords[0]) {
			this.quadrantFour = blackNode;
		}  else if ((int)(cityData.getX()) >= lowerLeft[0] && (int)cityData.getY() < coords[1]
				&& (int)cityData.getX() < coords[0]) {
			this.quadrantThree = blackNode;
		} else if ((int)(cityData.getX()) >= lowerLeft[0] && (int)cityData.getY() >= coords[1]
				&& (int)cityData.getX() < coords[0]) {
			this.quadrantOne = blackNode;
		}
	}

	public GreyNode addPM(City startCityData, City endCityData) {
		/*if a node isn't contained within this quadrant, add a black node to it.*/
		Line2D.Double line = new Line2D.Double(startCityData.getX(), startCityData.getY(),
				endCityData.getX(),
				endCityData.getY());
		int cardinality = 0;
		
		/* TODO: Have a check for start and end of cityData. Right now, it is just start.*/
		if ((coords[0] == startCityData.getX() && coords[1] == startCityData.getY())
			|| (coords[0] == endCityData.getX() && coords[1] == endCityData.getY())){
			BlackNode blackNode = null;
			int startY = 0, startX = 0, endY = 0, endX = 0;
			
			boolean test = line.getPathIterator(at);
			
			for (int quadrant = 1; quadrant <= 4; quadrant++) {
			
				if (quadrant == 1 && (coords[0] == startCityData.getX() && coords[1] == startCityData.getY())) {
					startY = (int) line.getY1();
					startX = (int) upperLeft[0];
					endY = (int) upperLeft[1];
					endX = (int) line.getX1();
				} else if (quadrant == 2 && (coords[0] == startCityData.getX() && coords[1] == startCityData.getY())) {
					startY = (int) line.getY1();
					startX = (int) line.getX1();
					endY = (int) upperRight[1];
					endX = (int) upperRight[0];
				} else if (quadrant == 3 && (coords[0] == startCityData.getX() && coords[1] == startCityData.getY())) {
					startY = (int) lowerLeft[1];
					startX = (int) lowerLeft[0];
					endY = (int) line.getY1();
					endX = (int) line.getX1();
				} else if (quadrant == 4 && (coords[0] == startCityData.getX() && coords[1] == startCityData.getY())) {
					startY = (int)(lowerLeft[1] + lowerRight[1])/2;
					startX = (int) line.getX1();
					endY = (int) line.getY1();
					endX = (int) lowerRight[0];
				}
				
				if (quadrant == 1 && (coords[0] == endCityData.getX() && coords[1] == endCityData.getY())) {
					startY = (int) line.getY1();
					startX = (int) upperLeft[0];
					endY = (int) upperLeft[1];
					endX = (int) line.getX1();
				} else if (quadrant == 2 && (coords[0] == endCityData.getX() && coords[1] == endCityData.getY())) {
					startY = (int) line.getY1();
					startX = (int) line.getX1();
					endY = (int) upperRight[1];
					endX = (int) upperRight[0];
				} else if (quadrant == 3 && (coords[0] == endCityData.getX() && coords[1] == endCityData.getY())) {
					startY = (int) lowerLeft[1];
					startX = (int) lowerLeft[0];
					endY = (int) line.getY1();
					endX = (int) line.getX1();
				} else if (quadrant == 4 && (coords[0] == endCityData.getX() && coords[1] == endCityData.getY())) {
					startY = (int)(lowerLeft[1] + lowerRight[1])/2;
					startX = (int) line.getX1();
					endY = (int) line.getY1();
					endX = (int) lowerRight[0];
				}
				
				for (int pointY = startY; pointY < endY; pointY++) {
					for (int pointX = startX; pointX < endX; pointX++) {
						if (line.contains(pointX, pointY)) {
							blackNode = new BlackNode(startCityData.getName(), endCityData.getName(),
									line, this);
							cardinality++;
							
							if (pointX == startCityData.getX() && pointY == startCityData.getY()
									&& line.contains(pointX, pointY)) {
								blackNode.addVertex(startCityData);
								cardinality++;
							} else if (pointX == endCityData.getX() && pointY == endCityData.getY()
									&& line.contains(pointX, pointY)) {
								blackNode.addVertex(endCityData);
								cardinality++;
							}
							
							blackNode.setCardinality(cardinality);
							//Just add the node here if it is in quadrant loop
							if (quadrant == 1)
								this.quadrantOne = blackNode;
							else if (quadrant == 2)
								this.quadrantTwo = blackNode;
							else if (quadrant == 3)
								this.quadrantThree = blackNode;
							else if (quadrant == 4)
								this.quadrantFour = blackNode;
							
							cardinality = 0;
						}
					}
				}
			}
		}
		
		/*if ((lowXVal == 1 && highXVal == 1) || (lowYVal == 1 && highYVal == 1)) {
    		//System.out.println(lowXVal + ", " + lowYVal + ", " + highXVal + ", " + highYVal);
    		AddBlackNode(cityData);
    	}*/ else {
    		int newXSplit, newYSplit;
			if ((int)(startCityData.getX()) >= upperLeft[0] && (int)startCityData.getY() >= coords[1]
					&& (int)startCityData.getX() < coords[0]) {
				if (this.quadrantOne == WhiteNode.getInstance()) {
					newXSplit = (int)(upperLeft[0] + coords[0])/2;
					newYSplit = (int)(upperLeft[1] + coords[1])/2;
		    		GreyNode deeperQ1Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ1Node.data[0] = newXSplit;
		    		deeperQ1Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
			    		deeperQ1Node.upperLeft[i] = upperLeft[i];
			    		deeperQ1Node.upperRight[i] = (upperLeft[i] + upperRight[i])/2;
			    		deeperQ1Node.lowerLeft[i] = (upperLeft[i] + lowerLeft[i])/2;
			    		deeperQ1Node.lowerRight[i] = coords[i];
		    		}
		    		this.quadrantOne = deeperQ1Node;
		    		deeperQ1Node.addPM(startCityData, endCityData);
				} else {
					this.quadrantOne.addPM(startCityData, endCityData);
				}
	    	} else if ((int)(startCityData.getX()) <= upperRight[0] && (int)startCityData.getY() >= coords[1]
					&& (int)startCityData.getX() >= coords[0]) {
				if (this.quadrantTwo == WhiteNode.getInstance()) {
					newXSplit = (int)(upperRight[0] + coords[0])/2;
					newYSplit = (int)(upperRight[1] + coords[1])/2;

		    		GreyNode deeperQ2Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ2Node.data[0] = newXSplit;
		    		deeperQ2Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ2Node.upperLeft[i] = (upperLeft[i] + upperRight[i])/2;
		    			deeperQ2Node.upperRight[i] = upperRight[i];
		    			deeperQ2Node.lowerLeft[i] = coords[i];
		    			deeperQ2Node.lowerRight[i] = (upperRight[i] + lowerRight[i])/2;
		    		}
		    		this.quadrantTwo = deeperQ2Node;
		    		deeperQ2Node.addPM(startCityData, endCityData);
				} else {
					this.quadrantTwo.addPM(startCityData, endCityData);
				}
	    	} else if ((int)(startCityData.getX()) >= lowerLeft[0] && (int)startCityData.getY() <= coords[1]
					&& (int)startCityData.getX() < coords[0]) {
				if (this.quadrantThree == WhiteNode.getInstance()) {
					newXSplit = (int)(lowerLeft[0] + coords[0])/2;
					newYSplit = (int)(lowerLeft[1] + coords[1])/2;
					GreyNode deeperQ3Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ3Node.data[0] = newXSplit;
		    		deeperQ3Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ3Node.upperLeft[i] = (upperLeft[i] + lowerLeft[i])/2;
		    			deeperQ3Node.upperRight[i] = coords[i];
		    			deeperQ3Node.lowerLeft[i] = lowerLeft[i];
		    			deeperQ3Node.lowerRight[i] = (lowerLeft[i] + lowerRight[i])/2;
		    		}
		    		this.quadrantThree = deeperQ3Node;
		    		deeperQ3Node.addPM(startCityData, endCityData);
				} else {
					this.quadrantThree.addPM(startCityData, endCityData);
				}
	    	} else if ((int)(startCityData.getX()) <= lowerRight[0] && (int)startCityData.getY() < coords[1]
					&& (int)startCityData.getX() >= coords[0]) {
				if (this.quadrantFour == WhiteNode.getInstance()) {
					newXSplit = (int)(lowerRight[0] + coords[0])/2;
					newYSplit = (int)(lowerRight[1] + coords[1])/2;
					
					GreyNode deeperQ4Node = new GreyNode(newXSplit, newYSplit, this);
		    		deeperQ4Node.data[0] = newXSplit;
		    		deeperQ4Node.data[1] = newYSplit;
		    		
		    		for (int i = 0; i < 2; i++) {
		    			deeperQ4Node.upperLeft[i] = coords[i];
		    			deeperQ4Node.upperRight[i] = (upperRight[i] + lowerRight[i])/2;
		    			deeperQ4Node.lowerLeft[i] = (lowerLeft[i] + lowerRight[i])/2;
		    			deeperQ4Node.lowerRight[i] = lowerRight[i];
		    		}
		    		this.quadrantFour = deeperQ4Node;
		    		deeperQ4Node.addPM(startCityData, endCityData);
				} else {
					this.quadrantFour.addPM(startCityData, endCityData);
				}
	    	}
    	}
    	//If duplicate, just return this.
    	return this;
	}
	
}