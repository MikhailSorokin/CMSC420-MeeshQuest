package cmsc420.pmquadtree;

public class DuplicateCoordinateThrowable extends Throwable {
	private static final long serialVersionUID = 1L;
	
	public DuplicateCoordinateThrowable() {
    }

    public DuplicateCoordinateThrowable(String msg) {
    	super(msg);
    } 
}