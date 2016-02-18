/**
 * 
 */
package rosbagreader.exceptions;

/**
 * Abstract parent of all exceptions that can be
   thrown when there is a problem with ROSBag file parsing. 
 * @author Tomas Prochazka
 *
 */
public abstract class RosbagException extends Exception {

	public RosbagException(String message) {
		super(message);
	}
	public RosbagException() {
		
	}

    public RosbagException(Throwable cause) {
        super(cause);
    }
        
	
}
