package exceptions;

/**
 * Indicates that a grocery item is invalid.
 *
 * @author Vinnie Di Maio
 * @version 1.0
 * @since 11/05/2026
 */
public class BadItemException extends RuntimeException {
    /**
     * Creates an exception with a detail message.
     *
     * @param message error message to send the user
     */
    public BadItemException(String message) {
        super(message);
    }
    /**
     * Creates an exception without a detail message.
     */
    public BadItemException(){super();}
}
