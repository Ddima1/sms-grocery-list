package exceptions;

public class BadPacketException extends RuntimeException {
    public BadPacketException(){super();}
    public BadPacketException(String message) {
        super(message);
    }
}
