/**
 * 
 */
package rosbagreader.exceptions;

import rosbagreader.exceptions.RosbagException;

/**
 * @author Tomas Prochazka
 *
 */
public class UnexpectedEndOfRosbagFileException extends RosbagException {
	/**
	 * Exception thrown when data in the rosbag file are expected
	 * but end of file is found.
	 */
	public UnexpectedEndOfRosbagFileException() {
		super("Rosbag input stream ended unexpectedly.");
	}

    public UnexpectedEndOfRosbagFileException(String message) {
        super(message);
    }

    public UnexpectedEndOfRosbagFileException(Throwable cause) {
        super(cause);
    }
    
        
}
