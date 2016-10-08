package cmsc420.meeshquest.datastructures;

import java.util.ArrayList;

import org.w3c.dom.Element;

import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.utilities.XmlParser;

public class MXQuadtree {

	private GreyNode root;     
	private ArrayList<String> allRangeValues = new ArrayList<String>();;
	private boolean contains = false;
	
	public MXQuadtree() {
		root = null;
	}
	
    /* Function to check if quadTree is empty */
    public boolean isEmpty()
    {
        return root == null;
    }
    
    /* Make the tree logically empty */
    public void makeEmpty()
    {
        root = null;
    }
	
    public void insert(City cityData) {
    	if (root == null) {
    		root = new GreyNode(XmlParser.spatialWidth/2, XmlParser.spatialHeight/2, null);
    		root.data[0] = root.getCoords()[0];
    		root.data[1] = root.getCoords()[1];
    		root.upperLeft[0] = 0;
    		root.upperLeft[1] = XmlParser.spatialHeight;
    		
    		root.upperRight[0] = XmlParser.spatialWidth;
    		root.upperRight[1] = XmlParser.spatialHeight;
    		
    		root.lowerLeft[0] = 0;
    		root.lowerLeft[1] = 0;
    		
    		root.lowerRight[0] = XmlParser.spatialWidth;
    		root.lowerRight[1] = 0;
    	}
    	root = (GreyNode)root.add(cityData);
    }
    
    public void delete(String cityName) {
    	if (root != null) {		
    		root = (GreyNode) delete(root, cityName);
    	}
    }
    
    private Node delete(Node node, String cityName) {
    	if (node.getClass().getSimpleName().equals("GreyNode"))
        {
        	
        	GreyNode greyNode = (GreyNode)node;
        	
            delete(greyNode.quadrantOne, cityName);
            delete(greyNode.quadrantTwo, cityName);
            delete(greyNode.quadrantThree, cityName);
            delete(greyNode.quadrantFour, cityName);
            
    		
    		if (greyNode.quadrantOne == WhiteNode.getInstance() && greyNode.quadrantTwo == WhiteNode.getInstance()
    				&& greyNode.quadrantThree == WhiteNode.getInstance() && greyNode.quadrantFour == WhiteNode.getInstance()) {
        		
    			if (greyNode.parent != null) {
	    			if (greyNode.parent.quadrantOne == greyNode) greyNode.parent.quadrantOne = WhiteNode.getInstance();
	        		else if (greyNode.parent.quadrantTwo == greyNode) greyNode.parent.quadrantTwo = WhiteNode.getInstance(); 
	        		else if (greyNode.parent.quadrantThree == greyNode) greyNode.parent.quadrantThree = WhiteNode.getInstance(); 
	        		else if (greyNode.parent.quadrantFour == greyNode) greyNode.parent.quadrantFour = WhiteNode.getInstance();
    			} else {
    				greyNode.quadrantOne = null;
    				greyNode.quadrantTwo = null;
    				greyNode.quadrantThree = null;
    				greyNode.quadrantFour = null;
    				node = null;
    			}
    		}
        } else if (node.getClass().getSimpleName().equals("BlackNode")) { 
        	BlackNode blackNode = (BlackNode)node;
        	if (blackNode.getCityName().equals(cityName)) {
        		GreyNode parent = blackNode.getParent();

        		if (parent.quadrantOne == blackNode) parent.quadrantOne = WhiteNode.getInstance();
        		else if (parent.quadrantTwo == blackNode) parent.quadrantTwo = WhiteNode.getInstance(); 
        		else if (parent.quadrantThree == blackNode) parent.quadrantThree = WhiteNode.getInstance(); 
        		else if (parent.quadrantFour == blackNode) parent.quadrantFour = WhiteNode.getInstance();
        		
        	}
        }
        
    	return node;
    }
    
