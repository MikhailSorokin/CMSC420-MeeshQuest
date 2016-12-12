package cmsc420.pmquadtree;

import cmsc420.pmquadtree.PMQuadtree.Black;

public class PM1Validator implements Validator {

	@Override
	public boolean valid(final Black node) {
		return (node.containsRoad());
	}

}