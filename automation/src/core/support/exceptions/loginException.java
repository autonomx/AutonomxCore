package core.support.exceptions;

/**
 * login exception for retryign login failures
 * 
 * @author CAEHMAT
 *
 */
public class loginException extends Exception {
	private static final long serialVersionUID = 1L;

	public loginException(String message) {
		super(message);
	}
}