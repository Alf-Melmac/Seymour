package de.webalf.seymour.exception;

/**
 * Exception commonly thrown in case a required resource is missing.
 *
 * @author Alf
 * @since 22.06.2020
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 3327000772094345936L;

	public ResourceNotFoundException() {
		super();
	}

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
