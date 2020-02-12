package core.support.exceptions;

/**
 * login exception for retrying login failures
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