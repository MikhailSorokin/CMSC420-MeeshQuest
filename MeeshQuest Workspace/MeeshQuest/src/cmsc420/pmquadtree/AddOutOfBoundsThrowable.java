package cmsc420.pmquadtree;

public class AddOutOfBoundsThrowable extends Throwable {
	private static final long serialVersionUID = 1L;
	
	public AddOutOfBoundsThrowable() {
    }

    public AddOutOfBoundsThrowable(String msg) {
    	super(msg);
    } 
}