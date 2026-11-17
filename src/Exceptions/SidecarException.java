package exceptions;

public class SidecarException extends RuntimeException {
    public SidecarException(String message) {
        super(message);
    }
    public SidecarException(){super();}
}
