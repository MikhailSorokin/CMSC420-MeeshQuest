package cmsc420.meeshquest.exception;

/**
 * Thrown if a road attempted to be mapped is outside the bounds of the
 * spatial map.
 */
public class RoadOutOfBoundsException extends Throwable {
	private static final long serialVersionUID = -179684890921590960L;

	public RoadOutOfBoundsException() {
	}

	public RoadOutOfBoundsException(String message) {
		super(message);
	}

}
