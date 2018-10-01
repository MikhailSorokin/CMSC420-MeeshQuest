package cmsc420.pmquadtree;

public class CityNotMappedThrowable  extends Throwable {
	private static final long serialVersionUID = 1L;
	
	public CityNotMappedThrowable() {
    }

    public CityNotMappedThrowable(String msg) {
    	super(msg);
    } 
}
