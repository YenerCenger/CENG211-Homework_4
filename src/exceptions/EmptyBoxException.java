package exceptions;

public class EmptyBoxException extends Exception {
    public EmptyBoxException() {
        super("BOX IS EMPTY!");
    }
}
