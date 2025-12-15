package boxes;

public class UnchangingBox extends Box {

    public UnchangingBox() {
        super();
    }

    @Override
    public char getTypeMarker() {
        char c = 'U';
        return c; // R for Regular Box
    }

    // Can't change the num
    @Override
    public void setTop(char letter) {
    }
}