package exceptions;

/**
 * Indicates that an SMS message is invalid.
 *
 * @author Vinnie Di Maio
 * @version 1.0
 * @since 11/09/2026
 */
public class BadMessageException extends RuntimeException {
    /**
     * Creates an exception with a detail message.
     *
     * @param message error message to send the user
     */
    public BadMessageException(String message) {
        super(message);
    }
    /**
     * Creates an exception without a detail message.
     */
    public BadMessageException(){super();}
}
