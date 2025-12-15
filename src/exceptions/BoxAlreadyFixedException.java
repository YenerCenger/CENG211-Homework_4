package exceptions;

public class BoxAlreadyFixedException extends Exception {
    public BoxAlreadyFixedException() {
        super("BOX IS ALREADY A FIXED BOX.");
    }
}
