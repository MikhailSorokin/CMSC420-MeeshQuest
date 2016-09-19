package cmsc420.meeshquest.datastructures;

import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.utilities.XmlParser;

public class MXQuadtree {

	private Node root;     

    /* Constructor */
    public MXQuadtree()
    {
        root = new GreyNode(XmlParser.spatialWidth/2, XmlParser.spatialHeight/2);
    }
    /* Function to check if quadTree is empty */
    public boolean isEmpty()
    {
        return root == null;
    }
	
    public void insert(City cityData) {
    	//root = root.add(cityData);
    }
    
    private Node insert(City cityData, Node node) {
    	/*if (node == null) {
    		return new GreyNode((int)cityData.getX(), (int)cityData.getY());
    	} else if (cityData.getX() < node.getCoords()[0] && cityData.getY() > node.getCoords()[1]) {
    		node.quadrantOne = insert(cityData, node.quadrantOne);
    	} else if (cityData.getX() > node.getCoords()[0] && cityData.getY() > node.getCoords()[1]) {
    		node.quadrantTwo = insert(cityData, node.quadrantTwo);
    	} else if (cityData.getX() < node.getCoords()[0] && cityData.getY() < node.getCoords()[1]) {
    		node.quadrantThree = insert(cityData, node.quadrantThree);
    	} else if (cityData.getX() > node.getCoords()[0] && cityData.getY() < node.getCoords()[1]) { {
    		node.quadrantFour = insert(cityData, node.quadrantFour);
    	}
    	return node;*/
    	return null;
    }
    
}
