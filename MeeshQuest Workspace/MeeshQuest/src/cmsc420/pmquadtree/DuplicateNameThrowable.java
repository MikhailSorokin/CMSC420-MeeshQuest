package cmsc420.pmquadtree;

public class DuplicateNameThrowable extends Throwable {
	private static final long serialVersionUID = 1L;
	
	public DuplicateNameThrowable() {
    }

    public DuplicateNameThrowable(String msg) {
    	super(msg);
    } 
}