    /* Function for inorder traversal */
    public void inorder(Element parentElement)
    {
        inorder(root, parentElement);
    }
    private void inorder(Node node, Element parentElement)
    {
        if (node.getClass().getSimpleName().equals("GreyNode"))
        {
        	Element grayNode = XmlParser.results.createElement("gray");
        	grayNode.setAttribute("x", Integer.toString((int)node.data[0]));
        	grayNode.setAttribute("y", Integer.toString((int)node.data[1]));
        	parentElement.appendChild(grayNode);
        	
        	GreyNode greyNode = (GreyNode)node;
            inorder(greyNode.quadrantOne, grayNode);
            inorder(greyNode.quadrantTwo, grayNode);
            inorder(greyNode.quadrantThree, grayNode);
            inorder(greyNode.quadrantFour, grayNode);
        } else if (node.getClass().getSimpleName().equals("BlackNode")) { 
        	Element blackNode = XmlParser.results.createElement("black");
        	blackNode.setAttribute("name", ((BlackNode) node).getCityName());
        	blackNode.setAttribute("x", Integer.toString((int)node.data[0]));
        	blackNode.setAttribute("y", Integer.toString((int)node.data[1]));
        	parentElement.appendChild(blackNode);
        } else if (node.getClass().getSimpleName().equals("WhiteNode")) {
        	//If Null, create empty children Node
        	Element emptyChildElement = XmlParser.results.createElement("white"); 
        	parentElement.appendChild(emptyChildElement);
        }
    }
    
	public String findClosestPoint(int givenX, int givenY) {
		currDistance = Double.MAX_VALUE;
		closestCity = "";
		return findClosestPoint(givenX, givenY, root);
	}
	
	private double currDistance;
	private String closestCity;
	
	private String findClosestPoint(int givenX, int givenY, Node node) {
		if (node != null) {
	        if (node.getClass().getSimpleName().equals("GreyNode"))
	        {
	        	GreyNode greyNode = (GreyNode)node;
	        	findClosestPoint(givenX, givenY, greyNode.quadrantOne);
	        	findClosestPoint(givenX, givenY, greyNode.quadrantTwo);
	        	findClosestPoint(givenX, givenY, greyNode.quadrantThree);
	        	findClosestPoint(givenX, givenY, greyNode.quadrantFour);
	        } else if (node.getClass().getSimpleName().equals("BlackNode")) { 
	        	double distance = Math.sqrt(Math.pow(node.data[0] - givenX,2) + Math.pow(node.data[1] - givenY,2));
	        	if (distance < currDistance) {
	        		closestCity = ((BlackNode) node).getCityName();
	        		currDistance = distance;
	        	} else if (distance == currDistance && closestCity != null) {
	        		int value = closestCity.compareTo(((BlackNode) node).getCityName());
	        		if (value < 1) {
	        			closestCity = ((BlackNode) node).getCityName();
	        		}
	        	}
	        }
		}
        
        return closestCity;
	}

	public boolean contains(String cityName) {
		contains = false;
		contains(cityName, root);
		return contains;
	}
	
	private void contains(String cityName, Node node) {
		if (node != null) {
			if (node.getClass().getSimpleName().equals("GreyNode"))
	        {
	        	GreyNode greyNode = (GreyNode)node;
        		contains(cityName, greyNode.quadrantOne);
        		contains(cityName, greyNode.quadrantTwo);
        		contains(cityName, greyNode.quadrantThree);
        		contains(cityName, greyNode.quadrantFour);
	        } else if (node.getClass().getSimpleName().equals("BlackNode")) { 
	        	if (((BlackNode) node).getCityName().equals(cityName)) {
	        		contains = true;
	        	}
	        } 
		}
	}

	public ArrayList<String> findRangeValues(int cityXCoord, int cityYCoord, int radius) {
		allRangeValues.clear();
		return findRangeValues(cityXCoord, cityYCoord, radius, root);
	}

	
	private ArrayList<String> findRangeValues(int cityXCoord, int cityYCoord, int radius, Node node) {
		if (node != null) {
	        if (node.getClass().getSimpleName().equals("GreyNode"))
	        {
	        	GreyNode greyNode = (GreyNode)node;
	        	findRangeValues(cityXCoord, cityYCoord, radius, greyNode.quadrantOne);
	        	findRangeValues(cityXCoord, cityYCoord, radius, greyNode.quadrantTwo);
	        	findRangeValues(cityXCoord, cityYCoord, radius, greyNode.quadrantThree);
	        	findRangeValues(cityXCoord, cityYCoord, radius, greyNode.quadrantFour);
	        } else if (node.getClass().getSimpleName().equals("BlackNode")) { 
	        	BlackNode blackNode = (BlackNode)node;
	        	if (Math.sqrt(Math.pow(blackNode.getCoords()[0] - cityXCoord, 2) + Math.pow(blackNode.getCoords()[1] - cityYCoord, 2)) 
	            		<= radius) {
	            	allRangeValues.add(blackNode.getCityName());
	        	}
	        }
		}

        return allRangeValues;
	}
    
}