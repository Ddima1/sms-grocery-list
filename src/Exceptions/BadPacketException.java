package exceptions;

/**
 * Indicates that a packet contains an invalid command or data.
 *
 * @author Vinnie Di Maio
 * @version 1.0
 * @since 11/04/2026
 * @see userconnection.Host
 */
public class BadPacketException extends RuntimeException {
    /**
     * Creates an exception without a detail message.
     */
    public BadPacketException(){super();}
    /**
     * Creates an exception with a detail message.
     *
     * @param message error message to send the user
     */
    public BadPacketException(String message) {
        super(message);
    }
}
