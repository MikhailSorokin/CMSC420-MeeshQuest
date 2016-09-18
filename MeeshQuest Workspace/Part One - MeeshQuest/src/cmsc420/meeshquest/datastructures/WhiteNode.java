package cmsc420.meeshquest.datastructures;

/**
 * This is a singleton class which extends
 * an abstract class Node. S WhiteNode represents an empty
 * node.
 * @author Mikhail Sorokin
 *
 */
public class WhiteNode extends Node {

	public static WhiteNode singleton;
	
	public WhiteNode() {
		singleton = this;
	}
	
}