package cmsc420.pmquadtree;

public class CityDoesNotExistThrowable extends Throwable {
	private static final long serialVersionUID = 1L;
	
	public CityDoesNotExistThrowable() {
    }

    public CityDoesNotExistThrowable(String msg) {
    	super(msg);
    } 
}
