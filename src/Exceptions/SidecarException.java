package exceptions;

/**
 * Indicates an error in the Python text gateway sidecar connection.
 *
 * @author Vinnie Di Maio
 * @version 1.0
 * @since 11/10/2026
 * @see userconnection.Client
 */
public class SidecarException extends RuntimeException {
    /**
     * Creates an exception with a detail message.
     *
     * @param message error message to send the user
     */
    public SidecarException(String message) {
        super(message);
    }
    /**
     * Creates an exception without a detail message.
     */
    public SidecarException(){super();}
}
